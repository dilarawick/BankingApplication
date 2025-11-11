package com.bankapp.servlet;

import com.bankapp.dao.CustomerDAO;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public class ChangePasswordServlet extends jakarta.servlet.http.HttpServlet {

    private CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doPost(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        int customerId = Integer.parseInt(request.getParameter("customerId"));
        String newPassword = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");
        if (!newPassword.equals(confirm)) {
            request.setAttribute("pwdError", "Passwords do not match.");
            request.getRequestDispatcher("/change_password.jsp").forward(request, response);
            return;
        }
        try {
            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            boolean ok = customerDAO.setCredentialsForCustomer(customerId, customerDAO.findById(customerId).getUsername(), hashed);
            if (!ok) throw new SQLException("Failed to update password.");
            request.setAttribute("pwdSuccess", "Password updated. Please login.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
