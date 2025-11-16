document.getElementById('signinBtn').addEventListener('click', async () => {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const err = document.getElementById('login-error');

    const res = await fetch('/api/auth/login', {
        method:'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({username, password})
    });
    const data = await res.json();
    if (data.ok) {
        window.location.href = '/dashboard.html';
    } else {
        err.style.display = 'block';
        err.textContent = data.message || 'Username or password incorrect';
    }
});
