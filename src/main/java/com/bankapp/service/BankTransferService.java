package com.bankapp.service;

import com.bankapp.dto.BankTransferRequest;
import com.bankapp.dto.BankTransferResponse;
import com.bankapp.model.Account;
import com.bankapp.model.BankTransfer;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.BankTransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BankTransferService {

    private static final Logger logger = LoggerFactory.getLogger(BankTransferService.class);

    @Autowired
    private BankTransferRepository bankTransferRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthService authService; // Using existing AuthService for customer verification

    // Rate limiting - track transfers per customer
    private final java.util.Map<Integer, java.util.List<java.time.LocalDateTime>> customerTransferLog = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Initiates a bank transfer by validating the request and creating a pending
     * transfer record
     */
    @Transactional
    public BankTransferResponse initiateTransfer(BankTransferRequest request, Integer customerId) {
        try {
            // Check rate limiting - max 5 transfers per hour per customer
            if (isRateLimited(customerId)) {
                BankTransferResponse response = new BankTransferResponse(
                        "Transfer limit exceeded. Maximum 5 transfers per hour.", "FAILED");
                return response;
            }

            // Validate the request data
            BankTransferResponse validationResponse = validateTransfer(request, customerId);
            if (!"SUCCESS".equals(validationResponse.getTransferStatus())) {
                return validationResponse;
            }

            // Check if sender account belongs to the authenticated customer
            Optional<Account> senderAccountOpt = accountRepository.findByAccountNo(request.getSenderAccountNo());
            if (senderAccountOpt.isEmpty() || !senderAccountOpt.get().getCustomerID().equals(customerId)) {
                BankTransferResponse response = new BankTransferResponse(
                        "Sender account does not belong to the authenticated customer", "FAILED");
                return response;
            }

            Account senderAccount = senderAccountOpt.get();

            // Log incoming amount and sender balance for debugging
            logger.info("Initiate transfer request: senderAccount={}, requestedAmount={}, senderBalance={}",
                    request.getSenderAccountNo(), request.getTransferAmount(), senderAccount.getAccountBalance());

            // Check if sender has sufficient balance using BigDecimal
            BigDecimal senderBalanceBd = BigDecimal.valueOf(senderAccount.getAccountBalance());
            if (senderBalanceBd.compareTo(request.getTransferAmount()) < 0) {
                BankTransferResponse response = new BankTransferResponse(
                        "Insufficient balance in sender account", "FAILED");
                return response;
            }

            // Create a new bank transfer record and attach managed sender account
            BankTransfer bankTransfer = new BankTransfer();
            bankTransfer.setSenderCustomerId(customerId);
            // Attach the managed Account entity to avoid persisting a transient Account
            bankTransfer.setSenderAccount(senderAccount);
            bankTransfer.setRecipientAccountNo(request.getRecipientAccountNo());
            bankTransfer.setRecipientBank(request.getRecipientBank());
            bankTransfer.setRecipientBranch(request.getRecipientBranch());
            bankTransfer.setRecipientName(request.getRecipientName());
            bankTransfer.setTransferAmount(request.getTransferAmount());
            bankTransfer.setReference(request.getReference());
            bankTransfer.setTransferStatus("PENDING");

            BankTransfer savedTransfer = bankTransferRepository.save(bankTransfer);

            // Log the transfer attempt for rate limiting
            logTransferAttempt(customerId);

            BankTransferResponse response = new BankTransferResponse(
                    "Transfer initiated successfully. Please confirm to complete the transfer.", "PENDING");
            response.setTransferId(savedTransfer.getTransferId());
            response.setSenderAccountNo(savedTransfer.getSenderAccountNo());
            response.setRecipientAccountNo(savedTransfer.getRecipientAccountNo());
            response.setTransferAmount(savedTransfer.getTransferAmount().doubleValue());
            response.setTransferDate(savedTransfer.getTransferDate());

            return response;
        } catch (Exception e) {
            BankTransferResponse response = new BankTransferResponse(
                    "Error initiating transfer: " + e.getMessage(), "FAILED");
            return response;
        }
    }

    /**
     * Checks if customer has exceeded transfer rate limit
     */
    private boolean isRateLimited(Integer customerId) {
        java.time.LocalDateTime oneHourAgo = java.time.LocalDateTime.now().minusHours(1);

        customerTransferLog.putIfAbsent(customerId, new java.util.ArrayList<>());
        java.util.List<java.time.LocalDateTime> recentTransfers = customerTransferLog.get(customerId);

        // Remove transfers older than 1 hour
        recentTransfers.removeIf(timestamp -> timestamp.isBefore(oneHourAgo));

        // Check if customer has made 5 or more transfers in the last hour
        return recentTransfers.size() >= 5;
    }

    /**
     * Logs a transfer attempt for rate limiting purposes
     */
    private void logTransferAttempt(Integer customerId) {
        customerTransferLog.putIfAbsent(customerId, new java.util.ArrayList<>());
        customerTransferLog.get(customerId).add(java.time.LocalDateTime.now());
    }

    /**
     * Confirms a bank transfer by processing the actual fund transfer
     */
    @Transactional
    public BankTransferResponse confirmTransfer(Integer transferId, Integer customerId) {
        try {
            // Get the pending transfer
            Optional<BankTransfer> transferOpt = bankTransferRepository.findById(transferId);
            if (transferOpt.isEmpty()) {
                BankTransferResponse response = new BankTransferResponse(
                        "Transfer not found", "FAILED");
                return response;
            }

            BankTransfer transfer = transferOpt.get();

            // Verify the transfer belongs to the authenticated customer
            if (!transfer.getSenderCustomerId().equals(customerId)) {
                BankTransferResponse response = new BankTransferResponse(
                        "Unauthorized: This transfer does not belong to you", "FAILED");
                return response;
            }

            // Ensure transfer is still pending
            if (!"PENDING".equals(transfer.getTransferStatus())) {
                BankTransferResponse response = new BankTransferResponse(
                        "Transfer is not in pending state", "FAILED");
                return response;
            }

            // Get sender account and verify sufficient balance
            Optional<Account> senderAccountOpt = accountRepository.findByAccountNo(transfer.getSenderAccountNo());
            if (senderAccountOpt.isEmpty()) {
                BankTransferResponse response = new BankTransferResponse(
                        "Sender account not found", "FAILED");
                return response;
            }

            Account senderAccount = senderAccountOpt.get();

            // Double-check balance as it might have changed since initiation (use
            // BigDecimal)
            BigDecimal currentBalance = BigDecimal.valueOf(senderAccount.getAccountBalance());
            if (currentBalance.compareTo(transfer.getTransferAmount()) < 0) {
                BankTransferResponse response = new BankTransferResponse(
                        "Insufficient balance. Balance may have changed since initiation.", "FAILED");
                return response;
            }

            // Check for account status - only process if account is active
            if (!"Active".equalsIgnoreCase(senderAccount.getAccountStatus())) {
                BankTransferResponse response = new BankTransferResponse(
                        "Sender account is not active", "FAILED");
                return response;
            }

            // Perform the actual transfer using BigDecimal arithmetic
            BigDecimal beforeBd = BigDecimal.valueOf(senderAccount.getAccountBalance());
            BigDecimal deductBd = transfer.getTransferAmount();
            logger.info("Confirming transfer {}: beforeBalance={}, deduct={}", transfer.getTransferId(), beforeBd,
                    deductBd);

            BigDecimal afterBd = beforeBd.subtract(deductBd);
            senderAccount.setAccountBalance(afterBd.doubleValue());
            accountRepository.save(senderAccount);
            logger.info("After transfer {}: newBalance={}", transfer.getTransferId(), afterBd);

            // Credit recipient account if it exists in our system
            String recipientAccNo = transfer.getRecipientAccountNo();
            try {
                Optional<Account> recipientOpt = accountRepository.findByAccountNo(recipientAccNo);
                if (recipientOpt.isPresent()) {
                    Account recipient = recipientOpt.get();
                    BigDecimal recipientBefore = BigDecimal.valueOf(recipient.getAccountBalance());
                    BigDecimal recipientAfter = recipientBefore.add(deductBd);
                    recipient.setAccountBalance(recipientAfter.doubleValue());
                    accountRepository.save(recipient);
                    logger.info("Credited recipient {}: before={}, after={}", recipientAccNo, recipientBefore,
                            recipientAfter);
                } else {
                    // Recipient account not present locally (external bank) — log and continue
                    logger.info("Recipient account {} not found locally; assuming external credit handled offsite",
                            recipientAccNo);
                }
            } catch (Exception ex) {
                logger.error("Failed to credit recipient account {}: {}", recipientAccNo, ex.getMessage());
            }

            // Update transfer status to completed
            transfer.setTransferStatus("COMPLETED");
            BankTransfer updatedTransfer = bankTransferRepository.save(transfer);

            BankTransferResponse response = new BankTransferResponse(
                    "Transfer completed successfully", "COMPLETED");
            response.setTransferId(updatedTransfer.getTransferId());
            response.setSenderAccountNo(updatedTransfer.getSenderAccountNo());
            response.setRecipientAccountNo(updatedTransfer.getRecipientAccountNo());
            response.setTransferAmount(updatedTransfer.getTransferAmount().doubleValue());
            response.setTransferDate(updatedTransfer.getTransferDate());
            response.setCompletedDate(updatedTransfer.getCompletedDate());

            return response;
        } catch (Exception e) {
            BankTransferResponse response = new BankTransferResponse(
                    "Error confirming transfer: " + e.getMessage(), "FAILED");
            return response;
        }
    }

    /**
     * Validates transfer data before processing
     */
    public BankTransferResponse validateTransfer(BankTransferRequest request, Integer customerId) {
        try {
            // Validate sender account exists and belongs to customer
            Optional<Account> senderAccountOpt = accountRepository.findByAccountNo(request.getSenderAccountNo());
            if (senderAccountOpt.isEmpty()) {
                BankTransferResponse response = new BankTransferResponse(
                        "Sender account does not exist", "FAILED");
                return response;
            }

            Account senderAccount = senderAccountOpt.get();
            if (!senderAccount.getCustomerID().equals(customerId)) {
                BankTransferResponse response = new BankTransferResponse(
                        "Sender account does not belong to the authenticated customer", "FAILED");
                return response;
            }

            // Validate transfer amount is positive and within reasonable limits
            if (request.getTransferAmount().compareTo(BigDecimal.ZERO) <= 0) {
                BankTransferResponse response = new BankTransferResponse(
                        "Transfer amount must be greater than zero", "FAILED");
                return response;
            }

            // Check for maximum transfer limit (e.g., 1 million)
            if (request.getTransferAmount().compareTo(new BigDecimal("1000000.00")) > 0) {
                BankTransferResponse response = new BankTransferResponse(
                        "Transfer amount exceeds maximum limit of 1,000,000.00", "FAILED");
                return response;
            }

            // Check for sufficient balance using BigDecimal
            if (BigDecimal.valueOf(senderAccount.getAccountBalance()).compareTo(request.getTransferAmount()) < 0) {
                BankTransferResponse response = new BankTransferResponse(
                        "Insufficient balance in sender account", "FAILED");
                return response;
            }

            // Validate recipient account format (alphanumeric, 6-50 chars)
            if (request.getRecipientAccountNo().length() != 6) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient account number must be exactly 6 characters", "FAILED");
                return response;
            }

            if (!request.getRecipientAccountNo().matches("^[a-zA-Z0-9]+$")) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient account number can only contain letters and numbers", "FAILED");
                return response;
            }

            // Validate recipient name format
            if (request.getRecipientName() == null || request.getRecipientName().trim().isEmpty()) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient name is required", "FAILED");
                return response;
            }

            if (request.getRecipientName().length() < 2 || request.getRecipientName().length() > 100) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient name must be between 2 and 100 characters", "FAILED");
                return response;
            }

            if (!request.getRecipientName().matches("^[a-zA-Z\\s\\.\\-']+$")) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient name contains invalid characters", "FAILED");
                return response;
            }

            // Validate recipient bank name
            if (request.getRecipientBank() == null || request.getRecipientBank().trim().isEmpty()) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient bank is required", "FAILED");
                return response;
            }

            if (request.getRecipientBank().length() < 2 || request.getRecipientBank().length() > 100) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient bank name must be between 2 and 100 characters", "FAILED");
                return response;
            }

            // Validate recipient branch
            if (request.getRecipientBranch() == null || request.getRecipientBranch().trim().isEmpty()) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient branch is required", "FAILED");
                return response;
            }

            if (request.getRecipientBranch().length() < 2 || request.getRecipientBranch().length() > 100) {
                BankTransferResponse response = new BankTransferResponse(
                        "Recipient branch name must be between 2 and 100 characters", "FAILED");
                return response;
            }

            // Validate reference if provided
            if (request.getReference() != null && request.getReference().length() > 255) {
                BankTransferResponse response = new BankTransferResponse(
                        "Reference must not exceed 255 characters", "FAILED");
                return response;
            }

            BankTransferResponse response = new BankTransferResponse(
                    "Transfer validation successful", "SUCCESS");
            return response;
        } catch (Exception e) {
            BankTransferResponse response = new BankTransferResponse(
                    "Error validating transfer: " + e.getMessage(), "FAILED");
            return response;
        }
    }

    /**
     * Gets transfer history for a specific customer
     */
    public List<BankTransferResponse> getTransferHistory(Integer customerId) {
        List<BankTransfer> transfers = bankTransferRepository.findBySenderCustomerIdAndTransferStatus(
                customerId, "COMPLETED");

        List<BankTransferResponse> responses = new ArrayList<>();
        for (BankTransfer transfer : transfers) {
            BankTransferResponse response = new BankTransferResponse();
            response.setTransferId(transfer.getTransferId());
            response.setSenderAccountNo(transfer.getSenderAccountNo());
            response.setRecipientAccountNo(transfer.getRecipientAccountNo());
            response.setRecipientBank(transfer.getRecipientBank());
            response.setRecipientBranch(transfer.getRecipientBranch());
            response.setRecipientName(transfer.getRecipientName());
            response.setTransferAmount(transfer.getTransferAmount().doubleValue());
            response.setReference(transfer.getReference());
            response.setTransferStatus(transfer.getTransferStatus());
            response.setTransferDate(transfer.getTransferDate());
            response.setCompletedDate(transfer.getCompletedDate());
            responses.add(response);
        }

        return responses;
    }

    /**
     * Gets details of a specific transfer
     */
    public BankTransferResponse getTransferDetails(Integer transferId, Integer customerId) {
        Optional<BankTransfer> transferOpt = bankTransferRepository.findById(transferId);
        if (transferOpt.isEmpty()) {
            BankTransferResponse response = new BankTransferResponse(
                    "Transfer not found", "FAILED");
            return response;
        }

        BankTransfer transfer = transferOpt.get();

        // Verify the transfer belongs to the authenticated customer
        if (!transfer.getSenderCustomerId().equals(customerId)) {
            BankTransferResponse response = new BankTransferResponse(
                    "Unauthorized: This transfer does not belong to you", "FAILED");
            return response;
        }

        BankTransferResponse response = new BankTransferResponse();
        response.setTransferId(transfer.getTransferId());
        response.setSenderAccountNo(transfer.getSenderAccountNo());
        response.setRecipientAccountNo(transfer.getRecipientAccountNo());
        response.setRecipientBank(transfer.getRecipientBank());
        response.setRecipientBranch(transfer.getRecipientBranch());
        response.setRecipientName(transfer.getRecipientName());
        response.setTransferAmount(transfer.getTransferAmount().doubleValue());
        response.setReference(transfer.getReference());
        response.setTransferStatus(transfer.getTransferStatus());
        response.setTransferDate(transfer.getTransferDate());
        response.setCompletedDate(transfer.getCompletedDate());
        response.setMessage("Transfer details retrieved successfully");

        return response;
    }
}