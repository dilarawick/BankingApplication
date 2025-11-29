// Bank Transfer JavaScript with Backend Integration
const API_BASE_URL = '/api/transactions';

let transferData = {};
let userBalance = 0;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    loadBankList();
    loadUserBalance();
    setupFormHandler();
    setupLogout();
    
    // Debug: Log when page is loaded
    console.log('Bank transfer page loaded');
});

// Load available banks from backend
async function loadBankList() {
    try {
        console.log('Loading banks from:', `${API_BASE_URL}/banks`);
        const response = await fetch(`${API_BASE_URL}/banks`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        console.log('Bank API response status:', response.status);
        
        if (response.ok) {
            const banks = await response.json();
            console.log('Banks loaded:', banks);
            const select = document.getElementById('bankSelect');
            
            // Clear existing options except the first one
            select.innerHTML = '<option value="">Select a bank...</option>';
            
            banks.forEach(bank => {
                const option = document.createElement('option');
                option.value = bank;
                option.textContent = bank.replace(/_/g, ' ');
                select.appendChild(option);
            });
            console.log('Bank dropdown populated with', banks.length, 'banks');
        } else if (response.status === 401) {
            console.warn('User not authenticated, showing default banks');
            // Add some default banks for testing when not authenticated
            const select = document.getElementById('bankSelect');
            const defaultBanks = ['NOVA_BANK', 'STATE_BANK', 'CITY_BANK', 'NATIONAL_BANK'];
            defaultBanks.forEach(bank => {
                const option = document.createElement('option');
                option.value = bank;
                option.textContent = bank.replace(/_/g, ' ');
                select.appendChild(option);
            });
        } else {
            console.error('Failed to load banks:', response.status);
            // Add some default banks for testing
            const select = document.getElementById('bankSelect');
            const defaultBanks = ['NOVA_BANK', 'STATE_BANK', 'CITY_BANK'];
            defaultBanks.forEach(bank => {
                const option = document.createElement('option');
                option.value = bank;
                option.textContent = bank.replace(/_/g, ' ');
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading banks:', error);
        // Add some default banks for testing
        const select = document.getElementById('bankSelect');
        const defaultBanks = ['NOVA_BANK', 'STATE_BANK', 'CITY_BANK'];
        defaultBanks.forEach(bank => {
            const option = document.createElement('option');
            option.value = bank;
            option.textContent = bank.replace(/_/g, ' ');
            select.appendChild(option);
        });
    }
}

// Load user's current balance
async function loadUserBalance() {
    try {
        const response = await fetch(`${API_BASE_URL}/balance`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            userBalance = data.balance;
        }
    } catch (error) {
        console.error('Error loading balance:', error);
    }
}

// Setup form submission handler
function setupFormHandler() {
    const form = document.getElementById('bankTransferForm');
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // Collect form data
        transferData = {
            amount: parseFloat(document.getElementById('amount').value),
            recipientAccountNumber: document.getElementById('accountNumber').value,
            recipientAccountName: document.getElementById('accountName').value,
            recipientBank: document.getElementById('bankSelect').value,
            description: document.getElementById('description').value
        };
        
        // Show confirmation step
        showConfirmation();
    });
    
    // Setup transfer button
    document.getElementById('transferBtn').addEventListener('click', processTransfer);
}

// Show confirmation screen
function showConfirmation() {
    document.getElementById('confirmAmount').textContent = transferData.amount.toFixed(2);
    document.getElementById('confirmRecipient').textContent = transferData.recipientAccountName;
    
    hideAllSteps();
    document.getElementById('confirmationStep').classList.add('active');
}

// Process the transfer
async function processTransfer() {
    const btn = document.getElementById('transferBtn');
    btn.disabled = true;
    btn.textContent = 'PROCESSING...';
    
    try {
        const response = await fetch(`${API_BASE_URL}/transfer`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(transferData)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showSuccess(data);
        } else {
            showError(data);
        }
    } catch (error) {
        showError({
            message: 'Transaction Failed',
            details: 'Network error. Please try again.'
        });
    } finally {
        btn.disabled = false;
        btn.textContent = 'TRANSFER';
    }
}

// Show success screen
function showSuccess(data) {
    document.getElementById('transactionId').textContent = data.transactionId || 'N/A';
    hideAllSteps();
    document.getElementById('successStep').classList.add('active');
}

// Show error screen
function showError(data) {
    document.getElementById('errorMessage').textContent = data.message || 'Transaction Failed';
    document.getElementById('errorDetails').textContent = data.details || data.error || '';
    hideAllSteps();
    document.getElementById('errorStep').classList.add('active');
}

// Hide all steps
function hideAllSteps() {
    document.querySelectorAll('.transfer-step').forEach(step => {
        step.classList.remove('active');
    });
}

// Setup logout functionality
function setupLogout() {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            // Clear session and redirect to login
            window.location.href = 'login.html';
        });
    }
}