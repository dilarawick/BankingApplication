package com.bankapp.controller;

import com.bankapp.model.Budget;
import com.bankapp.model.BudgetTransaction;
import com.bankapp.service.SmartSpendService;
import com.bankapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/smartspend")
public class SmartSpendController {

    @Autowired
    private SmartSpendService smartSpendService;

    @Autowired
    private AuthService authService;

    // Get current budget for the selected account
    @GetMapping("/budget")
    public ResponseEntity<?> getCurrentBudget(@RequestParam(required = false) String accountNo,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            // If no account is specified, try to use the selected account from localStorage
            if (accountNo == null || accountNo.trim().isEmpty()) {
                // In a real scenario, we'd get this from the client, but for now we'll just get
                // any budget
                List<Budget> budgets = smartSpendService.getBudgetsForCustomer(customerId);
                if (budgets.isEmpty()) {
                    Map<String, Object> noBudgetResponse = new HashMap<>();
                    noBudgetResponse.put("hasBudget", false);
                    return ResponseEntity.ok(noBudgetResponse);
                }
                // Return the first active budget as default
                Budget budget = budgets.get(0);
                return ResponseEntity.ok(formatBudgetResponse(budget));
            }

            // Verify that the account belongs to the customer
            boolean accountBelongsToCustomer = authService.getCustomerAccounts(customerId)
                    .stream()
                    .anyMatch(ca -> accountNo.equals(ca.getAccountNo()));
            if (!accountBelongsToCustomer) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Account does not belong to you");
                return ResponseEntity.status(403).body(errorResponse);
            }

            Optional<Budget> budgetOpt = smartSpendService.getActiveBudget(customerId, accountNo);
            if (!budgetOpt.isPresent()) {
                Map<String, Object> noBudgetResponse = new HashMap<>();
                noBudgetResponse.put("hasBudget", false);
                return ResponseEntity.ok(noBudgetResponse);
            }

