<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign Up</title>
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
            <form class="signup-form" method="post" action="${pageContext.request.contextPath}/signup-verify">
                <h2>SIGN UP</h2>

                <div id="errorMessage" class="message error" style="display:none;"></div>
                <div id="successMessage" class="message success" style="display:none;"></div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="accountNumber">Account Number :</label>
                        <input type="text" id="accountNumber" name="accountNo" required />
                    </div>
                    <div class="form-group">
                        <label for="branch">Branch :</label>
                        <input type="text" id="branch" name="branch" required />
                    </div>
                </div>

                <div class="form-row full">
                    <div class="form-group">
                        <label for="fullName">Full Name :</label>
                        <input type="text" id="fullName" name="name" required />
                    </div>
                </div>

                <div class="form-row full">
                    <div class="form-group">
                        <label for="email">Email :</label>
                        <input type="email" id="email" name="email" required />
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="nic">NIC :</label>
                        <input type="text" id="nic" name="nic" required />
                    </div>
                    <div class="form-group">
                        <label for="phoneNumber">Phone Number :</label>
                        <input type="tel" id="phoneNumber" name="phone" required />
                    </div>
                </div>
                <% if (request.getAttribute("signupError") != null) { %>
                <div class="error"><%= request.getAttribute("signupError") %></div>
                <% } %>
                <button type="submit" class="btn-confirm">Confirm</button>

                <div class="back-to-signin">
                    <a href="login.jsp">Back to Sign in</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
