package com.bankapp.repository;

import com.bankapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountNo(String accountNo);
    List<Account> findByCustomerID(Integer customerId);
}
