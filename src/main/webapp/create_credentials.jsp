<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Object cid = request.getAttribute("customerId");
    Object accountNo = request.getAttribute("accountNo");
%>
<html>
<head>
    <title>Create Credentials</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <h2>Create username & password</h2>
    <form method="post" action="${pageContext.request.contextPath}/create-credentials">
        <input type="hidden" name="customerId" value="<%= cid %>"/>
        <input type="hidden" name="accountNo" value="<%= accountNo %>"/>
        <label>Username</label><br/>
        <input name="username" required/><br/>
        <label>Password</label><br/>
        <input name="password" type="password" required/><br/>
        <label>Confirm Password</label><br/>
        <input name="confirm" type="password" required/><br/>
        <% if (request.getAttribute("credError") != null) { %>
        <div class="error"><%= request.getAttribute("credError") %></div>
        <% } %>
        <button type="submit" class="primary">Sign Up</button>
    </form>
</div>
</body>
</html>
