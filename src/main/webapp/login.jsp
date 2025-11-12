<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Sign In</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles2.css">
    <link rel="icon" type="image/png" href="images/favicon.png">
</head>
<body style="background-image: url('images/brg.png');">
    <div class="login-wrapper">
        <div class="login-left" style="background-image: url('images/logo_brg.png');">
            <div class="logo-container">
                <img src="images/logo.png" alt="Logo">
            </div>
        </div>

        <div class="login-right">
            <form class="login-form" method="post" action="${pageContext.request.contextPath}/login">
                <h2>LOGIN</h2>

                <div id="errorMessage" class="message error" style="display:none;"></div>
                <div id="successMessage" class="message success" style="display:none;"></div>

                <div class="input-group">
                    <div class="input-wrapper">
                        <span class="input-icon"><img src="images/user_icon.png" alt="User icon"></span>
                        <input type="text" name="username" placeholder="Username" required />
                    </div>
                </div>

                <div class="input-group">
                    <div class="input-wrapper">
                        <span class="input-icon"><img src="images/pass_icon.png" alt="User icon"></span>
                        <input type="password" name="password" placeholder="Password" required />
                    </div>
                    <div class="forgot-password">
                        <a href="change_password.jsp">Forgot password?</a>
                    </div>
                </div>
                <c:if test="${not empty errorMsg}">
                    <span class="error">${errorMsg}</span>
                </c:if>
                <c:if test="${not empty signupSuccess}">
                    <span class="success">${signupSuccess}</span>
                </c:if>
                <c:if test="${not empty pwdSuccess}">
                    <span class="success">${pwdSuccess}</span>
                </c:if>
                <button type="submit" class="btn-signin">Sign In</button>

                <div class="signup-link">
                    Don't have an account?<br>
                    <a href="signup.jsp">SIGN UP</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
