function initDashboard() {
    const rawName = localStorage.getItem('name') || 'Name';
    const name = rawName.charAt(0).toUpperCase() + rawName.slice(1);

    document.getElementById('name').textContent = `Welcome back, ${name}`;
}

window.handleLogout = async function() {
    localStorage.clear();

    window.location.replace('../login.html');
};

window.handleSettings = function() {
    window.location.href = '#';
};

function showToast(message, type) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} active`;
    setTimeout(() => toast.classList.remove('active'), 3000);
}