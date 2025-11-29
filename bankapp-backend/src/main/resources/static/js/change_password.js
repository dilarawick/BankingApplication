let cooldown = false, timer=null;
document.getElementById('sendOtpBtn').addEventListener('click', async () => {
    if (cooldown) return;
    const email = document.getElementById('email').value.trim();
    if (!email) return alert('Enter email first');
    const res = await fetch('/api/auth/send-otp-for-reset', {
        method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({email})
    });
    const data = await res.json();
    if (data.ok) startCooldown(60);
    else alert('Failed to send OTP');
});

function startCooldown(seconds) {
    cooldown = true;
    const btn = document.getElementById('sendOtpBtn');
    const cd = document.getElementById('countdown');
    btn.disabled = true;
    let left = seconds;
    cd.textContent = `Resend in ${left}s`;
    timer = setInterval(()=> {
        left--;
        if (left<=0) {
            clearInterval(timer); cooldown=false; btn.disabled=false; cd.textContent='';
        } else cd.textContent = `Resend in ${left}s`;
    },1000);
}

document.getElementById('verifyBtn').addEventListener('click', async () => {
    const payload = {
        name: document.getElementById('name').value.trim(),
        nic: document.getElementById('nic').value.trim(),
        email: document.getElementById('email').value.trim(),
        otp: document.getElementById('otp').value.trim()
    };
    const res = await fetch('/api/auth/verify-reset', {
        method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)
    });
    const data = await res.json();
    const err = document.getElementById('verify-error');
    if (data.ok) {
        document.getElementById('changeSection').style.display = 'block';
        err.style.display = 'none';
    } else {
        err.style.display = 'block';
        err.textContent = data.message || 'Verification failed';
    }
});

document.getElementById('changeBtn').addEventListener('click', async () => {
    const newPassword = document.getElementById('newPassword').value;
    const confirm = document.getElementById('confirmPassword').value;
    const err = document.getElementById('change-error');
    if (newPassword !== confirm) { err.style.display='block'; err.textContent='Passwords do not match'; return; }
    const res = await fetch('/api/auth/change-password', {
        method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({password: newPassword})
    });
    const data = await res.json();
    if (data.ok) {
        alert('Password changed. Please login.');
        window.location.href = '/login.html';
    } else {
        err.style.display='block';
        err.textContent = data.message || 'Unable to change password';
    }
});
