package com.bankapp.bankapp_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bankapp.bankapp_backend.model.*;
import com.bankapp.bankapp_backend.repository.*;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired private AccountRepository accountRepo;
    @Autowired private BillRepository billRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private CustomerAccountRepository customerAccountRepo;

    // Validate bill exists and belongs to chosen biller (and optionally customer)
    public Optional<Bill> validateBill(String billNo, String biller) {
        Optional<Bill> b = billRepo.findById(billNo);
        if (b.isEmpty()) return Optional.empty();
        if (!b.get().getBiller().equalsIgnoreCase(biller)) return Optional.empty();
        return b;
    }

    // Process payment: check balance, deduct, mark Bill / save Payment
    public Payment processPayment(Integer customerId, String fromAccountNo, String billNo, String biller, Double amount) {
        Payment p = new Payment();
        p.setCustomerID(customerId);
        p.setFromAccountNo(fromAccountNo);
        p.setBillNo(billNo);
        p.setBiller(biller);
        p.setAmount(amount);
        p.setConfirmNo(UUID.randomUUID().toString().substring(0,8).toUpperCase());

        Optional<Account> accOpt = accountRepo.findById(fromAccountNo);
        if (accOpt.isEmpty()) {
            p.setStatus("Failed");
            p.setFailureReason("Source account not found");
            return paymentRepo.save(p);
        }
        Account acc = accOpt.get();
        if (acc.getAccountBalance() < amount) {
            p.setStatus("Failed");
            p.setFailureReason("Insufficient balance");
            return paymentRepo.save(p);
        }

        // deduct
        acc.setAccountBalance(acc.getAccountBalance() - amount);
        accountRepo.save(acc);

        // mark bill paid (if exists)
        Optional<Bill> billOpt = billRepo.findById(billNo);
        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            bill.setStatus("Paid");
            billRepo.save(bill);
        }

        p.setStatus("Success");
        p.setFailureReason(null);
        return paymentRepo.save(p);
    }
}
