package com.bankapp.servlet;

import com.bankapp.dao.CustomerDAO;
import com.bankapp.dao.CustomerAccountDAO;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public class CreateCredentialsServlet extends jakarta.servlet.http.HttpServlet {

    private CustomerDAO customerDAO = new CustomerDAO();
    private CustomerAccountDAO customerAccountDAO = new CustomerAccountDAO();

    @Override
    protected void doPost(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");
        int customerId = Integer.parseInt(request.getParameter("customerId"));
        String accountNo = request.getParameter("accountNo");

        if (!password.equals(confirm)) {
            request.setAttribute("credError", "Passwords do not match.");
            request.setAttribute("customerId", customerId);
            request.setAttribute("accountNo", accountNo);
            request.getRequestDispatcher("/create_credentials.jsp").forward(request, response);
            return;
        }
        try {
            if (customerDAO.usernameExists(username)) {
                request.setAttribute("credError", "Username already taken.");
                request.setAttribute("customerId", customerId);
                request.setAttribute("accountNo", accountNo);
                request.getRequestDispatcher("/create_credentials.jsp").forward(request, response);
                return;
            }
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
            boolean ok = customerDAO.setCredentialsForCustomer(customerId, username, hashed);
            if (!ok) throw new SQLException("Failed to write credentials.");
            // add to CustomerAccount as primary
            customerAccountDAO.addCustomerAccount(customerId, accountNo, true);
            // redirect to login
            request.setAttribute("signupSuccess", "Account created. Please login.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
