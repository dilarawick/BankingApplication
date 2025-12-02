document.addEventListener('DOMContentLoaded', () => {
    initializeNavigation();
    loadDashboard(); // Start loading data and wiring UI
});

// ---------------------- helpers ----------------------
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-LK', {
        style: 'currency',
        currency: 'LKR',
        minimumFractionDigits: 2
    }).format(Number(amount || 0));
}

function createEl(tag, cls, inner) {
    const el = document.createElement(tag);
    if (cls) el.className = cls;
    if (inner !== undefined) el.innerHTML = inner;
    return el;
}

// ---------------------- navigation ----------------------
function initializeNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            navItems.forEach(n => n.classList.remove('active'));
            item.classList.add('active');
        });
    });
}

// ---------------------- account carousel ----------------------
let carouselState = {
    currentIndex: 0,
    intervalId: null,
    accounts: []
};

function renderAccountSlides(accounts) {
    const slidesContainer = document.getElementById('accountSlides');
    const dotsContainer = document.getElementById('carouselDots');

    slidesContainer.innerHTML = '';
    dotsContainer.innerHTML = '';

    if (!accounts || accounts.length === 0) {
        const empty = createEl('div', 'account-card', `<strong>No accounts</strong><div>Add an account to start</div>`);
        slidesContainer.appendChild(empty);
        document.getElementById('balanceAmount').textContent = formatCurrency(0);
        return;
    }

    accounts.forEach((acc, idx) => {
        const card = createEl('div', 'account-card', `
            <div class="balance-card">
                <div class="balance-info">
                    <div class="balance-label">Current Balance</div>
                    <div class="balance-amount">${formatCurrency(acc.balance)}</div>
                </div>
            </div>
            <div class="account-info">
                <div class="account-name">${acc.nickname}</div>
                <div class="account-type">${acc.type || 'No type'}</div>  <!-- Use acc.type instead of acc.accountType -->
                <div class="account-no">Acc: ${acc.accountNo}</div>
            </div>
        `);

        if (idx !== 0) card.style.display = 'none';
        slidesContainer.appendChild(card);

        const dot = createEl('span', 'dot' + (idx === 0 ? ' active' : ''));
        dot.dataset.index = idx;
        dot.addEventListener('click', () => {
            goToAccountIndex(Number(dot.dataset.index));
        });
        dotsContainer.appendChild(dot);
    });

    carouselState.accounts = accounts;
    carouselState.currentIndex = 0;
    updateBalanceForCurrent();
    startCarouselAuto();
}
    

function updateBalanceForCurrent() {
    const idx = carouselState.currentIndex || 0;
    const acc = carouselState.accounts[idx];
    const balEl = document.getElementById('balanceAmount');
    if (balEl) {
        balEl.textContent = formatCurrency(acc.balance || 0);
    }
}

function goToAccountIndex(index) {
    if (!carouselState.accounts || carouselState.accounts.length === 0) return;
    index = index % carouselState.accounts.length;
    showSlide(index);
}

function showSlide(index) {
    const slides = document.querySelectorAll('#accountSlides .account-card');
    const dots = document.querySelectorAll('#carouselDots .dot');
    if (!slides.length) return;

    slides.forEach((s, i) => s.style.display = i === index ? '' : 'none');
    dots.forEach((d, i) => {
        if (i === index) d.classList.add('active');
        else d.classList.remove('active');
    });

    carouselState.currentIndex = index;
    updateBalanceForCurrent();
}

function startCarouselAuto() {
    stopCarouselAuto();
    carouselState.intervalId = setInterval(() => {
        showSlide((carouselState.currentIndex + 1) % carouselState.accounts.length);
    }, 5000);
}

function stopCarouselAuto() {
    if (carouselState.intervalId) {
        clearInterval(carouselState.intervalId);
        carouselState.intervalId = null;
    }
}

// ---------------------- transactions ----------------------
function addTransaction(date, id, amount, status) {
    const transactionsList = document.getElementById('transactionsList');
    const transactionItem = createEl('div', 'transaction-item', `
        <div class="transaction-avatar">
            <img src="img/pfp_img.png" alt="Transaction">
        </div>
        <div class="transaction-date">${date}</div>
        <div class="transaction-id">........${String(id || '').slice(-4)}</div>
        <div class="transaction-amount">${formatCurrency(amount)}</div>
        <div class="transaction-status ${String(status || '').toLowerCase()}">${status || ''}</div>
    `);
    transactionsList.prepend(transactionItem);
}

// ---------------------- saved payees ----------------------
function renderSavedPayees(accounts) {
    const saved = document.getElementById('savedPayees');
    saved.innerHTML = '';

    if (!accounts || accounts.length === 0) {
        saved.innerHTML = '<p style="color:#777">No saved payees</p>';
        return;
    }

    accounts.forEach(acc => {
        const item = createEl('div', 'payee-item', `
            <div class="payee-avatar"><img src="img/pfp_img.png" alt="avatar"></div>
            <div class="payee-info">
                <div class="payee-name">${acc.nickname}</div>
                <div class="payee-details">${acc.accountNo}</div>
            </div>
        `);
        saved.appendChild(item);
    });
}

// ---------------------- main loader ----------------------
async function loadDashboard() {
    try {
        const response = await fetch('/api/dashboard/data/1');
        const data = await response.json();
        applyDashboardData(data);
    } catch (err) {
        console.error('Error loading dashboard data', err);
    }
}

function applyDashboardData(data) {
    const greet = document.getElementById('greeting');
    if (greet) greet.textContent = `Welcome back, ${data.customerName || 'Customer'}!`;

    const accounts = data.accounts || [];
    renderAccountSlides(accounts);
    renderSavedPayees(accounts);

    const txs = data.recentTransactions || [];
    const list = document.getElementById('transactionsList');
    list.innerHTML = '';

    if (!txs.length) {
        list.innerHTML = '<p style="text-align:center;color:#777">No recent transactions</p>';
    } else {
        txs.forEach(t => {
            const date = t.transactionDate || '-';
            const id = t.transactionCode || '-';
            const amount = t.amount || 0;
            const status = t.status || 'Completed';

            const d = new Date(date);
            const dateStr = !isNaN(d) ? d.toLocaleString() : date;

            addTransaction(dateStr, id, amount, status);
        });
    }
}

// ---------------------- logout ----------------------
const logoutBtn = document.getElementById('logoutBtn');
if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
        try {
            await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
        } catch (e) { /* ignore */ }
        window.location.href = '/login.html';
    });
}
