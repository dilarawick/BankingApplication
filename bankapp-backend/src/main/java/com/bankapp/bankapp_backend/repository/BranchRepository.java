package com.bankapp.bankapp_backend.repository;

import com.bankapp.bankapp_backend.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Integer> {
    Optional<Branch> findByCity(String city);
}
