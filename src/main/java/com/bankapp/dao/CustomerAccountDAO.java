package com.bankapp.dao;

import com.bankapp.model.CustomerAccount;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerAccountDAO {

    public boolean addCustomerAccount(int customerId, String accountNo, boolean isPrimary) throws SQLException {
        String unsetSql = "UPDATE CustomerAccount SET IsPrimary = FALSE WHERE CustomerID = ?";
        String insertSql = "INSERT INTO CustomerAccount (CustomerID, AccountNo, IsPrimary) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement unset = c.prepareStatement(unsetSql)) {
                unset.setInt(1, customerId);
                unset.executeUpdate();
            }
            try (PreparedStatement insert = c.prepareStatement(insertSql)) {
                insert.setInt(1, customerId);
                insert.setString(2, accountNo);
                insert.setBoolean(3, isPrimary);
                insert.executeUpdate();
            }
            c.commit();
            return true;
        } catch (SQLException e) {
            throw e;
        }
    }

    public List<String> getAccountsForCustomer(int customerId) throws SQLException {
        String sql = "SELECT ca.AccountNo, ca.IsPrimary, a.AccountBalance, a.AccountType FROM CustomerAccount ca JOIN Account a ON ca.AccountNo = a.AccountNo WHERE ca.CustomerID = ? ORDER BY ca.IsPrimary DESC, ca.AddedDate ASC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> rows = new ArrayList<>();
                while (rs.next()) {
                    String row = rs.getString("AccountNo") + "|" + rs.getBigDecimal("AccountBalance") + "|" + rs.getString("AccountType") + "|" + rs.getBoolean("IsPrimary");
                    rows.add(row);
                }
                return rows;
            }
        }
    }
}
