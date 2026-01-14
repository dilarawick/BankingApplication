package com.bankapp.repository;

import com.bankapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    /**
     * Find all transactions for a specific account
     */
    List<Transaction> findByAccountNo(String accountNo);

    /**
     * Find all transactions for a specific account ordered by date
     */
    List<Transaction> findByAccountNoOrderByTransactionDateDesc(String accountNo);

    /**
     * Find all transactions for a specific account and transaction type
     */
    List<Transaction> findByAccountNoAndTransactionType(String accountNo, String transactionType);

    /**
     * Find all transactions by reference ID and type
     */
    List<Transaction> findByReferenceIdAndReferenceType(Integer referenceId, String referenceType);

    /**
     * Find all transactions for a specific account within a date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountNo = :accountNo AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountNoAndTransactionDateBetween(@Param("accountNo") String accountNo,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}