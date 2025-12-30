package com.bankapp.controller;

import com.bankapp.dto.LoginRequest;
import com.bankapp.model.Account;
import com.bankapp.model.Customer;
import com.bankapp.model.CustomerAccount;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.BranchRepository;
import com.bankapp.repository.CustomerAccountRepository;
import com.bankapp.repository.CustomerRepository;
import com.bankapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private AuthService authService;
    @Autowired
    private CustomerRepository customerRepo;
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private CustomerAccountRepository customerAccountRepo;
    @Autowired
    private BranchRepository branchRepo;

    // LOGIN
    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest req, HttpSession session) {
        Optional<Customer> c = authService.authenticate(req.getUsername(), req.getPassword());
        Map<String, Object> resp = new HashMap<>();
        if (c.isPresent()) {
            session.setAttribute("customerId", c.get().getCustomerID());
            resp.put("ok", true);
            resp.put("name", c.get().getName());
            resp.put("email", c.get().getEmail());
            return resp;
        } else {
            resp.put("ok", false);
            resp.put("message", "Username or password incorrect");
            return resp;
        }
    }

    // LOGOUT
    @PostMapping("/auth/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("ok", true);
    }

    // SEND OTP (for signup or password change)
    @PostMapping("/auth/send-otp")
    public Map<String, Object> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        boolean sent = authService.sendOtpEmail(email);
        return Map.of("ok", sent);
    }

    // VERIFY signup details and create new customer without account number
    // requirement
    @PostMapping("/auth/verify-signup")
    public Map<String, Object> verifySignup(@RequestBody Map<String, String> body, HttpSession session) {
        String name = body.get("name");
        String nic = body.get("nic");
        String email = body.get("email");
        String phone = body.get("phone");
        String otp = body.get("otp");
        String username = body.get("username");

        Map<String, Object> resp = new HashMap<>();

        if (!authService.verifyOtp(email, otp)) {
            resp.put("ok", false);
            resp.put("message", "OTP incorrect");
            return resp;
        }

        // Check if customer already exists with this email, NIC, or username
        Optional<Customer> existingCustomerByEmail = customerRepo.findByEmail(email);
        Optional<Customer> existingCustomerByNic = customerRepo.findByNic(nic);
        Optional<Customer> existingCustomerByUsername = customerRepo.findByUsername(username);

        if (existingCustomerByEmail.isPresent() || existingCustomerByNic.isPresent()
                || existingCustomerByUsername.isPresent()) {
            resp.put("ok", false);
            resp.put("message", "Customer with this email, NIC, or username already exists");
            return resp;
        }

        // Create new customer
        Customer newCustomer = new Customer();
        newCustomer.setName(name);
        newCustomer.setNic(nic);
        newCustomer.setEmail(email);
        newCustomer.setPhoneNumber(phone);
        // Don't set username yet - let AuthService handle it

        // Save the new customer to get the ID
        Customer savedCustomer = customerRepo.save(newCustomer);

        // Get the password from the request
        String password = body.get("password");

        // Create credentials for the customer
        boolean created = authService.createCredentialsForCustomer(savedCustomer.getCustomerID(), username, password);
        if (!created) {
            resp.put("ok", false);
            resp.put("message", "Failed to create credentials");
            return resp;
        }

        // Store customer ID in session for login
        session.setAttribute("customerId", savedCustomer.getCustomerID());
        resp.put("ok", true);
        return resp;
    }

    // CREATE CREDENTIALS after verify-signup
    @PostMapping("/auth/create-credentials")
    public Map<String, Object> createCredentials(@RequestBody Map<String, String> body, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("signupCustomerId");
        String accountNo = (String) session.getAttribute("signupAccountNo");
        if (customerId == null || accountNo == null) {
            return Map.of("ok", false, "message", "No signup session present");
        }
        String username = body.get("username");
        String password = body.get("password");

        // basic checks omitted for brevity: unique username, password strength
        Optional<Customer> maybe = customerRepo.findByUsername(username);
        if (maybe.isPresent())
            return Map.of("ok", false, "message", "Username already taken");

        boolean created = authService.createCredentialsForCustomer(customerId, username, password);
        if (!created)
            return Map.of("ok", false, "message", "Failed to create credentials");

        // add customer account and mark primary
        authService.addCustomerAccount(customerId, accountNo, true);

        // clear signup session
        session.removeAttribute("signupCustomerId");
        session.removeAttribute("signupAccountNo");

        return Map.of("ok", true);
    }

    // SEND OTP for password reset identity verification
    @PostMapping("/auth/send-otp-for-reset")
    public Map<String, Object> sendOtpForReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        boolean sent = authService.sendOtpEmail(email);
        return Map.of("ok", sent);
    }

    // VERIFY identity details for password reset
    @PostMapping("/auth/verify-reset")
    public Map<String, Object> verifyReset(@RequestBody Map<String, String> body, HttpSession session) {
        String name = body.get("name");
        String nic = body.get("nic");
        String email = body.get("email");
        String otp = body.get("otp");

        if (!authService.verifyOtp(email, otp))
            return Map.of("ok", false, "message", "OTP incorrect");

        Optional<Customer> oc = customerRepo.findByNameAndNic(name, nic);
        if (oc.isEmpty())
            return Map.of("ok", false, "message", "Customer details not match");

        // store in session
        session.setAttribute("resetCustomerId", oc.get().getCustomerID());
        return Map.of("ok", true);
    }

    // CHANGE PASSWORD after reset verification
    @PostMapping("/auth/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> body, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("resetCustomerId");
        if (customerId == null)
            return Map.of("ok", false, "message", "No reset session");

        String newPassword = body.get("password");
        boolean changed = authService.changePasswordByCustomer(customerId, newPassword);
        if (changed) {
            session.removeAttribute("resetCustomerId");
            return Map.of("ok", true);
        } else {
            return Map.of("ok", false, "message", "Failed to change password");
        }
    }

    // DASHBOARD: fetch customer and accounts
    @GetMapping("/dashboard/me")
    public Map<String, Object> dashboard(HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null)
            return Map.of("ok", false, "message", "Not logged in");

        Optional<Customer> oc = customerRepo.findById(customerId);
        if (oc.isEmpty())
            return Map.of("ok", false, "message", "Customer not found");

        Customer c = oc.get();
        List<CustomerAccount> cas = customerAccountRepo.findByCustomerID(customerId);
        List<Map<String, Object>> accounts = new ArrayList<>();
        // primary first
        cas.sort((a, b) -> Boolean.compare(b.getIsPrimary() != null && b.getIsPrimary(),
                a.getIsPrimary() != null && a.getIsPrimary()));
        for (CustomerAccount ca : cas) {
            Optional<Account> aopt = accountRepo.findById(ca.getAccountNo());
            if (aopt.isPresent()) {
                Account a = aopt.get();
                Map<String, Object> m = new HashMap<>();
                m.put("accountNo", a.getAccountNo());
                m.put("accountType", a.getAccountType());
                m.put("balance", a.getAccountBalance());
                m.put("isPrimary", ca.getIsPrimary());
                accounts.add(m);
            }
        }
        return Map.of("ok", true, "name", c.getName(), "email", c.getEmail(), "accounts", accounts);
    }

    // BANK TRANSFER: get source account info (account number/name/bank) for
    // logged-in user
    @GetMapping("/transfer/source")
    public Map<String, Object> transferSource(HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null)
            return Map.of("ok", false, "message", "Not logged in");

        List<CustomerAccount> cas = customerAccountRepo.findByCustomerID(customerId);
        if (cas.isEmpty())
            return Map.of("ok", false, "message", "No accounts found");

        // find primary first
        cas.sort((a, b) -> Boolean.compare(b.getIsPrimary() != null && b.getIsPrimary(),
                a.getIsPrimary() != null && a.getIsPrimary()));
        CustomerAccount ca = cas.get(0);
        Optional<Account> aopt = accountRepo.findById(ca.getAccountNo());
        if (aopt.isEmpty())
            return Map.of("ok", false, "message", "Account record not found");
        Account a = aopt.get();

        Optional<Customer> cust = customerRepo.findById(customerId);
        String accountName = cust.map(Customer::getName).orElse(null);
        String bankName = null;
        if (a.getBranchID() != null) {
            bankName = branchRepo.findById(a.getBranchID()).map(b -> b.getBranchName()).orElse(null);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        resp.put("accountNo", a.getAccountNo());
        resp.put("accountName", accountName);
        resp.put("bank", bankName);
        resp.put("balance", a.getAccountBalance());
        return resp;
    }
}