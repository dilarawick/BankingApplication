package com.bankapp.repository;

import com.bankapp.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByNic(String nic);
    Optional<Customer> findByNameAndNic(String name, String nic);
}
