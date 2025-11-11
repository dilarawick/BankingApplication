package com.bankapp.dao;

import com.bankapp.model.Customer;
import java.sql.*;

public class CustomerDAO {

    public Customer findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE Username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE CustomerID = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Customer findByNameNicEmailAndAccount(String name, String nic, String email, String accountNo) throws SQLException {
        // Check customer info matches and account belongs to that customer
        String sql = "SELECT c.* FROM Customer c JOIN Account a ON c.CustomerID = a.CustomerID " +
                "WHERE c.Name = ? AND c.NIC = ? AND c.Email = ? AND a.AccountNo = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, nic);
            ps.setString(3, email);
            ps.setString(4, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM Customer WHERE Username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean setCredentialsForCustomer(int customerId, String username, String passwordHash) throws SQLException {
        String sql = "UPDATE Customer SET Username = ?, PasswordHash = ? WHERE CustomerID = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setInt(3, customerId);
            return ps.executeUpdate() == 1;
        }
    }

    public Customer findByNameNic(String name, String nic) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE Name = ? AND NIC = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, nic);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("CustomerID"));
        c.setName(rs.getString("Name"));
        c.setEmail(rs.getString("Email"));
        c.setNic(rs.getString("NIC"));
        c.setPhoneNumber(rs.getString("PhoneNumber"));
        c.setUsername(rs.getString("Username"));
        c.setPasswordHash(rs.getString("PasswordHash"));
        return c;
    }
}
