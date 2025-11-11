<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Change Password</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <h2>Change Password</h2>
    <!-- Step 1: verify identity -->
    <form method="post" action="${pageContext.request.contextPath}/change-password-verify">
        <label>Name</label><br/>
        <input name="name" required/><br/>
        <label>NIC</label><br/>
        <input name="nic" required/><br/>
        <label>Email</label><br/>
        <input name="email" type="email" required/><br/>
        <% if (request.getAttribute("pwdError") != null) { %>
        <div class="error"><%= request.getAttribute("pwdError") %></div>
        <% } %>
        <button type="submit">Verify</button>
    </form>

    <!-- If verification passed, current page will have customerId attribute and we show the change form -->
    <% if (request.getAttribute("customerId") != null) { %>
    <hr/>
    <form method="post" action="${pageContext.request.contextPath}/change-password">
        <input type="hidden" name="customerId" value="<%= request.getAttribute("customerId") %>"/>
        <label>New Password</label><br/>
        <input name="newPassword" type="password" required/><br/>
        <label>Confirm Password</label><br/>
        <input name="confirmPassword" type="password" required/><br/>
        <button type="submit">Change Password</button>
    </form>
    <% } %>
</div>
</body>
</html>
