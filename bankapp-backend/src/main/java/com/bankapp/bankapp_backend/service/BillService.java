package com.bankapp.bankapp_backend.service;

import com.bankapp.bankapp_backend.model.Bill;
import com.bankapp.bankapp_backend.repository.BillRepository;
import org.springframework.stereotype.Service;

@Service
public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill findByBillNoAndBiller(String billNo, String biller) {
        return billRepository.findByBillNoAndBiller(billNo, biller);
    }
}
