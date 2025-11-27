package com.bankapp.bankapp_backend.repository;

import com.bankapp.bankapp_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByAccountNumber(String accountNumber);

    boolean existsByUsername(String username);

    boolean existsByAccountNumber(String accountNumber);
}
