package com.bankapp.dto;

public class EStatementEmailRequest {
    private String accountNo;
    private EStatementDTO statement;

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public EStatementDTO getStatement() {
        return statement;
    }

    public void setStatement(EStatementDTO statement) {
        this.statement = statement;
    }
}