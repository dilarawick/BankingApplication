<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Sign In</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <h2>Sign in</h2>
    <form method="post" action="${pageContext.request.contextPath}/login">
        <label>Username</label><br/>
        <input name="username" type="text" required/><br/>
        <label>Password</label><br/>
        <input name="password" type="password" required/><br/>
        <a href="change_password.jsp">Forgot password?</a><br/>
        <c:if test="${not empty errorMsg}">
            <div class="error">${errorMsg}</div>
        </c:if>
        <c:if test="${not empty signupSuccess}">
            <div class="success">${signupSuccess}</div>
        </c:if>
        <button type="submit" class="primary">Sign In</button>
    </form>
    <p>Don't have an account? <a href="signup.jsp">Sign up.</a></p>
</div>
</body>
</html>
