(function authGuard() {
    const token = sessionStorage.getItem('token');

    // If no token present — redirect to login
    if (!token) {
        window.location.replace('login.html');
        return;
    }

    // Basic JWT validation: decode payload and check expiry and scope
    try {
        const parts = token.split('.');
        if (parts.length !== 3) throw new Error('Invalid token format');

        // Base64URL decode
        const payloadJson = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
        const payload = JSON.parse(decodeURIComponent(escape(payloadJson)));

        // Check scope if present
        if (payload.scope && payload.scope !== 'LOGIN') {
            localStorage.removeItem('token');
            window.location.replace('login.html');
            return;
        }

        // Check expiry (exp is in seconds since epoch)
        if (payload.exp && Date.now() / 1000 > payload.exp) {
            localStorage.removeItem('token');
            window.location.replace('login.html');
            return;
        }

        // Token looks syntactically valid and not expired — allow page to load
    } catch (e) {
        // Malformed token: remove and redirect
        localStorage.removeItem('token');
        window.location.replace('login.html');
    }
})();