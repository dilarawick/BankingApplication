<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Sign Up - Verify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <h2>Sign Up - Verify details</h2>
    <form method="post" action="${pageContext.request.contextPath}/signup-verify">
        <label>Account Number (6 chars)</label><br/>
        <input name="accountNo" required/><br/>
        <label>Branch</label><br/>
        <input name="branch"/><br/>
        <label>Name</label><br/>
        <input name="name" required/><br/>
        <label>Email</label><br/>
        <input name="email" type="email" required/><br/>
        <label>NIC</label><br/>
        <input name="nic" required/><br/>
        <label>Phone</label><br/>
        <input name="phone"/><br/>
        <% if (request.getAttribute("signupError") != null) { %>
        <div class="error"><%= request.getAttribute("signupError") %></div>
        <% } %>
        <button type="submit" class="primary">Confirm</button>
    </form>
</div>
</body>
</html>
