package com.bankapp.servlet;

import com.bankapp.dao.CustomerDAO;
import com.bankapp.dao.CustomerAccountDAO;
import com.bankapp.dao.AccountDAO;
import com.bankapp.model.Customer;
import com.bankapp.model.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardServlet extends jakarta.servlet.http.HttpServlet {

    private CustomerDAO customerDAO = new CustomerDAO();
    private CustomerAccountDAO caDAO = new CustomerAccountDAO();
    private AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        int customerId = (Integer) session.getAttribute("customerId");
        try {
            Customer c = customerDAO.findById(customerId);
            List<String> accountRows = caDAO.getAccountsForCustomer(customerId);
            List<Account> accounts = new ArrayList<>();
            for (String row : accountRows) {
                String[] parts = row.split("\\|");
                Account a = new Account();
                a.setAccountNo(parts[0]);
                a.setAccountBalance(new java.math.BigDecimal(parts[1]));
                a.setAccountType(parts[2]);
                a.setAccountStatus(Boolean.parseBoolean(parts[3]) ? "Primary" : "Secondary");
                accounts.add(a);
            }
            request.setAttribute("customer", c);
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
