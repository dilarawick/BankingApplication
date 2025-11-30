package com.bankapp.bankapp_backend.repository;

import com.bankapp.bankapp_backend.model.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Integer> {
    List<CustomerAccount> findByCustomerID(Integer customerID);
    List<CustomerAccount> findByAccountNo(String accountNo);

    boolean existsByAccountNoAndCustomerID(String accountNo, Integer customerID);

    void deleteByAccountNoAndCustomerID(String accountNo, Integer customerID);

    CustomerAccount findByAccountNoAndCustomerID(String accountNo, Integer customerID);

}



