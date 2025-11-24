package com.bankapp.bankapp_backend.controller;

import com.bankapp.bankapp_backend.model.Bill;
import com.bankapp.bankapp_backend.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    // Request body class for bill lookup
    public static class BillCheckRequest {
        private String billNo;
        private String biller;

        public String getBillNo() { return billNo; }
        public void setBillNo(String billNo) { this.billNo = billNo; }

        public String getBiller() { return biller; }
        public void setBiller(String biller) { this.biller = biller; }
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkBill(@RequestBody BillCheckRequest request) {

        Bill bill = billService.findByBillNoAndBiller(request.getBillNo(), request.getBiller());

        boolean exists = (bill != null);

        return ResponseEntity.ok(Map.of(
                "exists", exists
        ));
    }
}
