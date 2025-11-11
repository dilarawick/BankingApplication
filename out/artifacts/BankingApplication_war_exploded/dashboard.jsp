<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    com.bankapp.model.Customer customer = (com.bankapp.model.Customer) request.getAttribute("customer");
    java.util.List<com.bankapp.model.Account> accounts = (java.util.List<com.bankapp.model.Account>) request.getAttribute("accounts");
%>
<html>
<head>
    <title>Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <h2>Welcome, <%= customer != null ? customer.getName() : "User" %></h2>
    <p>Email: <%= customer != null ? customer.getEmail() : "" %></p>
    <h3>Your accounts</h3>
    <table>
        <tr><th>Account No</th><th>Type</th><th>Balance</th><th>Primary</th></tr>
        <% if (accounts != null) {
            for (com.bankapp.model.Account a : accounts) { %>
        <tr>
            <td><%= a.getAccountNo() %></td>
            <td><%= a.getAccountType() %></td>
            <td><%= a.getAccountBalance() %></td>
            <td><%= "Primary".equals(a.getAccountStatus()) ? "Yes" : "No" %></td>
        </tr>
        <%  }
        } %>
    </table>
</div>
</body>
</html>
