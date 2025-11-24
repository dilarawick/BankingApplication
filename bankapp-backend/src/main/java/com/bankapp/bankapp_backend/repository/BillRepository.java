package com.bankapp.bankapp_backend.repository;

import com.bankapp.bankapp_backend.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, String> {

    // Get all bills for a customer (optional)
    List<Bill> findByCustomerID(Integer customerId);

    // Get bills by biller
    List<Bill> findByBiller(String biller);

    // IMPORTANT: Used by your /api/bills/check endpoint
    Bill findByBillNoAndBiller(String billNo, String biller);
}
