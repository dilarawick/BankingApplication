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
    const cd = document.getElementById('countdown');
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
    const payload = {
        accountNo: document.getElementById('accountNo').value.trim(),
        branch: document.getElementById('branch').value.trim(),
        name: document.getElementById('name').value.trim(),
        nic: document.getElementById('nic').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        email: document.getElementById('email').value.trim(),
        otp: document.getElementById('otp').value.trim()
    };
    const err = document.getElementById('signup-error');
    const res = await fetch('/api/auth/verify-signup', {
        method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.ok) {
        // move to create credentials page
        window.location.href = '/create_credentials.html';
    } else {
        err.style.display = 'block';
        err.textContent = data.message || 'Verification failed';
    }
});
