// Load accounts when the page loads
document.addEventListener('DOMContentLoaded', function() {
    loadAccounts();
});

// Load all accounts for the customer
async function loadAccounts() {
    try {
        const response = await fetch('/api/accounts/my-accounts', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const accounts = await response.json();
            const accountSelector = document.getElementById('accountSelector');
            
            // Clear existing options except the first placeholder
            accountSelector.innerHTML = '<option value="">Select Account</option>';
            
            accounts.forEach(account => {
                const option = document.createElement('option');
                option.value = account.accountNo;
                option.textContent = `${account.accountNickname} (${account.accountNo}) - ${account.accountType}`;
                accountSelector.appendChild(option);
            });
        } else {
            const errorData = await response.json();
            console.error('Failed to load accounts:', errorData);
            showToast(errorData.message || 'Failed to load accounts', 'error');
        }
    } catch (error) {
        console.error('Error loading accounts:', error);
        showToast('Error loading accounts: ' + error.message, 'error');
    }
}

// Load e-statement for the selected account
async function loadEStatement() {
    const accountNo = document.getElementById('accountSelector').value;
    
    if (!accountNo) {
        // Clear the statement if no account is selected
        clearStatement();
        return;
    }

    try {
        const response = await fetch(`/api/estatement/account/${accountNo}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const statement = await response.json();
            displayEStatement(statement);
        } else {
            const errorData = await response.json();
            console.error('Failed to load e-statement:', errorData);
            showToast(errorData.message || 'Failed to load e-statement', 'error');
        }
    } catch (error) {
        console.error('Error loading e-statement:', error);
        showToast('Error loading e-statement: ' + error.message, 'error');
    }
}

// Display the e-statement data
function displayEStatement(statement) {
    // Populate header information
    document.getElementById('accountHolder').textContent = statement.accountHolder;
    document.getElementById('accountNumber').textContent = statement.accountNumber;
    document.getElementById('accountType').textContent = statement.accountType;
    document.getElementById('branch').textContent = statement.branch;
    document.getElementById('statementPeriod').textContent = statement.statementPeriod;

    // Populate account summary cards
    document.getElementById('openingBalance').textContent = formatCurrency(statement.accountSummary.openingBalance);
    document.getElementById('totalCredits').textContent = formatCurrency(statement.accountSummary.totalCredits);
    document.getElementById('totalDebits').textContent = formatCurrency(statement.accountSummary.totalDebits);
    document.getElementById('closingBalance').textContent = formatCurrency(statement.accountSummary.closingBalance);
    
    // Clear and populate transaction details
    const transactionsBody = document.getElementById('transactions-body');
    transactionsBody.innerHTML = '';
    
    statement.transactionDetails.forEach(transaction => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${transaction.date}</td>
            <td>${transaction.transactionId}</td>
            <td>${transaction.description}</td>
            <td>${transaction.type}</td>
            <td>${formatCurrency(transaction.amount)}</td>
            <td>${formatCurrency(transaction.balance)}</td>
        `;
        transactionsBody.appendChild(tr);
    });
}

// Clear the statement display
function clearStatement() {
    document.getElementById('accountHolder').textContent = '';
    document.getElementById('accountNumber').textContent = '';
    document.getElementById('accountType').textContent = '';
    document.getElementById('branch').textContent = '';
    document.getElementById('statementPeriod').textContent = '';

    document.getElementById('summary-body').innerHTML = '';
    document.getElementById('transactions-body').innerHTML = '';
}

// Format currency with commas and 2 decimal places
function formatCurrency(amount) {
    return parseFloat(amount).toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Generate and email the current statement
async function generateAndEmailStatement() {
    const accountNo = document.getElementById('accountSelector').value;
    
    if (!accountNo) {
        showToast('Please select an account first', 'error');
        return;
    }
    
    const generateBtn = document.getElementById('generateEmailBtn');
    const originalText = generateBtn.innerHTML;
    
    // Show loading state
    generateBtn.innerHTML = '<span class="btn-icon">⏳</span> Processing...';
    generateBtn.disabled = true;
    
    try {
        // Get the current statement data
        const response = await fetch(`/api/estatement/account/${accountNo}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to retrieve statement data');
        }
        
        const statement = await response.json();
        
        // Send the statement data to the email endpoint
        const emailResponse = await fetch('/api/estatement/email', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                accountNo: accountNo,
                statement: statement
            })
        });
        
        if (emailResponse.ok) {
            showToast('E-statement sent to your registered email successfully!', 'success');
        } else {
            const errorData = await emailResponse.json();
            console.error('Failed to send email:', errorData);
            throw new Error(errorData.message || 'Failed to send email');
        }
    } catch (error) {
        console.error('Error generating and emailing statement:', error);
        showToast(`Error: ${error.message || 'Failed to send e-statement'}`, 'error');
    } finally {
        // Restore original button state
        generateBtn.innerHTML = originalText;
        generateBtn.disabled = false;
    }
}