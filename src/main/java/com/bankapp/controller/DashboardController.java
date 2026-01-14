package com.bankapp.controller;

import com.bankapp.dto.DashboardTransactionDTO;
import com.bankapp.model.CustomerAccount;
import com.bankapp.repository.CustomerAccountRepository;
import com.bankapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @GetMapping("/transactions/{accountNo}")
    public ResponseEntity<?> getRecentTransactions(@PathVariable String accountNo, Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            if (customerId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Authentication failed"));
            }

            // Verify that the account belongs to the authenticated user
            List<CustomerAccount> customerAccounts = customerAccountRepository
                    .findByCustomerIDAndAccountNo(customerId, accountNo);

            if (customerAccounts == null || customerAccounts.isEmpty()) {
                return ResponseEntity.status(403).body(Map.of("message", "Account doesn't belong to user"));
            }

            // Get the 5 most recent transactions for this account
            List<com.bankapp.model.Transaction> transactions = transactionRepository
                    .findByAccountNoOrderByTransactionDateDesc(accountNo)
                    .stream()
                    .limit(5) // Get only the 5 most recent transactions
                    .collect(Collectors.toList());

            // Convert to DTOs
            List<DashboardTransactionDTO> transactionDTOs = transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(transactionDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to retrieve transactions: " + e.getMessage()));
        }
    }

    private DashboardTransactionDTO convertToDTO(com.bankapp.model.Transaction transaction) {
        DashboardTransactionDTO dto = new DashboardTransactionDTO();
        dto.setTransactionId(String.valueOf(transaction.getTransactionId()));
        dto.setAccountNo(transaction.getAccountNo());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setTransactionDate(transaction.getTransactionDate());

        // Format the transaction date as a string in the required format
        if (transaction.getTransactionDate() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setTransactionDateStr(transaction.getTransactionDate().format(formatter));
        }

        dto.setReferenceType(transaction.getReferenceType());
        return dto;
    }
}