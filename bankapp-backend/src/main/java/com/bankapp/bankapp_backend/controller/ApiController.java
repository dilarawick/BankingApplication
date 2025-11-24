package com.bankapp.bankapp_backend.controller;

import com.bankapp.bankapp_backend.dto.BillCheckRequest;
import com.bankapp.bankapp_backend.dto.LoginRequest;
import com.bankapp.bankapp_backend.model.*;
import com.bankapp.bankapp_backend.repository.*;
import com.bankapp.bankapp_backend.service.AuthService;
import com.bankapp.bankapp_backend.service.OtpService;
import com.bankapp.bankapp_backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired private AuthService authService;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private CustomerAccountRepository customerAccountRepo;
    @Autowired
    private BillRepository billRepository;

    // LOGIN
    @PostMapping("/auth/login")
    public Map<String,Object> login(@RequestBody LoginRequest req, HttpSession session) {
        Optional<Customer> c = authService.authenticate(req.getUsername(), req.getPassword());
        Map<String,Object> resp = new HashMap<>();
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
    public Map<String,Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("ok", true);
    }

    // SEND OTP (for signup or password change)
    @PostMapping("/auth/send-otp")
    public Map<String,Object> sendOtp(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        boolean sent = authService.sendOtpEmail(email);
        return Map.of("ok", sent);
    }

    // VERIFY signup details (name, nic, email, accountNo) with OTP check
    @PostMapping("/auth/verify-signup")
    public Map<String,Object> verifySignup(@RequestBody Map<String,String> body, HttpSession session) {
        String name = body.get("name");
        String nic = body.get("nic");
        String email = body.get("email");
        String accountNo = body.get("accountNo");
        String otp = body.get("otp");

        Map<String,Object> resp = new HashMap<>();

        if (!authService.verifyOtp(email, otp)) {
            resp.put("ok", false);
            resp.put("message", "OTP incorrect");
            return resp;
        }

        Optional<Customer> oc = authService.findCustomerByNameNic(name, nic);
        if (oc.isEmpty()) {
            resp.put("ok", false);
            resp.put("message", "Customer details do not match");
            return resp;
        }
        Customer c = oc.get();

        // check if username exists already
        if (c.getUsername() != null) {
            resp.put("ok", false);
            resp.put("message", "User already exists (credentials set).");
            return resp;
        }

        // verify account exists
        Optional<Account> acc = authService.findAccountByAccountNo(accountNo);
        if (acc.isEmpty()) {
            resp.put("ok", false);
            resp.put("message", "Account number not found");
            return resp;
        }

        // ensure the account belongs to the same customer in DB (Account.CustomerID)
        if (!Objects.equals(acc.get().getCustomerID(), c.getCustomerID())) {
            resp.put("ok", false);
            resp.put("message", "Account does not belong to this customer");
            return resp;
        }

        // temp store customerId in session for next step (create credentials)
        session.setAttribute("signupCustomerId", c.getCustomerID());
        session.setAttribute("signupAccountNo", accountNo);
        resp.put("ok", true);
        return resp;
    }

    // CREATE CREDENTIALS after verify-signup
    @PostMapping("/auth/create-credentials")
    public Map<String,Object> createCredentials(@RequestBody Map<String,String> body, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("signupCustomerId");
        String accountNo = (String) session.getAttribute("signupAccountNo");
        if (customerId == null || accountNo == null) {
            return Map.of("ok", false, "message", "No signup session present");
        }
        String username = body.get("username");
        String password = body.get("password");

        // basic checks omitted for brevity: unique username, password strength
        Optional<Customer> maybe = customerRepo.findByUsername(username);
        if (maybe.isPresent()) return Map.of("ok", false, "message", "Username already taken");

        boolean created = authService.createCredentialsForCustomer(customerId, username, password);
        if (!created) return Map.of("ok", false, "message", "Failed to create credentials");

        // add customer account and mark primary
        authService.addCustomerAccount(customerId, accountNo, true);

        // clear signup session
        session.removeAttribute("signupCustomerId");
        session.removeAttribute("signupAccountNo");

        return Map.of("ok", true);
    }

    // SEND OTP for password reset identity verification
    @PostMapping("/auth/send-otp-for-reset")
    public Map<String,Object> sendOtpForReset(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        boolean sent = authService.sendOtpEmail(email);
        return Map.of("ok", sent);
    }

    // VERIFY identity details for password reset
    @PostMapping("/auth/verify-reset")
    public Map<String,Object> verifyReset(@RequestBody Map<String,String> body, HttpSession session) {
        String name = body.get("name");
        String nic = body.get("nic");
        String email = body.get("email");
        String otp = body.get("otp");

        if (!authService.verifyOtp(email, otp)) return Map.of("ok", false, "message", "OTP incorrect");

        Optional<Customer> oc = customerRepo.findByNameAndNic(name, nic);
        if (oc.isEmpty()) return Map.of("ok", false, "message", "Customer details not match");

        // store in session
        session.setAttribute("resetCustomerId", oc.get().getCustomerID());
        return Map.of("ok", true);
    }

    // CHANGE PASSWORD after reset verification
    @PostMapping("/auth/change-password")
    public Map<String,Object> changePassword(@RequestBody Map<String,String> body, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("resetCustomerId");
        if (customerId == null) return Map.of("ok", false, "message", "No reset session");

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
    public Map<String,Object> dashboard(HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) return Map.of("ok", false, "message", "Not logged in");

        Optional<Customer> oc = customerRepo.findById(customerId);
        if (oc.isEmpty()) return Map.of("ok", false, "message", "Customer not found");

        Customer c = oc.get();
        List<CustomerAccount> cas = customerAccountRepo.findByCustomerID(customerId);
        List<Map<String,Object>> accounts = new ArrayList<>();
        // primary first
        cas.sort((a,b) -> Boolean.compare(b.getIsPrimary()!=null && b.getIsPrimary(), a.getIsPrimary()!=null && a.getIsPrimary()));
        for (CustomerAccount ca: cas) {
            Optional<Account> aopt = accountRepo.findById(ca.getAccountNo());
            if (aopt.isPresent()) {
                Account a = aopt.get();
                Map<String,Object> m = new HashMap<>();
                m.put("accountNo", a.getAccountNo());
                m.put("accountType", a.getAccountType());
                m.put("balance", a.getAccountBalance());
                m.put("isPrimary", ca.getIsPrimary());
                accounts.add(m);
            }
        }
        return Map.of("ok", true, "name", c.getName(), "email", c.getEmail(), "accounts", accounts);
    }
    @Autowired private PaymentService paymentService;
    @Autowired private BillRepository billRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private OtpService otpService; // already present

    @PostMapping("/payments/validate-bill")
    public Map<String,Object> validateBill(@RequestBody Map<String,String> body) {
        String billNo = body.get("billNo");
        String biller = body.get("biller");
        Optional<Bill> b = paymentService.validateBill(billNo, biller);
        if (b.isPresent()) {
            return Map.of("ok", true, "bill", b.get());
        } else {
            return Map.of("ok", false, "message", "Bill not found or mismatched biller");
        }
    }

    @PostMapping("/payments/send-otp-for-payment")
    public Map<String,Object> sendOtpForPayment(@RequestBody Map<String,String> body, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) return Map.of("ok", false, "message", "Not logged in");
        // use customer's email (fetch)
        Customer c = customerRepo.findById(customerId).orElse(null);
        if (c == null || c.getEmail() == null) return Map.of("ok", false, "message", "Customer not found");
        boolean sent = authService.sendOtpEmail(c.getEmail()); // reused
        return Map.of("ok", sent);
    }

    @PostMapping("/payments/confirm")
    public Map<String,Object> confirmPayment(@RequestBody Map<String,String> body, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) return Map.of("ok", false, "message", "Not logged in");

        String fromAccount = body.get("fromAccount");
        String billNo = body.get("billNo");
        String biller = body.get("biller");
        Double amount = Double.parseDouble(body.get("amount"));
        String otp = body.get("otp");

        // verify OTP
        Customer c = customerRepo.findById(customerId).orElse(null);
        if (c == null) return Map.of("ok", false, "message", "Customer not found");

        if (!otpService.verifyOtp(c.getEmail(), otp)) {
            return Map.of("ok", false, "message", "OTP incorrect or expired");
        }

        Payment p = paymentService.processPayment(customerId, fromAccount, billNo, biller, amount);
        if ("Success".equals(p.getStatus())) {
            return Map.of("ok", true, "confirmNo", p.getConfirmNo(), "payment", p);
        } else {
            return Map.of("ok", false, "message", p.getFailureReason(), "payment", p);
        }
    }

    @GetMapping("/payments/history")
    public Map<String,Object> paymentHistory(HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) return Map.of("ok", false, "message", "Not logged in");
        List<Payment> list = paymentRepo.findByCustomerIDOrderByCreatedDateDesc(customerId);
        return Map.of("ok", true, "payments", list);
    }


    @PostMapping("/api/bills/check")
    public ResponseEntity<?> checkBill(@RequestBody BillCheckRequest request) {

        Bill bill = billRepository.findByBillNoAndBiller(
                request.getBillNo(),
                request.getBiller()
        );

        if (bill != null) {
            return ResponseEntity.ok(Map.of("exists", true));
        } else {
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }
}


