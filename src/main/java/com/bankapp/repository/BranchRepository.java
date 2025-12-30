package com.bankapp.repository;

import com.bankapp.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Integer> {
    Optional<Branch> findByCity(String city);
}
