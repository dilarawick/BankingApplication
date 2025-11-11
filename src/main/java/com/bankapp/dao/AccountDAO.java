package com.bankapp.dao;

import com.bankapp.model.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class AccountDAO {

    public Account findByAccountNo(String accountNo) throws SQLException {
        String sql = "SELECT * FROM Account WHERE AccountNo = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Account> findAccountsByCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM Account WHERE CustomerID = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Account> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountNo(rs.getString("AccountNo"));
        a.setCustomerId(rs.getInt("CustomerID"));
        a.setAccountType(rs.getString("AccountType"));
        a.setBranchId(rs.getInt("BranchID"));
        a.setAccountBalance(rs.getBigDecimal("AccountBalance"));
        a.setAccountStatus(rs.getString("AccountStatus"));
        return a;
    }
}
