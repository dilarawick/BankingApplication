package com.bankapp.service;

import com.bankapp.model.Budget;
import com.bankapp.model.BudgetTransaction;
import com.bankapp.model.Account;
import com.bankapp.repository.BudgetRepository;
import com.bankapp.repository.BudgetTransactionRepository;
import com.bankapp.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SmartSpendService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetTransactionRepository budgetTransactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    // Get active budget for a customer and account
    public Optional<Budget> getActiveBudget(Integer customerId, String accountNo) {
        return budgetRepository.findActiveBudgetByCustomerAndAccount(customerId, accountNo);
    }

    // Get all budgets for a customer
    public List<Budget> getBudgetsForCustomer(Integer customerId) {
        return budgetRepository.findByCustomerIDAndIsActiveTrue(customerId);
    }

    // Create or update a budget
    @Transactional
    public Budget createOrUpdateBudget(Integer customerId, String accountNo, BigDecimal budgetLimit,
            LocalDate startDate, LocalDate endDate) {
        // Validate that the budget doesn't exceed the account balance
        Optional<Account> accountOpt = accountRepository.findByAccountNo(accountNo);
        if (!accountOpt.isPresent()) {
            throw new IllegalArgumentException("Account not found");
        }

        Account account = accountOpt.get();
        BigDecimal accountBalance = BigDecimal.valueOf(account.getAccountBalance());
        if (budgetLimit.compareTo(accountBalance) > 0) {
            throw new IllegalArgumentException("Budget limit cannot exceed account balance");
        }

        // Check if there's an existing active budget for this account
        Optional<Budget> existingBudgetOpt = budgetRepository.findActiveBudgetByCustomerAndAccount(customerId,
                accountNo);

        Budget budget;
        if (existingBudgetOpt.isPresent()) {
            // Update existing budget
            budget = existingBudgetOpt.get();
            budget.setBudgetLimit(budgetLimit);
            budget.setStartDate(startDate);
            budget.setEndDate(endDate);
            budget.setUpdatedDate(LocalDateTime.now());
        } else {
            // Create new budget
            budget = new Budget();
            budget.setCustomerID(customerId);
            budget.setAccountNo(accountNo);
            budget.setBudgetLimit(budgetLimit);
            budget.setStartDate(startDate);
            budget.setEndDate(endDate);
            budget.setIsActive(true);
        }

        return budgetRepository.save(budget);
    }

    // Record a transaction against the budget
    @Transactional
    public BudgetTransaction recordTransaction(Integer budgetId, BigDecimal amount, String description) {
        // Verify the budget exists and is active
        Optional<Budget> budgetOpt = budgetRepository.findByBudgetIdAndIsActiveTrue(budgetId);
        if (!budgetOpt.isPresent()) {
            throw new IllegalArgumentException("Budget not found or inactive");
        }

        // Create the budget transaction
        BudgetTransaction transaction = new BudgetTransaction();
        transaction.setBudgetId(budgetId);
        transaction.setTransactionAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionType("AUTOMATIC"); // Default to automatic for bank transfers and bill payments
        transaction.setTransactionDate(LocalDateTime.now());

        return budgetTransactionRepository.save(transaction);
    }

    // Calculate total spending for a budget within its date range
    public BigDecimal getTotalSpentForBudget(Integer budgetId) {
        Optional<Budget> budgetOpt = budgetRepository.findByBudgetIdAndIsActiveTrue(budgetId);
        if (!budgetOpt.isPresent()) {
            return BigDecimal.ZERO;
        }

        Budget budget = budgetOpt.get();
        LocalDateTime startDate = budget.getStartDate().atStartOfDay();
        LocalDateTime endDate = budget.getEndDate().atTime(23, 59, 59);

        Double totalSpent = budgetTransactionRepository.calculateTotalSpendingByBudgetIdAndDateRange(
                budgetId, startDate, endDate);

        return totalSpent != null ? BigDecimal.valueOf(totalSpent) : BigDecimal.ZERO;
    }

    // Calculate percentage of budget used
    public double getBudgetPercentageUsed(Integer budgetId) {
        Optional<Budget> budgetOpt = budgetRepository.findByBudgetIdAndIsActiveTrue(budgetId);
        if (!budgetOpt.isPresent()) {
            return 0.0;
        }

        Budget budget = budgetOpt.get();
        BigDecimal totalSpent = getTotalSpentForBudget(budgetId);
        BigDecimal budgetLimit = budget.getBudgetLimit();

        if (budgetLimit.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        double percentage = totalSpent.multiply(BigDecimal.valueOf(100))
                .divide(budgetLimit, 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();

        // Cap the percentage at 100% to prevent display of values over 100%
        return Math.min(percentage, 100.0);
    }

    // Get automatic transactions for a budget (bank transfers and bill payments)
    public List<BudgetTransaction> getTransactionsForBudget(Integer budgetId) {
        return budgetTransactionRepository.findByBudgetIdAndTransactionTypeOrderByTransactionDateDesc(budgetId,
                "AUTOMATIC");
    }

    // Get all transactions for a budget (both automatic and manual)
    public List<BudgetTransaction> getAllTransactionsForBudget(Integer budgetId) {
        return budgetTransactionRepository.findByBudgetIdOrderByTransactionDateDesc(budgetId);
    }

    // Deactivate a budget (effectively deleting it)
    @Transactional
    public boolean deactivateBudget(Integer budgetId, Integer customerId) {
        Optional<Budget> budgetOpt = budgetRepository.findByBudgetIdAndIsActiveTrue(budgetId);
        if (!budgetOpt.isPresent()) {
            return false;
        }

        Budget budget = budgetOpt.get();
        // Verify that the budget belongs to the customer
        if (!budget.getCustomerID().equals(customerId)) {
            return false;
        }

        budget.setIsActive(false);
        budgetRepository.save(budget);
        return true;
    }

    // Check if a budget has reached certain percentage thresholds
    public BudgetAlertStatus checkBudgetAlertStatus(Integer budgetId) {
        // Get the actual percentage without capping to determine proper alert status
        Optional<Budget> budgetOpt = budgetRepository.findByBudgetIdAndIsActiveTrue(budgetId);
        if (!budgetOpt.isPresent()) {
            return BudgetAlertStatus.INITIAL;
        }

        Budget budget = budgetOpt.get();
        BigDecimal totalSpent = getTotalSpentForBudget(budgetId);
        BigDecimal budgetLimit = budget.getBudgetLimit();

        if (budgetLimit.compareTo(BigDecimal.ZERO) == 0) {
            return BudgetAlertStatus.INITIAL;
        }

        double actualPercentage = totalSpent.multiply(BigDecimal.valueOf(100))
                .divide(budgetLimit, 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();

        if (actualPercentage >= 100.0) {
            return BudgetAlertStatus.EXHAUSTED;
        } else if (actualPercentage >= 80.0) {
            return BudgetAlertStatus.WARNING;
        } else if (actualPercentage > 0.0) {
            return BudgetAlertStatus.ACTIVE;
        } else {
            return BudgetAlertStatus.INITIAL;
        }
    }

    // Enum for budget alert status
    public enum BudgetAlertStatus {
        INITIAL, // 0% - Budget set but no spending yet
        ACTIVE, // 1-79% - Normal usage
        WARNING, // 80-99% - Warning threshold reached
        EXHAUSTED // 100% - Budget completely used
    }

    // Method to automatically record a debit transaction against a budget when a
    // transfer occurs
    @Transactional
    public Optional<BudgetTransaction> recordDebitAgainstBudget(String accountNo, BigDecimal amount,
            String description) {
        // Find the customer ID associated with this account
        Optional<Account> accountOpt = accountRepository.findByAccountNo(accountNo);
        if (!accountOpt.isPresent()) {
            return Optional.empty();
        }

        Integer customerId = accountOpt.get().getCustomerID();

        // Find active budget for this customer and account
        Optional<Budget> budgetOpt = budgetRepository.findActiveBudgetByCustomerAndAccount(customerId, accountNo);
        if (!budgetOpt.isPresent()) {
            return Optional.empty(); // No active budget for this account
        }

        Budget budget = budgetOpt.get();

        // Check if the debit is within the budget period
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(budget.getStartDate()) || currentDate.isAfter(budget.getEndDate())) {
            return Optional.empty(); // Current date is outside budget period
        }

        // Record the transaction against the budget
        BudgetTransaction transaction = recordTransaction(budget.getBudgetId(), amount, description);

        // Check for budget alerts after the transaction
        BudgetAlertStatus alertStatus = checkBudgetAlertStatus(budget.getBudgetId());

        // For now, just log the alert status - in a real system, you might want to send
        // notifications
        System.out.println("Budget alert triggered: " + alertStatus + " for budget " + budget.getBudgetId() +
                " (" + getBudgetPercentageUsed(budget.getBudgetId()) + "% used)");

        return Optional.of(transaction);
    }

    // Record a manual expense transaction against the budget with category and
    // payment type
    @Transactional
    public BudgetTransaction recordManualExpense(Integer budgetId, BigDecimal amount, String description,
            String category, String paymentType) {
        // Verify the budget exists and is active
        Optional<Budget> budgetOpt = budgetRepository.findByBudgetIdAndIsActiveTrue(budgetId);
        if (!budgetOpt.isPresent()) {
            throw new IllegalArgumentException("Budget not found or inactive");
        }

        // Create the budget transaction
        BudgetTransaction transaction = new BudgetTransaction();
        transaction.setBudgetId(budgetId);
        transaction.setTransactionAmount(amount);
        transaction.setDescription(description);
        transaction.setCategory(category);
        transaction.setPaymentType(paymentType);
        transaction.setTransactionType("MANUAL");
        transaction.setTransactionDate(LocalDateTime.now());

        return budgetTransactionRepository.save(transaction);
    }

    // Calculate total spending for a specific category within the budget period
    public BigDecimal getTotalSpentForCategory(Integer budgetId, String category) {
        Optional<Budget> budgetOpt = budgetRepository.findByBudgetIdAndIsActiveTrue(budgetId);
        if (!budgetOpt.isPresent()) {
            return BigDecimal.ZERO;
        }

        Budget budget = budgetOpt.get();
        LocalDateTime startDate = budget.getStartDate().atStartOfDay();
        LocalDateTime endDate = budget.getEndDate().atTime(23, 59, 59);

        Double totalSpent = budgetTransactionRepository.calculateTotalSpendingByBudgetIdAndCategoryAndDateRange(
                budgetId, category, startDate, endDate);

        return totalSpent != null ? BigDecimal.valueOf(totalSpent) : BigDecimal.ZERO;
    }

    // Get all transactions for a specific category
    public List<BudgetTransaction> getTransactionsForCategory(Integer budgetId, String category) {
        return budgetTransactionRepository.findByBudgetIdAndCategory(budgetId, category);
    }

    // Get all transactions for a specific budget grouped by category
    public List<BudgetTransaction> getTransactionsByCategory(Integer budgetId, String category, LocalDate startDate,
            LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return budgetTransactionRepository.findByBudgetIdAndCategoryAndTransactionDateBetween(
                budgetId, category, startDateTime, endDateTime);
    }
}