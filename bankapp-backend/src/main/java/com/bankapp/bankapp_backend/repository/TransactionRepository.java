package com.bankapp.bankapp_backend.repository;

import com.bankapp.bankapp_backend.entity.Transaction;
import com.bankapp.bankapp_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findBySenderOrderByTransactionDateDesc(User sender);

    List<Transaction> findBySenderAndStatusOrderByTransactionDateDesc(User sender, String status);
}
