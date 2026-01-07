package com.bankapp.repository;

import com.bankapp.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    // Find active budget for a specific customer and account
    @Query("SELECT b FROM Budget b WHERE b.customerID = :customerId AND b.accountNo = :accountNo AND b.isActive = true")
    Optional<Budget> findActiveBudgetByCustomerAndAccount(
            @Param("customerId") Integer customerId,
            @Param("accountNo") String accountNo);

    // Find all budgets for a specific customer
    List<Budget> findByCustomerID(Integer customerId);

    // Find all active budgets for a specific customer
    List<Budget> findByCustomerIDAndIsActiveTrue(Integer customerId);

    // Find budgets by account number
    List<Budget> findByAccountNo(String accountNo);

    // Find active budget by budget ID
    Optional<Budget> findByBudgetIdAndIsActiveTrue(Integer budgetId);
}