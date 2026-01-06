package com.bankapp.controller;

import com.bankapp.model.Bill;
import com.bankapp.service.BillService;
import com.bankapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    @Autowired
    private AuthService authService;

    // Get unpaid bills for the authenticated customer
    @GetMapping("/unpaid")
    public ResponseEntity<?> getUnpaidBills(
            @RequestParam String category,
            @RequestParam String biller,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            List<Bill> unpaidBills = billService.getUnpaidBillsForCustomer(customerId, category, biller);

            // Convert bills to the format expected by the frontend
            List<Map<String, Object>> billsList = unpaidBills.stream().map(bill -> {
                Map<String, Object> billMap = new HashMap<>();
                billMap.put("id", bill.getBillId());
                billMap.put("reference", bill.getReference());
                billMap.put("invoice", bill.getInvoiceNumber());
                billMap.put("dueDate", bill.getDueDate() != null ? bill.getDueDate().toString() : null);
                billMap.put("amount", bill.getAmount());
                return billMap;
            }).toList();

            return ResponseEntity.ok(billsList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get unpaid bills: " + e.getMessage()));
        }
    }

    // Pay a bill
    @PostMapping("/pay")
    public ResponseEntity<?> payBill(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            String accountNumber = (String) request.get("accountNumber");
            Integer billId = Integer.valueOf(request.get("billId").toString());
            Double amount = Double.valueOf(request.get("amount").toString());

            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Account number is required"));
            }

            if (billId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bill ID is required"));
            }

            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Valid amount is required"));
            }

            // Verify that the customer owns the account
            boolean accountBelongsToCustomer = authService.getCustomerAccounts(customerId)
                    .stream()
                    .anyMatch(ca -> accountNumber.equals(ca.getAccountNo()));
            if (!accountBelongsToCustomer) {
                return ResponseEntity.status(403).body(Map.of("message", "Account does not belong to you"));
            }

            // Attempt to pay the bill
            boolean success = billService.payBill(billId, accountNumber, amount);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Bill paid successfully",
                        "status", "success"));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Failed to pay bill. Please check if bill exists and account has sufficient funds.",
                        "status", "failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to pay bill: " + e.getMessage()));
        }
    }

    // Get all bills for the authenticated customer
    @GetMapping("/all")
    public ResponseEntity<?> getAllBills(Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            List<Bill> bills = billService.getBillsForCustomer(customerId);

            // Convert bills to the format expected by the frontend
            List<Map<String, Object>> billsList = bills.stream().map(bill -> {
                Map<String, Object> billMap = new HashMap<>();
                billMap.put("id", bill.getBillId());
                billMap.put("billerName", bill.getBillerName());
                billMap.put("category", bill.getCategory());
                billMap.put("reference", bill.getReference());
                billMap.put("invoice", bill.getInvoiceNumber());
                billMap.put("dueDate", bill.getDueDate() != null ? bill.getDueDate().toString() : null);
                billMap.put("amount", bill.getAmount());
                billMap.put("status", bill.getBillStatus().toString());
                billMap.put("createdDate", bill.getCreatedDate());
                billMap.put("paidDate", bill.getPaidDate());
                return billMap;
            }).toList();

            return ResponseEntity.ok(billsList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get bills: " + e.getMessage()));
        }
    }

    // Create a new bill (for admin or internal use)
    @PostMapping("/create")
    public ResponseEntity<?> createBill(@RequestBody Bill bill, Authentication authentication) {
        try {
            // Validate required fields
            if (bill.getBillerName() == null || bill.getBillerName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Biller name is required"));
            }

            if (bill.getCategory() == null || bill.getCategory().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Category is required"));
            }

            if (bill.getReference() == null || bill.getReference().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Reference is required"));
            }

            if (bill.getAmount() == null || bill.getAmount() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Valid amount is required"));
            }

            // Set customer ID from authentication
            Integer customerId = (Integer) authentication.getPrincipal();
            bill.setCustomerID(customerId);

            Bill createdBill = billService.createBill(bill);

            return ResponseEntity.ok(createdBill);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to create bill: " + e.getMessage()));
        }
    }
}