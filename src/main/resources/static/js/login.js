document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const errorBox = document.getElementById('login-error');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value;

        if (!username || !password) {
            showError("Please fill all required fields.");
            return;
        }

        hideError();

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                let errorMessage = "Invalid username or password.";

                try {
                    const errorData = await response.json();
                    if (errorData && errorData.message) {
                        errorMessage = errorData.message;
                    }
                } catch (e) {
                }

                showError(errorMessage);
                return;

            }

            const data = await response.json();

            localStorage.setItem('token', data.token);
            localStorage.setItem('customerId', data.customerId);
            localStorage.setItem('name', data.name);
            localStorage.setItem('email', data.email);

            window.location.href = '/dashboard.html';

        } catch (err) {
            console.error(err);
            showError("Server error. Please try again.");
        }
    });

    [usernameInput, passwordInput].forEach(input => {
        input.addEventListener('input', hideError);
    });

    function showError(message) {
        errorBox.innerText = message;
        errorBox.style.display = 'block';
    }

    function hideError() {
        errorBox.style.display = 'none';
        errorBox.innerText = '';
    }
});