            return ResponseEntity.ok(formatBudgetResponse(budgetOpt.get()));
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get budget: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Create or update budget
    @PostMapping("/budget")
    public ResponseEntity<?> createOrUpdateBudget(@RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            String accountNo = (String) request.get("accountNo");
            Double budgetLimitDouble = Double.valueOf(request.get("budgetLimit").toString());
            BigDecimal budgetLimit = BigDecimal.valueOf(budgetLimitDouble);
            String startDateStr = (String) request.get("startDate");
            String endDateStr = (String) request.get("endDate");

            // Validate required fields
            if (accountNo == null || accountNo.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Account number is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (budgetLimit == null || budgetLimit.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Valid budget limit is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (startDateStr == null || endDateStr == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Start and end dates are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Parse dates
            LocalDate startDate, endDate;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                startDate = LocalDate.parse(startDateStr, formatter);
                endDate = LocalDate.parse(endDateStr, formatter);
            } catch (DateTimeParseException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid date format. Use YYYY-MM-DD");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Verify that the account belongs to the customer
            boolean accountBelongsToCustomer = authService.getCustomerAccounts(customerId)
                    .stream()
                    .anyMatch(ca -> accountNo.equals(ca.getAccountNo()));
            if (!accountBelongsToCustomer) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Account does not belong to you");
                return ResponseEntity.status(403).body(errorResponse);
            }

            // Create or update budget
            Budget budget = smartSpendService.createOrUpdateBudget(customerId, accountNo, budgetLimit, startDate,
                    endDate);

            return ResponseEntity.ok(formatBudgetResponse(budget));
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to create/update budget: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Get budget progress
    @GetMapping("/budget-progress")
    public ResponseEntity<?> getBudgetProgress(@RequestParam(required = false) String accountNo,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            // If an account number is provided, get the specific budget for that account
            if (accountNo != null && !accountNo.trim().isEmpty()) {
                // Verify account belongs to customer
                boolean accountBelongsToCustomer = authService.getCustomerAccounts(customerId)
                        .stream()
                        .anyMatch(ca -> accountNo.equals(ca.getAccountNo()));
                if (!accountBelongsToCustomer) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Account does not belong to you");
                    return ResponseEntity.status(403).body(errorResponse);
                }

                Optional<Budget> budgetOpt = smartSpendService.getActiveBudget(customerId, accountNo);
                if (!budgetOpt.isPresent()) {
                    Map<String, Object> noBudgetResponse = new HashMap<>();
                    noBudgetResponse.put("hasBudget", false);
                    return ResponseEntity.ok(noBudgetResponse);
                }

                Budget budget = budgetOpt.get();

                double percentageUsed = smartSpendService.getBudgetPercentageUsed(budget.getBudgetId());
                BigDecimal totalSpent = smartSpendService.getTotalSpentForBudget(budget.getBudgetId());
                BigDecimal remaining = budget.getBudgetLimit().subtract(totalSpent);
                SmartSpendService.BudgetAlertStatus alertStatus = smartSpendService
                        .checkBudgetAlertStatus(budget.getBudgetId());

                Map<String, Object> response = new HashMap<>();
                response.put("hasBudget", true);
                response.put("budgetId", budget.getBudgetId());
                response.put("budgetLimit", budget.getBudgetLimit());
                response.put("totalSpent", totalSpent);
                response.put("remaining", remaining);
                response.put("percentageUsed", percentageUsed);
                response.put("alertStatus", alertStatus.toString());
                response.put("accountNo", budget.getAccountNo());
                response.put("startDate", budget.getStartDate());
                response.put("endDate", budget.getEndDate());

                return ResponseEntity.ok(response);
            }

            // If no account is specified, try to get any active budget
            List<Budget> budgets = smartSpendService.getBudgetsForCustomer(customerId);
            if (budgets.isEmpty()) {
                Map<String, Object> noBudgetResponse = new HashMap<>();
                noBudgetResponse.put("hasBudget", false);
                return ResponseEntity.ok(noBudgetResponse);
            }

            // Use the first budget
            Budget budget = budgets.get(0);

            double percentageUsed = smartSpendService.getBudgetPercentageUsed(budget.getBudgetId());
            BigDecimal totalSpent = smartSpendService.getTotalSpentForBudget(budget.getBudgetId());
            BigDecimal remaining = budget.getBudgetLimit().subtract(totalSpent);
            SmartSpendService.BudgetAlertStatus alertStatus = smartSpendService
                    .checkBudgetAlertStatus(budget.getBudgetId());

            Map<String, Object> response = new HashMap<>();
            response.put("hasBudget", true);
            response.put("budgetId", budget.getBudgetId());
            response.put("budgetLimit", budget.getBudgetLimit());
            response.put("totalSpent", totalSpent);
            response.put("remaining", remaining);
            response.put("percentageUsed", percentageUsed);
            response.put("alertStatus", alertStatus.toString());
            response.put("accountNo", budget.getAccountNo());
            response.put("startDate", budget.getStartDate());
            response.put("endDate", budget.getEndDate());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get budget progress: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Get recent transactions for the budget
    @GetMapping("/transactions")
    public ResponseEntity<?> getRecentTransactions(@RequestParam(required = false) Integer budgetId,
            @RequestParam(required = false) String accountNo,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            // If an account number is provided, get the specific budget for that account
            if (accountNo != null && !accountNo.trim().isEmpty()) {
                Optional<Budget> budgetOpt = smartSpendService.getActiveBudget(customerId, accountNo);
                if (!budgetOpt.isPresent()) {
                    return ResponseEntity.ok(Collections.emptyList());
                }

                List<BudgetTransaction> transactions = smartSpendService
                        .getTransactionsForBudget(budgetOpt.get().getBudgetId());

                // Format transactions for response
                List<Map<String, Object>> formattedTransactions = transactions.stream().map(t -> {
                    Map<String, Object> tx = new HashMap<>();
                    tx.put("transactionId", t.getTransactionId());
                    tx.put("transactionAmount", t.getTransactionAmount());
                    tx.put("transactionDate", t.getTransactionDate());
                    tx.put("description", t.getDescription());
                    tx.put("transactionType", t.getTransactionType());
                    return tx;
                }).toList();

                return ResponseEntity.ok(formattedTransactions);
            }

            // If no account is specified but budgetId is provided
            if (budgetId != null) {
                List<Budget> budgets = smartSpendService.getBudgetsForCustomer(customerId);
                boolean budgetBelongsToCustomer = budgets.stream()
                        .anyMatch(b -> b.getBudgetId().equals(budgetId));
                if (!budgetBelongsToCustomer) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Budget does not belong to you");
                    return ResponseEntity.status(403).body(errorResponse);
                }

                List<BudgetTransaction> transactions = smartSpendService.getTransactionsForBudget(budgetId);

                // Format transactions for response
                List<Map<String, Object>> formattedTransactions = transactions.stream().map(t -> {
                    Map<String, Object> tx = new HashMap<>();
                    tx.put("transactionId", t.getTransactionId());
                    tx.put("transactionAmount", t.getTransactionAmount());
                    tx.put("transactionDate", t.getTransactionDate());
                    tx.put("description", t.getDescription());
                    tx.put("transactionType", t.getTransactionType());
                    return tx;
                }).toList();

                return ResponseEntity.ok(formattedTransactions);
            }

            // If neither account nor budgetId is specified, use the first budget
            List<Budget> budgets = smartSpendService.getBudgetsForCustomer(customerId);
            if (budgets.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<BudgetTransaction> transactions = smartSpendService
                    .getTransactionsForBudget(budgets.get(0).getBudgetId());

            // Format transactions for response
            List<Map<String, Object>> formattedTransactions = transactions.stream().map(t -> {
                Map<String, Object> tx = new HashMap<>();
                tx.put("transactionId", t.getTransactionId());
                tx.put("transactionAmount", t.getTransactionAmount());
                tx.put("transactionDate", t.getTransactionDate());
                tx.put("description", t.getDescription());
                tx.put("transactionType", t.getTransactionType());
                return tx;
            }).toList();

            return ResponseEntity.ok(formattedTransactions);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get transactions: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Helper method to format budget response
    private Map<String, Object> formatBudgetResponse(Budget budget) {
        double percentageUsed = smartSpendService.getBudgetPercentageUsed(budget.getBudgetId());
        BigDecimal totalSpent = smartSpendService.getTotalSpentForBudget(budget.getBudgetId());
        BigDecimal remaining = budget.getBudgetLimit().subtract(totalSpent);
        SmartSpendService.BudgetAlertStatus alertStatus = smartSpendService
                .checkBudgetAlertStatus(budget.getBudgetId());

        Map<String, Object> response = new HashMap<>();
        response.put("hasBudget", true);
        response.put("budgetId", budget.getBudgetId());
        response.put("accountNo", budget.getAccountNo());
        response.put("budgetLimit", budget.getBudgetLimit());
        response.put("totalSpent", totalSpent);
        response.put("remaining", remaining);
        response.put("percentageUsed", percentageUsed);
        response.put("alertStatus", alertStatus.toString());
        response.put("startDate", budget.getStartDate());
        response.put("endDate", budget.getEndDate());
        response.put("isActive", budget.getIsActive());
        response.put("createdDate", budget.getCreatedDate());

        return response;
    }

    // Add a manual expense
    @PostMapping("/manual-expense")
    public ResponseEntity<?> addManualExpense(@RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            // Get the budget for the customer
            List<Budget> budgets = smartSpendService.getBudgetsForCustomer(customerId);
            if (budgets.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "No active budget found. Please set up a budget first.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Use the first budget (in a real app, we might want to specify which budget)
            Budget budget = budgets.get(0);

            // Extract expense details
            Double amountDouble = Double.valueOf(request.get("amount").toString());
            BigDecimal amount = BigDecimal.valueOf(amountDouble);
            String description = (String) request.get("description");
            String category = (String) request.get("category");
            String paymentType = (String) request.getOrDefault("paymentType", "Cash");

            // Validate required fields
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Valid amount is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (category == null || category.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Category is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Record the manual expense
            BudgetTransaction transaction = smartSpendService.recordManualExpense(
                    budget.getBudgetId(), amount, description, category, paymentType);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Manual expense recorded successfully");
            response.put("transactionId", transaction.getTransactionId());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to add manual expense: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Get budget progress by category
    @GetMapping("/budget-progress-by-category")
    public ResponseEntity<?> getBudgetProgressByCategory(@RequestParam Integer budgetId,
            @RequestParam String category,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            // Verify that the budget belongs to the customer
            List<Budget> customerBudgets = smartSpendService.getBudgetsForCustomer(customerId);
            boolean budgetBelongsToCustomer = customerBudgets.stream()
                    .anyMatch(b -> b.getBudgetId().equals(budgetId));

            if (!budgetBelongsToCustomer) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Budget does not belong to you");
                return ResponseEntity.status(403).body(errorResponse);
            }

            // Get the budget details
            Budget budget = null;
            for (Budget b : customerBudgets) {
                if (b.getBudgetId().equals(budgetId)) {
                    budget = b;
                    break;
                }
            }

            if (budget == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Budget not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Calculate category-specific spending
            BigDecimal categorySpent = smartSpendService.getTotalSpentForCategory(budgetId, category);

            // Calculate category budget limit (we'll distribute evenly among categories for
            // now)
            // In a real implementation, you might want to store specific limits per
            // category
            BigDecimal categoryLimit = budget.getBudgetLimit().divide(BigDecimal.valueOf(4), BigDecimal.ROUND_HALF_UP); // Assuming
                                                                                                                        // 4
                                                                                                                        // categories
            BigDecimal remaining = categoryLimit.subtract(categorySpent);

            double percentageUsed = 0.0;
            if (categoryLimit.compareTo(BigDecimal.ZERO) > 0) {
                percentageUsed = categorySpent.multiply(BigDecimal.valueOf(100))
                        .divide(categoryLimit, 4, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("budgetId", budgetId);
            response.put("category", category);
            response.put("categoryLimit", categoryLimit);
            response.put("totalSpent", categorySpent);
            response.put("remaining", remaining);
            response.put("percentageUsed", percentageUsed);
            response.put("budgetPeriod", budget.getStartDate() + " to " + budget.getEndDate());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get category budget progress: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Get all manual expenses for a budget
    @GetMapping("/manual-expenses")
    public ResponseEntity<?> getManualExpenses(@RequestParam Integer budgetId,
            Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();

            // Verify that the budget belongs to the customer
            List<Budget> customerBudgets = smartSpendService.getBudgetsForCustomer(customerId);
            boolean budgetBelongsToCustomer = customerBudgets.stream()
                    .anyMatch(b -> b.getBudgetId().equals(budgetId));

            if (!budgetBelongsToCustomer) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Budget does not belong to you");
                return ResponseEntity.status(403).body(errorResponse);
            }

            List<BudgetTransaction> transactions = smartSpendService.getAllTransactionsForBudget(budgetId);

            // Filter to only manual expenses
            List<Map<String, Object>> manualExpenses = transactions.stream()
                    .filter(t -> "MANUAL".equals(t.getTransactionType()))
                    .map(t -> {
                        Map<String, Object> tx = new HashMap<>();
                        tx.put("transactionId", t.getTransactionId());
                        tx.put("transactionAmount", t.getTransactionAmount());
                        tx.put("transactionDate", t.getTransactionDate());
                        tx.put("description", t.getDescription());
                        tx.put("category", t.getCategory());
                        tx.put("paymentType", t.getPaymentType());
                        tx.put("transactionType", t.getTransactionType());
                        return tx;
                    }).toList();

            return ResponseEntity.ok(manualExpenses);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get manual expenses: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}