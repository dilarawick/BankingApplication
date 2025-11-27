package com.bankapp.bankapp_backend.controller;

import com.bankapp.bankapp_backend.dto.BalanceResponse;
import com.bankapp.bankapp_backend.dto.TransactionReceipt;
import com.bankapp.bankapp_backend.dto.TransferRequest;
import com.bankapp.bankapp_backend.enums.BankName;
import com.bankapp.bankapp_backend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * Get current user's account balance
     * Automatically retrieves balance when user logs in or visits transfer page
     */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(Authentication authentication) {
        String username = authentication.getName();
        BalanceResponse balance = transactionService.getUserBalance(username);
        return ResponseEntity.ok(balance);
    }

    /**
     * Check if user has sufficient balance for a specific amount
     */
    @GetMapping("/balance/check")
    public ResponseEntity<BalanceResponse> checkBalance(
            @RequestParam BigDecimal amount,
            Authentication authentication) {
        String username = authentication.getName();
        BalanceResponse balanceCheck = transactionService.checkSufficientBalance(username, amount);
        return ResponseEntity.ok(balanceCheck);
    }

    /**
     * Get list of available banks for dropdown
     */
    @GetMapping("/banks")
    public ResponseEntity<BankName[]> getBanks() {
        return ResponseEntity.ok(BankName.values());
    }

    /**
     * Process bank transfer
     * Validates balance, records transaction, and returns receipt
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransactionReceipt> processTransfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionReceipt receipt = transactionService.processTransfer(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    /**
     * Get transaction receipt by transaction ID
     */
    @GetMapping("/receipt/{transactionId}")
    public ResponseEntity<TransactionReceipt> getReceipt(@PathVariable String transactionId) {
        TransactionReceipt receipt = transactionService.getTransactionReceipt(transactionId);
        return ResponseEntity.ok(receipt);
    }
}
