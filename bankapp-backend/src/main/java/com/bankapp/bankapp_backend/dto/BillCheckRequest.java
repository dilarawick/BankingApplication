package com.bankapp.bankapp_backend.dto;

public class BillCheckRequest {
    private String billNo;
    private String biller;

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getBiller() {
        return biller;
    }

    public void setBiller(String biller) {
        this.biller = biller;
    }
}
