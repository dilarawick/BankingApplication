<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Change Password</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles2.css">
    <link rel="icon" type="image/png" href="images/favicon.png">
</head>
<body style="background-image: url('images/brg.png');">
    <div class="signup-wrapper">
        <div class="signup-left" style="background-image: url('images/logo_brg.png');">
            <div class="logo-container">
                <img src="images/logo.png" alt="Logo">
            </div>
        </div>
        <div class="signup-right">
            <% if (request.getAttribute("customerId") == null) { %>
            <form class="signup-form" method="post" action="${pageContext.request.contextPath}/change-password-verify">
                <h2>Change Password</h2>

                <div class="form-row full">
                    <div class="form-group">
                        <label for="fullName">Full Name :</label>
                        <input type="text" id="fullName" name="name" required />
                    </div>
                </div>

                <div class="form-row full">
                    <div class="form-group">
                        <label for="nic">NIC :</label>
                        <input type="text" id="nic" name="nic" required />
                    </div>
                </div>

                <div class="form-row full">
                    <div class="form-group">
                        <label for="email">Email :</label>
                        <input type="email" id="email" name="email" required />
                    </div>
                </div>
                <% if (request.getAttribute("pwdError") != null) { %>
                <div class="error"><%= request.getAttribute("pwdError") %></div>
                <% } %>
                <button type="submit" class="btn-confirm">Verify</button>
            </form>
            <% } %>

            <!-- If verification passed, current page will have customerId attribute and we show the change form -->
            <% if (request.getAttribute("customerId") != null) { %>
            <form class="signup-form" method="post" action="${pageContext.request.contextPath}/change-password">
                <h2>Setup Password</h2>
                <input type="hidden" name="customerId" value="<%= request.getAttribute("customerId") %>"/>
                <div class="form-row full">
                    <div class="form-group">
                        <input type="password" name="newPassword" placeholder="New Password" required />
                    </div>
                </div>

                <div class="form-row full">
                    <div class="form-group">
                        <input type="password" name="confirmPassword" placeholder="Confirm Password" required />
                    </div>
                </div>
                <button type="submit" class="btn-confirm">Confirm</button>
            </form>
            <% } %>

            <div class="back-to-signin">
                <a href="login.jsp">Back to Sign in</a>
            </div>
        </div>
    </div>
</body>
</html>
