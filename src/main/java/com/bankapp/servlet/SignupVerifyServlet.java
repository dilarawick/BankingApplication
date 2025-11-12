package com.bankapp.servlet;

import com.bankapp.dao.CustomerDAO;
import com.bankapp.model.Customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public class SignupVerifyServlet extends jakarta.servlet.http.HttpServlet {

    private CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doPost(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        String accountNo = request.getParameter("accountNo");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String nic = request.getParameter("nic");
        String phone = request.getParameter("phone"); // optional

        try {
            Customer c = customerDAO.findByNameNicEmailAndAccount(name, nic, email, accountNo);
            if (c == null) {
                request.setAttribute("signupError", "Provided details do not match our records.");
                request.getRequestDispatcher("/signup.jsp").forward(request, response);
                return;
            }
            if (c.getUsername() != null) {
                request.setAttribute("signupError", "User already registered.");
                request.getRequestDispatcher("/signup.jsp").forward(request, response);
                return;
            }
            // Ok -> forward to create credentials, pass customerId and accountNo as hidden
            request.setAttribute("customerId", c.getCustomerId());
            request.setAttribute("accountNo", accountNo);
            request.getRequestDispatcher("/create_credentials.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
