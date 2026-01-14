package com.bankapp.controller;

import com.bankapp.dto.EStatementDTO;
import com.bankapp.dto.EStatementEmailRequest;
import com.bankapp.service.EStatementService;
import com.bankapp.service.EmailService;
import com.bankapp.model.Customer;
import com.bankapp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import com.bankapp.repository.CustomerAccountRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/estatement")
public class EStatementController {

    @Autowired
    private EStatementService eStatementService;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/account/{accountNo}")
    public ResponseEntity<?> getEStatement(
            @PathVariable String accountNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {

        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            if (customerId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Authentication failed"));
            }

            // Validate that the account belongs to the authenticated user
            List<com.bankapp.model.CustomerAccount> customerAccounts = customerAccountRepository
                    .findByCustomerIDAndAccountNo(customerId, accountNo);

            if (customerAccounts == null || customerAccounts.isEmpty()) {
                return ResponseEntity.status(403).body(Map.of("message", "Account doesn't belong to user"));
            }

            // Parse dates or use defaults (last 30 days)
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            EStatementDTO statement = eStatementService.generateEStatement(accountNo, start, end);

            return ResponseEntity.ok(statement);
        } catch (Exception e) {
            System.err.println("Error getting e-statement: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to retrieve e-statement: " + e.getMessage()));
        }
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEStatementByEmail(@RequestBody EStatementEmailRequest request,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            String accountNo = request.getAccountNo();

            // Validate that the account belongs to the authenticated user
            List<com.bankapp.model.CustomerAccount> customerAccounts = customerAccountRepository
                    .findByCustomerIDAndAccountNo(customerId, accountNo);

            if (customerAccounts == null || customerAccounts.isEmpty()) {
                return ResponseEntity.status(403).body(Map.of("message", "Account doesn't belong to user"));
            }

            // Get customer details to get email address
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Customer not found"));
            }

            Customer customer = customerOpt.get();

            // Create HTML email content
            String emailContent = generateStatementEmailContent(request.getStatement());

            // Send the email
            emailService.sendEmail(customer.getEmail(), "Nova Bank - E-Statement for Account " + accountNo,
                    emailContent);

