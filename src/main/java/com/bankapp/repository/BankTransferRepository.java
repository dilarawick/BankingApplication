package com.bankapp.repository;

import com.bankapp.model.BankTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BankTransferRepository extends JpaRepository<BankTransfer, Integer> {
    List<BankTransfer> findBySenderCustomerId(Integer customerId);

    List<BankTransfer> findBySenderAccountAccountNo(String accountNo);

    List<BankTransfer> findByTransferStatus(String transferStatus);

    List<BankTransfer> findBySenderCustomerIdAndTransferStatus(Integer customerId, String transferStatus);
}