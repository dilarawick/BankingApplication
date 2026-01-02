let otpCooldown = false;
let otpTimer = null;
let resetPasswordToken = null;

const sendOtpBtn = document.getElementById('sendOtpBtn');
const verifyBtn = document.getElementById('verifyBtn');
const confirmBtn = document.getElementById('confirmBtn');
const otpCountdown = document.getElementById('otpCountdown');
const emailInput = document.getElementById('email');
const sendOtpText = sendOtpBtn.querySelector('.btn-text');
const signupVerifySec = document.getElementById('signupVerifySection');
const createCredentialsSec = document.getElementById('createCredentialsSection');

function showOtpLoader() {
    sendOtpBtn.disabled = true;
    sendOtpText.style.display = 'none';

    if (!sendOtpBtn.querySelector('.dot-loader')) {
        const loader = document.createElement('div');
        loader.className = 'dot-loader';
        loader.innerHTML = `<span></span><span></span><span></span>`;
        sendOtpBtn.appendChild(loader);
    }
}

function hideOtpLoader() {
    const loader = sendOtpBtn.querySelector('.dot-loader');
    if (loader) loader.remove();
    sendOtpText.style.display = 'inline';
}

function startOtpCooldown(seconds) {
    otpCooldown = true;
    sendOtpBtn.disabled = true;
    otpCountdown.style.display = 'block';

    let remaining = seconds;
    otpCountdown.textContent = `Resend available in ${remaining}s`;

    if (otpTimer) clearInterval(otpTimer);

    otpTimer = setInterval(() => {
        remaining--;
        if (remaining <= 0) {
            clearInterval(otpTimer);
            otpCooldown = false;
            sendOtpBtn.disabled = false;
            otpCountdown.style.display = 'none';
        } else {
            otpCountdown.textContent = `Resend available in ${remaining}s`;
        }
    }, 1000);
}

sendOtpBtn.addEventListener('click', async () => {
    if (otpCooldown) return;

    const email = emailInput.value.trim();
    if (!email) return showToast('Please enter your email address!', 'error');

    otpCooldown = true;
    showOtpLoader();

    try {
        const res = await fetch('/api/auth/send-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });

        if (!res.ok) {
            let message = 'Failed to send OTP';

            try {
                const err = await res.json();
                if (err.message) message = err.message;
            } catch {}

            showToast(message, 'error');
            hideOtpLoader();

            otpCooldown = false;
            sendOtpBtn.disabled = false;
            return;
        }

        hideOtpLoader();
        startOtpCooldown(60);
        showToast('OTP sent successfully!', 'success');

    } catch {
        hideOtpLoader();
        otpCooldown = false;
        sendOtpBtn.disabled = false;
        showToast('Error sending OTP', 'error');
    }
});

verifyBtn.addEventListener('click', async () => {
    if (verifyBtn.disabled) return;
    verifyBtn.disabled = true;

    const payload = {
        name: document.getElementById('name').value.trim(),
        nic: document.getElementById('nic').value.trim(),
        email: emailInput.value.trim(),
        otp: document.getElementById('otp').value.trim()
    };

    if (Object.values(payload).some(v => !v)) {
        verifyBtn.disabled = false;
        return showToast('Fill all required fields', 'error');
    }

    try {
        const res = await fetch('/api/reset-password/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            let message = 'Failed to verify';

            try {
                const err = await res.json();
                if (err.message) message = err.message;
            } catch {}

            showToast(message, 'error');
            verifyBtn.disabled = false;
            return;
        }
        const data = await res.json();
        if (!data.resetPasswordToken) {
            verifyBtn.disabled = false;
            return showToast('Invalid server response', 'error');
        }
        resetPasswordToken = data.resetPasswordToken;

        clearInterval(otpTimer);
        otpCooldown = true;
        sendOtpBtn.disabled = true;

        signupVerifySec.style.display = 'none';
        createCredentialsSec.style.display = 'block';

        showToast('Details verified. Create credentials.', 'success');

    } catch {
        verifyBtn.disabled = false;
        showToast('An error occured while verifing', 'error');
    }
});

confirmBtn.addEventListener('click', async () => {
    if (confirmBtn.disabled) return;
    confirmBtn.disabled = true;

    const password = document.getElementById('password').value.trim();
    const confirmPassword = document.getElementById('confirmPassword').value.trim();

    if (!password || !confirmPassword) {
        confirmBtn.disabled = false;
        return showToast('Fill all required fields', 'error');
    }

    if (password !== confirmPassword) {
        confirmBtn.disabled = false;
        return showToast('Passwords do not match', 'error');
    }

    try {
        const res = await fetch('/api/reset-password/create-credentials', {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${resetPasswordToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ password })
        });

        if (!res.ok) {
            let message = 'Failed create credentials';

            try {
                const err = await res.json();
                if (err.message) message = err.message;
            } catch {}

            showToast(message, 'error');
            confirmBtn.disabled = false;
            return;
        }

        window.location.href = '/login.html';

    } catch {
        confirmBtn.disabled = false;
        showToast('Error creating credentials', 'error');
    }
});