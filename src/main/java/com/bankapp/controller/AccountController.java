package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.model.Branch;
import com.bankapp.repository.BranchRepository;
import com.bankapp.model.CustomerAccount;
import com.bankapp.service.AuthService;
import com.bankapp.repository.CustomerAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.bankapp.model.Customer;
import com.bankapp.repository.AccountRepository;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomerAccountRepository customerAccountRepo;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BranchRepository branchRepository;

    // Add account endpoint - creates a new CustomerAccount relationship
    @PostMapping("/add")
    public ResponseEntity<?> addAccount(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            String branch = request.get("branch");
            String accountNumber = request.get("accountNumber");
            String accountType = request.get("accountType");
            String accountNickname = request.get("accountNickname");

            // Validate required fields
            if (branch == null || branch.trim().isEmpty() ||
                    accountNumber == null || accountNumber.trim().isEmpty() ||
                    accountType == null || accountType.trim().isEmpty() ||
                    accountNickname == null || accountNickname.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "All fields are required"));
            }

            // Validate branch against allowed list
            List<String> allowedBranches = List.of("colombo", "colombo central", "colombo fort", "kandy", "galle",
                    "jaffna", "gampaha", "kurunegala",
                    "matara", "anuradhapura", "badulla", "ratnapura");
            if (!allowedBranches.contains(branch.toLowerCase())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid branch selected"));
            }

            // Validate account number format
            if (accountNumber.length() < 6 || accountNumber.length() > 50) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account number must be between 6 and 50 characters"));
            }
            if (!accountNumber.matches("^[a-zA-Z0-9]+$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account number can only contain letters and numbers"));
            }

            // Validate account type
            List<String> allowedTypes = List.of("savings", "current", "fixed_deposit");
            if (!allowedTypes.contains(accountType.toLowerCase())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid account type"));
            }

            // Validate nickname
            if (accountNickname.length() < 2 || accountNickname.length() > 50) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account nickname must be between 2 and 50 characters"));
            }
            if (!accountNickname.matches("^[a-zA-Z0-9\\s]+$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account nickname can only contain letters, numbers, and spaces"));
            }

            // Check for duplicate account for same customer
            List<CustomerAccount> existing = customerAccountRepo.findByCustomerIDAndAccountNo(customerId,
                    accountNumber);
            if (existing != null && !existing.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "This account is already linked to your profile"));
            }

            // Require OTP verification for this account addition
            boolean isVerified = authService.isAccountOtpVerified(customerId, accountNumber);
            if (!isVerified) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account not verified. Please complete OTP verification."));
            }

            // Persist the new customer-account link
            CustomerAccount customerAccount = new CustomerAccount();
            customerAccount.setCustomerID(customerId);
            customerAccount.setAccountNo(accountNumber);
            customerAccount.setAccountType(accountType);
            customerAccount.setAccountNickname(accountNickname);

            CustomerAccount savedAccount = authService.addCustomerAccount(customerAccount);

            return ResponseEntity.ok(savedAccount);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to add account: " + e.getMessage()));
        }
    }

    // Get all accounts for a customer
    @GetMapping("/my-accounts")
    public ResponseEntity<List<Map<String, Object>>> getMyAccounts(Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            List<CustomerAccount> customerAccounts = authService.getCustomerAccounts(customerId);

            List<Map<String, Object>> result = new ArrayList<>();

            for (CustomerAccount customerAccount : customerAccounts) {
                // Get the corresponding account to get branch information
                Optional<Account> accountOpt = accountRepository.findByAccountNo(customerAccount.getAccountNo());

                Map<String, Object> accountData = new HashMap<>();
                accountData.put("accountNo", customerAccount.getAccountNo());
                accountData.put("accountType", customerAccount.getAccountType());
                accountData.put("accountNickname", customerAccount.getAccountNickname());
                accountData.put("isPrimary", customerAccount.getIsPrimary());
                accountData.put("addedDate", customerAccount.getAddedDate());

                if (accountOpt.isPresent()) {
                    Account account = accountOpt.get();
                    // Get branch information
                    Optional<Branch> branchOpt = branchRepository.findById(account.getBranchID());
                    if (branchOpt.isPresent()) {
                        accountData.put("branch", branchOpt.get().getCity().toLowerCase());
                    } else {
                        accountData.put("branch", customerAccount.getAccountNo()); // fallback
                    }
                } else {
                    accountData.put("branch", customerAccount.getAccountNo()); // fallback
                }

                result.add(accountData);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get balance for a specific account (must belong to logged-in user)
    @GetMapping("/{accountNo}/balance")
    public ResponseEntity<?> getAccountBalance(@PathVariable String accountNo, Authentication authentication) {
        try {
            if (accountNo == null || accountNo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Account number is required"));
            }

            var opt = accountRepository.findByAccountNo(accountNo);
            if (opt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Account not found"));
            }

            Account account = opt.get();
            Integer customerId = (Integer) authentication.getPrincipal();
            if (!account.getCustomerID().equals(customerId)) {
                return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
            }

            return ResponseEntity.ok(Map.of(
                    "accountNo", account.getAccountNo(),
                    "accountBalance", account.getAccountBalance()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get balance: " + e.getMessage()));
        }
    }

    // Verify account endpoint - checks if account exists with the bank
    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String accountNumber = request.get("accountNumber");
            String branch = request.get("branch");
            String accountType = request.get("accountType");

            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account number is required", "isValid", false));
            }

            var opt = accountRepository.findByAccountNo(accountNumber);
            if (opt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Account does not exist", "isValid", false));
            }

            Account account = opt.get();

            // Validate branch
            if (branch == null || branch.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Branch is required", "isValid", false));
            }

            // Normalize: user sends lowercase city like 'colombo'
            String normalizedCity = branch.substring(0, 1).toUpperCase() + branch.substring(1).toLowerCase();
            var branchOpt = branchRepository.findByCity(normalizedCity);
            if (branchOpt.isEmpty() || !branchOpt.get().getBranchID().equals(account.getBranchID())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Select the correct branch", "isValid", false));
            }

            // Validate account type
            if (accountType == null || accountType.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account type is required", "isValid", false));
            }

            String dbType = account.getAccountType() == null ? ""
                    : account.getAccountType().toLowerCase().replaceAll("[^a-z0-9]", "");
            String inputType = accountType.toLowerCase().replaceAll("[^a-z0-9]", "");
            if (!dbType.equals(inputType)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Select the correct account type", "isValid", false));
            }

            // All good — generate account-specific OTP
            Integer customerId = (Integer) authentication.getPrincipal();
            try {
                authService.generateAccountAddOtp(customerId, accountNumber);
            } catch (Exception ex) {
                return ResponseEntity.status(500)
                        .body(Map.of("message", "Failed to send verification code", "isValid", false));
            }

            return ResponseEntity
                    .ok(Map.of("message", "Verification code sent to your registered email", "isValid", true));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to verify account: " + e.getMessage()));
        }
    }

    // Verify OTP endpoint
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String accountNumber = request.get("accountNumber");
            String otp = request.get("otp");

            if (accountNumber == null || accountNumber.trim().isEmpty() ||
                    otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Account number and OTP are required"));
            }

            // Verify OTP for this customer's account-add flow
            Integer customerId = (Integer) authentication.getPrincipal();
            boolean isValid = authService.verifyAccountOtp(customerId, accountNumber, otp);

            if (isValid) {
                return ResponseEntity.ok(Map.of(
                        "message", "OTP verified successfully",
                        "isValid", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Invalid OTP",
                        "isValid", false));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to verify OTP: " + e.getMessage()));
        }
    }

    // Update account nickname endpoint
    @PutMapping("/{accountNo}/nickname")
    public ResponseEntity<?> updateAccountNickname(@PathVariable String accountNo,
            @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            String newNickname = request.get("accountNickname");

            if (accountNo == null || accountNo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Account number is required"));
            }

            if (newNickname == null || newNickname.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Account nickname is required"));
            }

            // Validate nickname
            if (newNickname.length() < 2 || newNickname.length() > 50) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account nickname must be between 2 and 50 characters"));
            }
            if (!newNickname.matches("^[a-zA-Z0-9\\s]+$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account nickname can only contain letters, numbers, and spaces"));
            }

            // Update the nickname through the service layer
            CustomerAccount updatedAccount = authService.updateAccountNickname(customerId, accountNo, newNickname);

            if (updatedAccount == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account not found or not linked to your profile"));
            }

            return ResponseEntity.ok(updatedAccount);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to update account nickname: " + e.getMessage()));
        }
    }

    // Unlink account endpoint - removes the customer-account relationship
    @DeleteMapping("/{accountNo}/unlink")
    public ResponseEntity<?> unlinkAccount(@PathVariable String accountNo, Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            if (accountNo == null || accountNo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Account number is required"));
            }

            // Remove the customer-account relationship through the service layer
            boolean unlinked = authService.unlinkAccount(customerId, accountNo);

            if (!unlinked) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Account not found or not linked to your profile"));
            }

            return ResponseEntity.ok(Map.of("message", "Account unlinked successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to unlink account: " + e.getMessage()));
        }
    }
}