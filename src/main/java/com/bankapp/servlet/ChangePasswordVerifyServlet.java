package com.bankapp.servlet;

import com.bankapp.dao.CustomerDAO;
import com.bankapp.model.Customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public class ChangePasswordVerifyServlet extends jakarta.servlet.http.HttpServlet {

    private CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doPost(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String nic = request.getParameter("nic");
        String email = request.getParameter("email");

        try {
            Customer c = customerDAO.findByNameNic(name, nic);
            if (c == null || !c.getEmail().equalsIgnoreCase(email)) {
                request.setAttribute("pwdError", "Details did not match.");
                request.getRequestDispatcher("/change_password.jsp").forward(request, response);
                return;
            }
            // Allow change: forward with customerId
            request.setAttribute("customerId", c.getCustomerId());
            request.getRequestDispatcher("/change_password.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
