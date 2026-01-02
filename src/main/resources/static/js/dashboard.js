document.addEventListener('DOMContentLoaded', () => {
    initializeCarousel();
    initializeNavigation();
    initializeAnimations();
    initializeSearch();
    initDashboard();
});

function initializeCarousel() {
    const dots = document.querySelectorAll('.dot');
    let currentIndex = 0;

    dots.forEach((dot, index) => {
        dot.addEventListener('click', () => {
            dots.forEach(d => d.classList.remove('active'));
            dot.classList.add('active');
            currentIndex = index;
        });
    });

    setInterval(() => {
        dots.forEach(d => d.classList.remove('active'));
        currentIndex = (currentIndex + 1) % dots.length;
        dots[currentIndex].classList.add('active');
    }, 5000);
}

function initializeNavigation() {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
        });
    });
}


function initializeAnimations() {
    const transactionItems = document.querySelectorAll('.transaction-item');

    transactionItems.forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateY(20px)';

        setTimeout(() => {
            item.style.transition = 'all 0.5s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateY(0)';
        }, index * 100);
    });

    const balanceCard = document.querySelector('.balance-card');
    if (balanceCard) {
        balanceCard.addEventListener('mouseenter', () => {
            balanceCard.style.transform = 'scale(1.02)';
            balanceCard.style.transition = 'transform 0.3s ease';
        });

        balanceCard.addEventListener('mouseleave', () => {
            balanceCard.style.transform = 'scale(1)';
        });
    }
}

function initializeSearch() {
    const searchBtn = document.querySelector('.header-actions .icon-btn:first-child');

    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            console.log('Search clicked');
        });
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-LK', {
        style: 'currency',
        currency: 'LKR',
        minimumFractionDigits: 2
    }).format(amount);
}

function addTransaction(date, id, amount, status) {
    const transactionsList = document.querySelector('.transactions-list');

    const transactionItem = document.createElement('div');
    transactionItem.className = 'transaction-item';

    transactionItem.innerHTML = `
        <div class="transaction-avatar">
            <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='50' fill='%2340b4e5'/%3E%3Cpath d='M50 55c-8 0-15-7-15-15s7-15 15-15 15 7 15 15-7 15-15 15zm0-25c-5.5 0-10 4.5-10 10s4.5 10 10 10 10-4.5 10-10-4.5-10-10-10zM70 85H30c-2.8 0-5-2.2-5-5 0-11 9-20 20-20h10c11 0 20 9 20 20 0 2.8-2.2 5-5 5z' fill='%23fff'/%3E%3C/svg%3E" alt="Transaction">
        </div>
        <div class="transaction-date">${date}</div>
        <div class="transaction-id">........${id}</div>
        <div class="transaction-amount">-Rs. ${amount}</div>
        <div class="transaction-status ${status.toLowerCase()}">${status}</div>
    `;

    transactionItem.style.opacity = '0';
    transactionItem.style.transform = 'translateY(20px)';
    transactionsList.prepend(transactionItem);

    setTimeout(() => {
        transactionItem.style.transition = 'all 0.5s ease';
        transactionItem.style.opacity = '1';
        transactionItem.style.transform = 'translateY(0)';
    }, 50);
}

const notificationBtn = document.querySelector('.header-actions .icon-btn:nth-child(2)');
if (notificationBtn) {
    notificationBtn.addEventListener('click', () => {
        alert('You have no new notifications');
    });
}

const profileAvatar = document.querySelector('.profile-avatar');
if (profileAvatar) {
    profileAvatar.addEventListener('click', () => {
        console.log('Profile clicked');
    });
}

load();
