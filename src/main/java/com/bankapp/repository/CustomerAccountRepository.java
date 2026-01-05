package com.bankapp.repository;

import com.bankapp.model.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Integer> {
    List<CustomerAccount> findByCustomerID(Integer customerID);

    List<CustomerAccount> findByAccountNo(String accountNo);

    List<CustomerAccount> findByCustomerIDAndAccountNo(Integer customerID, String accountNo);
}
