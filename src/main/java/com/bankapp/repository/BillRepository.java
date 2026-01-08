package com.bankapp.repository;

import com.bankapp.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Integer> {

    // Find unpaid bills for a specific customer
    @Query("SELECT b FROM Bill b WHERE b.customerID = :customerId AND b.billStatus = 'PENDING'")
    List<Bill> findUnpaidBillsByCustomer(@Param("customerId") Integer customerId);

    // Find unpaid bills by customer, category, and biller
    @Query("SELECT b FROM Bill b WHERE b.customerID = :customerId AND b.billStatus = 'PENDING' AND b.category = :category AND b.billerName = :billerName")
    List<Bill> findUnpaidBillsByCustomerAndCategoryAndBiller(
            @Param("customerId") Integer customerId,
            @Param("category") String category,
            @Param("billerName") String billerName);

    // Find bills by account number
    List<Bill> findByAccountNo(String accountNo);

    // Find bills by customer ID
    List<Bill> findByCustomerID(Integer customerID);

    // Find bills by account number and paid date range
    @Query("SELECT b FROM Bill b WHERE b.accountNo = :accountNo AND b.paidDate BETWEEN :startDate AND :endDate")
    List<Bill> findByAccountNoAndPaidDateBetween(@Param("accountNo") String accountNo,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}