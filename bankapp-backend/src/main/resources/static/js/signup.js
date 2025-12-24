let cooldown = false;
let timer = null;
document.getElementById('sendOtpBtn').addEventListener('click', async () => {
    if (cooldown) return;
    const email = document.getElementById('email').value.trim();
    if (!email) return alert('Enter email first');
    const res = await fetch('/api/auth/send-otp', {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({email})
    });
    const data = await res.json();
    if (data.ok) {
        startCooldown(60);
    } else {
        alert('Failed to send OTP');
    }
});

function startCooldown(seconds) {
    cooldown = true;
    const btn = document.getElementById('sendOtpBtn');
    const cd = document.getElementById('otp-countdown');
    btn.disabled = true;
    let left = seconds;
    cd.textContent = `Resend available in ${left}s`;
    timer = setInterval(()=> {
        left--;
        if (left<=0) {
            clearInterval(timer);
            cooldown = false;
            btn.disabled = false;
            cd.textContent = '';
        } else {
            cd.textContent = `Resend available in ${left}s`;
        }
    },1000);
}

document.getElementById('confirmBtn').addEventListener('click', async ()=>{
    const accountNo = document.getElementById('accountNo').value.trim();
    const city = document.getElementById('branch').value.trim();
    const name = document.getElementById('name').value.trim();
    const nic = document.getElementById('nic').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const email = document.getElementById('email').value.trim();
    const otp = document.getElementById('otp').value.trim();
    const password = document.getElementById('password').value.trim();
    const confirmPassword = document.getElementById('confirmPassword').value.trim();
    
    // Client-side validation
    if (!accountNo || !city || !name || !nic || !phone || !email || !otp || !password) {
        const err = document.getElementById('signup-error');
        err.style.display = 'block';
        err.textContent = 'Please fill in all required fields';
        return;
    }
    
    if (password !== confirmPassword) {
        const err = document.getElementById('signup-error');
        err.style.display = 'block';
        err.textContent = 'Passwords do not match';
        return;
    }
    
    if (password.length < 6) {
        const err = document.getElementById('signup-error');
        err.style.display = 'block';
        err.textContent = 'Password must be at least 6 characters long';
        return;
    }
    
    const payload = {
        accountNo: accountNo,
        city: city,
        name: name,
        nic: nic,
        phone: phone,
        email: email,
        otp: otp,
        password: password
    };
    const err = document.getElementById('signup-error');
    const res = await fetch('/api/auth/verify-signup', {
        method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.ok) {
        // redirect to dashboard after successful signup
        window.location.href = '/dashboard.html';
    } else {
        err.style.display = 'block';
        err.textContent = data.message || 'Verification failed';
    }
});
