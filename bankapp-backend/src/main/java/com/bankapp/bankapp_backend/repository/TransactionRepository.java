package com.bankapp.bankapp_backend.repository;

import com.bankapp.bankapp_backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTop5BySenderAccountNoOrderByTransactionDateDesc(String firstAcc);
}
