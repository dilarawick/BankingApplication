package com.bankapp.servlet;

import com.bankapp.dao.CustomerDAO;
import com.bankapp.model.Customer;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public class LoginServlet extends jakarta.servlet.http.HttpServlet {

    private CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String error = null;
        try {
            Customer c = customerDAO.findByUsername(username);
            if (c == null || c.getPasswordHash() == null || !BCrypt.checkpw(password, c.getPasswordHash())) {
                error = "Username or password incorrect.";
                request.setAttribute("errorMsg", error);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            // login success
            HttpSession session = request.getSession();
            session.setAttribute("customerId", c.getCustomerId());
            session.setAttribute("customerName", c.getName());
            session.setAttribute("customerEmail", c.getEmail());
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
