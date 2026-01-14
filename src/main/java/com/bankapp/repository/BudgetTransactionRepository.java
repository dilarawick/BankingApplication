package com.bankapp.repository;

import com.bankapp.model.BudgetTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BudgetTransactionRepository extends JpaRepository<BudgetTransaction, Integer> {

    // Find all transactions for a specific budget
    List<BudgetTransaction> findByBudgetId(Integer budgetId);

    // Find transactions for a specific budget within a date range
    @Query("SELECT bt FROM BudgetTransaction bt WHERE bt.budgetId = :budgetId AND bt.transactionDate BETWEEN :startDate AND :endDate ORDER BY bt.transactionDate DESC")
    List<BudgetTransaction> findByBudgetIdAndTransactionDateBetween(
            @Param("budgetId") Integer budgetId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Calculate total spending for a specific budget within a date range
    @Query("SELECT COALESCE(SUM(bt.transactionAmount), 0) FROM BudgetTransaction bt WHERE bt.budgetId = :budgetId AND bt.transactionDate BETWEEN :startDate AND :endDate")
    Double calculateTotalSpendingByBudgetIdAndDateRange(
            @Param("budgetId") Integer budgetId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find recent transactions for a budget (most recent first)
    List<BudgetTransaction> findByBudgetIdOrderByTransactionDateDesc(Integer budgetId);

    // Calculate total spending for a specific budget and category within a date
    // range
    @Query("SELECT COALESCE(SUM(bt.transactionAmount), 0) FROM BudgetTransaction bt WHERE bt.budgetId = :budgetId AND bt.category = :category AND bt.transactionDate BETWEEN :startDate AND :endDate")
    Double calculateTotalSpendingByBudgetIdAndCategoryAndDateRange(
            @Param("budgetId") Integer budgetId,
            @Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find transactions for a specific budget by category
    List<BudgetTransaction> findByBudgetIdAndCategory(Integer budgetId, String category);

    // Find transactions for a specific budget by category within a date range
    List<BudgetTransaction> findByBudgetIdAndCategoryAndTransactionDateBetween(
            Integer budgetId, String category, LocalDateTime startDate, LocalDateTime endDate);

    // Find automatic transactions for a specific budget
    List<BudgetTransaction> findByBudgetIdAndTransactionType(Integer budgetId, String transactionType);

    // Find automatic transactions for a specific budget ordered by date
    List<BudgetTransaction> findByBudgetIdAndTransactionTypeOrderByTransactionDateDesc(Integer budgetId,
            String transactionType);
}