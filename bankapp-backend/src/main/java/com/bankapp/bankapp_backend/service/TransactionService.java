package com.bankapp.bankapp_backend.service;

import com.bankapp.bankapp_backend.dto.BalanceResponse;
import com.bankapp.bankapp_backend.dto.TransactionReceipt;
import com.bankapp.bankapp_backend.dto.TransferRequest;
import com.bankapp.bankapp_backend.entity.Transaction;
import com.bankapp.bankapp_backend.entity.User;
import com.bankapp.bankapp_backend.exception.InsufficientBalanceException;
import com.bankapp.bankapp_backend.exception.TransactionFailedException;
import com.bankapp.bankapp_backend.exception.UserNotFoundException;
import com.bankapp.bankapp_backend.repository.TransactionRepository;
import com.bankapp.bankapp_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Get user balance by username
     */
    public BalanceResponse getUserBalance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        return new BalanceResponse(
                user.getAccountNumber(),
                user.getAccountName(),
                user.getBalance());
    }

    /**
     * Check if user has sufficient balance for a transaction
     */
    public BalanceResponse checkSufficientBalance(String username, BigDecimal amount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        boolean isSufficient = user.getBalance().compareTo(amount) >= 0;
        String message = isSufficient
                ? "Sufficient balance available"
                : "Insufficient balance. Current balance: " + user.getBalance();

        return new BalanceResponse(
                user.getAccountNumber(),
                user.getAccountName(),
                user.getBalance(),
                isSufficient,
                message);
    }

    /**
     * Process bank transfer transaction
     */
    @Transactional
    public TransactionReceipt processTransfer(String username, TransferRequest request) {
        try {
            // Find sender
            User sender = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            // Validate balance
            if (sender.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance. Current balance: " + sender.getBalance() +
                                ", Required: " + request.getAmount());
            }

            // Create transaction entity
            Transaction transaction = new Transaction(
                    sender,
                    sender.getAccountNumber(),
                    sender.getAccountName(),
                    request.getAmount(),
                    request.getRecipientAccountNumber(),
                    request.getRecipientAccountName(),
                    request.getRecipientBank(),
                    request.getDescription());

            // Deduct amount from sender's balance
            BigDecimal newBalance = sender.getBalance().subtract(request.getAmount());
            sender.setBalance(newBalance);

            // Update transaction details
            transaction.setBalanceAfterTransaction(newBalance);
            transaction.setStatus("SUCCESS");

            // Save transaction and update user
            Transaction savedTransaction = transactionRepository.save(transaction);
            userRepository.save(sender);

            logger.info("Transaction successful: {} - Amount: {}",
                    savedTransaction.getTransactionId(), request.getAmount());

            // Generate and return receipt
            return new TransactionReceipt(
                    savedTransaction.getTransactionId(),
                    savedTransaction.getTransactionDate(),
                    savedTransaction.getSenderAccountNumber(),
                    savedTransaction.getSenderAccountName(),
                    savedTransaction.getAmount(),
                    savedTransaction.getRecipientAccountNumber(),
                    savedTransaction.getRecipientAccountName(),
                    savedTransaction.getRecipientBank(),
                    savedTransaction.getDescription(),
                    savedTransaction.getBalanceAfterTransaction(),
                    "SUCCESS",
                    "Transaction completed successfully");

        } catch (InsufficientBalanceException | UserNotFoundException e) {
            logger.error("Transaction failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Transaction processing error: {}", e.getMessage(), e);
            throw new TransactionFailedException("Failed to process transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Get transaction by transaction ID
     */
    public TransactionReceipt getTransactionReceipt(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        return new TransactionReceipt(
                transaction.getTransactionId(),
                transaction.getTransactionDate(),
                transaction.getSenderAccountNumber(),
                transaction.getSenderAccountName(),
                transaction.getAmount(),
                transaction.getRecipientAccountNumber(),
                transaction.getRecipientAccountName(),
                transaction.getRecipientBank(),
                transaction.getDescription(),
                transaction.getBalanceAfterTransaction(),
                transaction.getStatus(),
                "Transaction details retrieved");
    }
}
