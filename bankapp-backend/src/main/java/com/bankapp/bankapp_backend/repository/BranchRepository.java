package com.bankapp.bankapp_backend.repository;

import com.bankapp.bankapp_backend.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Integer> {
}
