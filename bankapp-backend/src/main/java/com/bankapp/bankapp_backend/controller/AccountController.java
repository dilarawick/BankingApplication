package com.bankapp.bankapp_backend.controller;

import com.bankapp.bankapp_backend.model.Account;
import com.bankapp.bankapp_backend.model.CustomerAccount;
import com.bankapp.bankapp_backend.repository.AccountRepository;
import com.bankapp.bankapp_backend.repository.CustomerAccountRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AccountController {

    @Autowired
    private CustomerAccountRepository repo;

    @Autowired
    private AccountRepository accountRepo;   // <-- FIXED: ADDED @Autowired

    // =======================================================
    // 1. Get accounts for logged user
    // =======================================================
    @GetMapping("/my")
    public List<Map<String, Object>> getMyAccounts(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("customerId");
        if (userId == null) return Collections.emptyList();

        List<CustomerAccount> list = repo.findByCustomerID(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (CustomerAccount acc : list) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("accountNo", acc.getAccountNo());
            obj.put("nickname", acc.getNickname());
            obj.put("isPrimary", acc.getIsPrimary());
            obj.put("addedDate", acc.getAddedDate());

            // Fetch account details (like account type) from the Account repository
            Optional<Account> account = accountRepo.findByAccountNo(acc.getAccountNo());
            if (account.isPresent()) {
                obj.put("accountType", account.get().getAccountType());  // Add accountType
            }

            result.add(obj);
        }

        return result;
    }

    // =======================================================
    // 2. Add account — with ownership validation
    // =======================================================
    @PostMapping("/add")
    public Map<String, String> addAccount(@RequestBody Map<String, String> body,
                                          HttpSession session) {

        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null)
            return Map.of("status", "error", "message", "User not logged in");

        String accountNo = body.get("accountNo");
        String nickname = body.getOrDefault("nickname", "My Account");

        if (accountNo == null || accountNo.trim().isEmpty())
            return Map.of("status", "error", "message", "Account number required");

        // 1) Verify Account exists
        var optionalAccount = accountRepo.findByAccountNo(accountNo);
        if (optionalAccount.isEmpty())
            return Map.of("status", "error", "message", "Account number not found");

        var account = optionalAccount.get();

        // 2) Validate ownership
        if (!Objects.equals(account.getCustomerID(), customerId))
            return Map.of("status", "error", "message", "This account does not belong to you");

        // 3) Check already linked
        boolean exists = repo.existsByAccountNoAndCustomerID(accountNo, customerId);
        if (exists)
            return Map.of("status", "error", "message", "You already added this account");

        // 4) Save
        CustomerAccount ca = new CustomerAccount();
        ca.setCustomerID(customerId);
        ca.setAccountNo(accountNo);
        ca.setNickname(nickname);
        ca.setIsPrimary(false);

        repo.save(ca);

        return Map.of("status", "ok");
    }

    // =======================================================
    // DELETE ACCOUNT
    // =======================================================
    @Transactional  // <-- Add @Transactional here
    @DeleteMapping("/delete/{accountNo}")
    public Map<String, String> deleteAccount(@PathVariable String accountNo, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("customerId");
        if (userId == null) {
            return Map.of("status", "error", "message", "Not logged in");
        }

        // Find the account by account number and customer ID to ensure it's the correct account
        CustomerAccount account = repo.findByAccountNoAndCustomerID(accountNo, userId);
        if (account != null) {
            repo.delete(account);  // Remove the account from the CustomerAccount table
            return Map.of("status", "ok");
        } else {
            return Map.of("status", "error", "message", "Account not found or not linked to user");
        }
    }
    // =======================================================
    // 4. Update nickname
    // =======================================================
    @PostMapping("/update-nickname")
    public Map<String, String> updateNickname(@RequestBody Map<String, String> body,
                                              HttpSession session) {

        Integer userId = (Integer) session.getAttribute("customerId");

        if (userId == null)
            return Map.of("status", "error", "message", "Not logged");

        String accountNo = body.get("accountNo");
        String nickname = body.get("nickname");

        CustomerAccount acc = repo.findByAccountNoAndCustomerID(accountNo, userId);

        if (acc == null)
            return Map.of("status", "error", "message", "Account not found");

        acc.setNickname(nickname);
        repo.save(acc);

        return Map.of("status", "ok");
    }
    @PostMapping("/check-account")
    public Map<String, String> checkAccountOwnership(@RequestBody Map<String, String> body, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return Map.of("status", "error", "message", "User not logged in");
        }

        String accountNo = body.get("accountNo");

        Optional<Account> optionalAccount = accountRepo.findByAccountNo(accountNo);
        if (optionalAccount.isEmpty()) {
            return Map.of("status", "error", "message", "Account not found");
        }

        Account account = optionalAccount.get();

        // Check if the account belongs to the logged-in user
        if (!Objects.equals(account.getCustomerID(), customerId)) {
            return Map.of("status", "error", "message", "This account does not belong to you");
        }

        return Map.of("status", "ok", "message", "Account is valid");
    }

}
