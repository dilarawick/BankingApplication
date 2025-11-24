package com.bankapp.bankapp_backend.repository;

import com.bankapp.bankapp_backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByCustomerIDOrderByCreatedDateDesc(Integer customerId);
}
