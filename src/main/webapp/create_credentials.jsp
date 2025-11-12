<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Object cid = request.getAttribute("customerId");
    Object accountNo = request.getAttribute("accountNo");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Credentials</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles2.css">
    <link rel="icon" type="image/png" href="images/favicon.png">
</head>
<body style="background-image: url('images/brg.png');">
    <div class="signup-wrapper">
        <div class="login-left" style="background-image: url('images/logo_brg.png');">
            <div class="logo-container">
                <img src="images/logo.png" alt="Logo">
            </div>
        </div>
        <div class="login-right">
            <form class="signup-form" method="post" action="${pageContext.request.contextPath}/create-credentials">
                <h2>SIGN UP</h2>
                <input type="hidden" name="customerId" value="<%= cid %>"/>
                <input type="hidden" name="accountNo" value="<%= accountNo %>"/>
                <div class="input-group">
                    <div class="input-wrapper">
                        <input type="text" name="username" placeholder="Username" required />
                    </div>
                </div>

                <div class="input-group">
                    <div class="input-wrapper">
                        <input type="password" name="password" placeholder="Password" required />
                    </div>
                </div>

                <div class="input-group">
                    <div class="input-wrapper">
                        <input type="password" name="confirm" placeholder="Confirm Password" required />
                    </div>
                </div>
                <% if (request.getAttribute("credError") != null) { %>
                <div class="error"><%= request.getAttribute("credError") %></div>
                <% } %>
                <button type="submit" class="btn-confirm">Sign Up</button>
            </form>
        </div>
    </div>
</body>
</html>