            return ResponseEntity.ok(Map.of("message", "E-statement sent successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to send e-statement: " + e.getMessage()));
        }
    }

    private String generateStatementEmailContent(EStatementDTO statement) {
        StringBuilder html = new StringBuilder();
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>\n");
        html.append("<title>Nova Bank - E-Statement</title>\n");
        html.append("</head>\n");
        html.append("<body style='font-family: Arial, sans-serif; font-size: 14px;'>\n");
        html.append(
                "<h2 style='color: #000F2E; text-align: center; margin: 0 0 10px 0;'>Nova Bank - E-Statement</h2>\n");

        // Account information
        html.append(
                "<div style='margin: 10px 0; padding: 10px; border: 1px solid #eee; background-color: #f9f9f9;'>\n");
        html.append("<p style='margin: 5px 0;'><strong>Account Holder:</strong> ").append(statement.getAccountHolder())
                .append("</p>\n");
        html.append("<p style='margin: 5px 0;'><strong>Account Number:</strong> ").append(statement.getAccountNumber())
                .append("</p>\n");
        html.append("<p style='margin: 5px 0;'><strong>Account Type:</strong> ").append(statement.getAccountType())
                .append("</p>\n");
        html.append("<p style='margin: 5px 0;'><strong>Branch:</strong> ").append(statement.getBranch())
                .append("</p>\n");
        html.append("<p style='margin: 5px 0;'><strong>Statement Period:</strong> ")
                .append(statement.getStatementPeriod()).append("</p>\n");
        html.append("</div>\n");

        // Account summary
        html.append("<div style='margin: 10px 0; padding: 10px; border: 1px solid #eee;'>\n");
        html.append("<h4 style='margin: 5px 0; color: #000F2E;'>Account Summary</h4>\n");
        html.append("<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>\n");
        html.append("<tr style='background-color: #f5f5f5;'>\n");
        html.append("<th style='border: 1px solid #ddd; padding: 6px; text-align: left;'>Description</th>\n");
        html.append("<th style='border: 1px solid #ddd; padding: 6px; text-align: right;'>Amount (LKR)</th>\n");
        html.append("</tr>\n");
        html.append("<tr>\n");
        html.append("<td style='border: 1px solid #ddd; padding: 6px;'>Opening Balance</td>\n");
        html.append("<td style='border: 1px solid #ddd; padding: 6px; text-align: right;'>")
                .append(String.format("%,.2f", statement.getAccountSummary().getOpeningBalance())).append("</td>\n");
        html.append("</tr>\n");
        html.append("<tr>\n");
        html.append("<td style='border: 1px solid #ddd; padding: 6px;'>Total Credits</td>\n");
        html.append("<td style='border: 1px solid #ddd; padding: 6px; text-align: right;'>")
                .append(String.format("%,.2f", statement.getAccountSummary().getTotalCredits())).append("</td>\n");
        html.append("</tr>\n");
        html.append("<tr>\n");
        html.append("<td style='border: 1px solid #ddd; padding: 6px;'>Total Debits</td>\n");
        html.append("<td style='border: 1px solid #ddd; padding: 6px; text-align: right;'>")
                .append(String.format("%,.2f", statement.getAccountSummary().getTotalDebits())).append("</td>\n");
        html.append("</tr>\n");
        html.append("<tr>\n");
        html.append("<td style='border: 1px solid #ddd; padding: 6px; font-weight: bold;'>Closing Balance</td>\n");
        html.append("<td style='border: 1px solid #ddd; padding: 6px; text-align: right; font-weight: bold;'>")
                .append(String.format("%,.2f", statement.getAccountSummary().getClosingBalance())).append("</td>\n");
        html.append("</tr>\n");
        html.append("</table>\n");
        html.append("</div>\n");

        // Limit transactions to first 10 to reduce email size
        int maxTransactions = Math.min(statement.getTransactionDetails().size(), 10);
        html.append("<div style='margin: 10px 0; padding: 10px; border: 1px solid #eee;'>\n");
        html.append("<h4 style='margin: 5px 0; color: #000F2E;'>Recent Transactions</h4>\n");
        html.append("<table style='width: 100%; border-collapse: collapse; font-size: 12px;'>\n");
        html.append("<tr style='background-color: #f5f5f5;'>\n");
        html.append("<th style='border: 1px solid #ddd; padding: 5px; text-align: left;'>Date</th>\n");
        html.append("<th style='border: 1px solid #ddd; padding: 5px; text-align: left;'>ID</th>\n");
        html.append("<th style='border: 1px solid #ddd; padding: 5px; text-align: left;'>Description</th>\n");
        html.append("<th style='border: 1px solid #ddd; padding: 5px; text-align: left;'>Type</th>\n");
        html.append("<th style='border: 1px solid #ddd; padding: 5px; text-align: right;'>Amount (LKR)</th>\n");
        html.append("<th style='border: 1px solid #ddd; padding: 5px; text-align: right;'>Balance (LKR)</th>\n");
        html.append("</tr>\n");

        for (int i = 0; i < maxTransactions; i++) {
            EStatementDTO.TransactionDetail transaction = statement.getTransactionDetails().get(i);
            html.append("<tr>\n");
            html.append("<td style='border: 1px solid #ddd; padding: 5px;'>").append(transaction.getDate())
                    .append("</td>\n");
            html.append("<td style='border: 1px solid #ddd; padding: 5px;'>").append(transaction.getTransactionId())
                    .append("</td>\n");
            html.append("<td style='border: 1px solid #ddd; padding: 5px;'>").append(transaction.getDescription())
                    .append("</td>\n");
            html.append("<td style='border: 1px solid #ddd; padding: 5px;'>").append(transaction.getType())
                    .append("</td>\n");
            html.append("<td style='border: 1px solid #ddd; padding: 5px; text-align: right;'>")
                    .append(String.format("%,.2f", transaction.getAmount())).append("</td>\n");
            html.append("<td style='border: 1px solid #ddd; padding: 5px; text-align: right;'>")
                    .append(String.format("%,.2f", transaction.getBalance())).append("</td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");

        // Add note if there are more transactions
        if (statement.getTransactionDetails().size() > 10) {
            html.append("<p style='margin-top: 8px; font-style: italic; color: #666;'>Showing first 10 of ")
                    .append(statement.getTransactionDetails().size()).append(" transactions</p>\n");
        }

        html.append("</div>\n");

        html.append(
                "<div style='margin: 15px 0; padding: 10px; background-color: #f9f9f9; border-left: 3px solid #000F2E; font-size: 12px;'>\n");
        html.append(
                "<p style='margin: 5px 0;'><strong>Disclaimer:</strong> For any discrepancies, please contact Nova Bank at +94 11 2345678 or support@novabank.lk</p>\n");
        html.append(
                "<p style='margin: 5px 0;'>This e-statement is electronically generated and does not require a signature.</p>\n");
        html.append("</div>\n");

        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }
}