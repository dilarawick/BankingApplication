document.getElementById('signupBtn').addEventListener('click', async () => {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const confirm = document.getElementById('confirmPassword').value;
    const err = document.getElementById('cred-error');

    if (password !== confirm) {
        err.style.display = 'block';
        err.textContent = "Passwords do not match";
        return;
    }
    const res = await fetch('/api/auth/create-credentials', {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({username, password})
    });
    const data = await res.json();
    if (data.ok) {
        window.location.href = '/login.html';
    } else {
        err.style.display = 'block';
        err.textContent = data.message || 'Failed to create credentials';
    }
});
