document.addEventListener('DOMContentLoaded', () => {
    initializeCarousel();
    initializeNavigation();
    initializeAnimations();
    initializeSearch();
    initDashboard();
    loadSelectedAccountBalance();
    setupAccountSwitching();
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

    // Set up auto-rotation but store interval ID to potentially clear it later
    const intervalId = setInterval(() => {
        dots.forEach(d => d.classList.remove('active'));
        currentIndex = (currentIndex + 1) % dots.length;
        dots[currentIndex].classList.add('active');
    }, 5000);
    
    // Store interval ID in case we need to clear it
    window.carouselInterval = intervalId;
}

function initializeNavigation() {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
        // Update active state without preventing default navigation
        item.addEventListener('click', (e) => {
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
            showToast('Search clicked', 'info');
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

async function loadSelectedAccountBalance() {
    try {
        const sel = localStorage.getItem('selectedAccount');
        
        // If no selected account is stored, check if the user has any accounts
        if (!sel) {
            const accounts = await loadAllAccounts();
            const balEl = document.querySelector('.balance-amount');
            if (balEl) {
                if (accounts.length === 0) {
                    // No accounts linked, show "-----"
                    balEl.innerText = '-----';
                } else {
                    // User has accounts, set the first one as selected
                    const firstAccount = accounts[0];
                    const accountData = {
                        accountNo: firstAccount.accountNo,
                        accountNickname: firstAccount.accountNickname
                    };
                    localStorage.setItem('selectedAccount', JSON.stringify(accountData));
                    
                    // Now load the balance for this account
                        const res = await fetch(`/api/accounts/${encodeURIComponent(firstAccount.accountNo)}/balance`, {
                            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('token')}` }
                    });
                    
                    if (!res.ok) {
                        balEl.innerText = '-----';
                        localStorage.removeItem('selectedAccount');
                    } else {
                        const data = await res.json();
                        balEl.innerText = `Rs. ${Number(data.accountBalance).toLocaleString('en-LK', {minimumFractionDigits:2, maximumFractionDigits:2})}`;
                    }
                }
            }
            return;
        }
        
        const obj = JSON.parse(sel);
        if (!obj || !obj.accountNo) {
            const balEl = document.querySelector('.balance-amount');
            if (balEl) balEl.innerText = '-----';
            return;
        }

            const res = await fetch(`/api/accounts/${encodeURIComponent(obj.accountNo)}/balance`, {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('token')}` }
        });
        
        const balEl = document.querySelector('.balance-amount');
        if (balEl) {
            if (!res.ok) {
                // If the account is not found or not accessible (unlinked), show "-----"
                balEl.innerText = '-----';
                // Also remove the selected account from localStorage
                localStorage.removeItem('selectedAccount');
            } else {
                const data = await res.json();
                balEl.innerText = `Rs. ${Number(data.accountBalance).toLocaleString('en-LK', {minimumFractionDigits:2, maximumFractionDigits:2})}`;
            }
        }
    } catch (e) {
        showToast('Failed to load selected account balance', 'error');
        console.error('Failed to load selected account balance', e);
        const balEl = document.querySelector('.balance-amount');
        if (balEl) {
            balEl.innerText = '-----';
            // Remove the selected account from localStorage
            localStorage.removeItem('selectedAccount');
        }
    }
}

// Listen for changes to selected account in localStorage from other tabs/pages
window.addEventListener('storage', function(e) {
    if (e.key === 'selectedAccount') {
        // Selected account was changed in another tab, update the display
        loadSelectedAccountBalance();
        
        // Also update the account card selection if on the accounts page
        if (window.location.pathname.includes('accounts.html')) {
            // If we're on the accounts page, we need to update the account card selection
            // This will be handled by the accounts.js file, but we'll make sure the function exists
        }
    }
});

// Function to load all accounts for the user
async function loadAllAccounts() {
    try {
        const response = await fetch('/api/accounts/my-accounts', {
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            }
        });
        
        if (!response.ok) return [];
        const accounts = await response.json();
        return accounts;
    } catch (error) {
        showToast('Failed to load accounts', 'error');
        console.error('Failed to load accounts', error);
        return [];
    }
}

// Function to handle account switching
function setupAccountSwitching() {
    const arrowBtn = document.querySelector('.arrow-btn');
    if (!arrowBtn) return;
    
    arrowBtn.addEventListener('click', async () => {
        const accounts = await loadAllAccounts();
        
        if (accounts.length === 0) {
            showToast('No accounts linked to your profile', 'info');
            return;
        }
        
        // Create a simple modal or dropdown to select an account
        const selectedAccount = localStorage.getItem('selectedAccount');
        let currentAccount = null;
        if (selectedAccount) {
            currentAccount = JSON.parse(selectedAccount);
        }
        
        // Find the current account index to cycle to the next one
        let currentIndex = -1;
        if (currentAccount) {
            currentIndex = accounts.findIndex(acc => acc.accountNo === currentAccount.accountNo);
        }
        
        // Select the next account (or first if none selected)
        const nextIndex = (currentIndex + 1) % accounts.length;
        const nextAccount = accounts[nextIndex];
        
        // Update the selected account in localStorage
        const accountData = {
            accountNo: nextAccount.accountNo,
            accountNickname: nextAccount.accountNickname
        };
        
        localStorage.setItem('selectedAccount', JSON.stringify(accountData));
        
        // Update the balance display
        await loadSelectedAccountBalance();
        
        showToast(`Switched to ${nextAccount.accountNickname || nextAccount.accountNo}`, 'success');
    });
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
        showToast('You have no new notifications', 'info');
    });
}

const profileAvatar = document.querySelector('.profile-avatar');
if (profileAvatar) {
    profileAvatar.addEventListener('click', () => {
        showToast('Profile clicked', 'info');
    });
}
