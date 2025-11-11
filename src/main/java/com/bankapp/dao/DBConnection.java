package com.bankapp.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DBConnection {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("jdbc.url");
            username = p.getProperty("jdbc.username");
            password = p.getProperty("jdbc.password");
            driver = p.getProperty("jdbc.driver");
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load DB properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(AutoCloseable ac) {
        if (ac == null) return;
        try { ac.close(); } catch (Exception ignored) {}
    }
}
