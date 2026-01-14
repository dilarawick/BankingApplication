// Smart Spend JavaScript functionality
(function(){
    // DOM Elements
    const budgetForm = document.getElementById('budgetForm');
    const accountNoSelect = document.getElementById('accountNo');
    const budgetLimitInput = document.getElementById('budgetLimit');
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const budgetLimitDisplay = document.getElementById('budgetLimitDisplay');
    const totalSpentDisplay = document.getElementById('totalSpentDisplay');
    const remainingDisplay = document.getElementById('remainingDisplay');
    const transactionsBody = document.getElementById('transactionsBody');
    const circle = document.querySelector('.circle');
    const percentageText = document.querySelector('.percentage');

    // Initialize the page
    document.addEventListener('DOMContentLoaded', function() {
        loadAccounts();
        loadCurrentBudget();
        setupEventListeners();
        
        // Set default dates (first and last day of current month)
        const today = new Date();
        const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
        const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
        
        startDateInput.valueAsDate = firstDay;
        endDateInput.valueAsDate = lastDay;
    });

    function setupEventListeners() {
        // Handle form submission
        budgetForm.addEventListener('submit', handleBudgetSubmit);
        
        // Listen for account selection changes
        accountNoSelect.addEventListener('change', function() {
            // Save the selected account to localStorage
            if (this.value) {
                const selectedOption = this.options[this.selectedIndex];
                localStorage.setItem('selectedAccount', JSON.stringify({
                    accountNo: this.value,
                    accountLabel: selectedOption.text
                }));
            }
            
            // Reload budget for the selected account
            loadCurrentBudget();
        });
    }

    // Load user's accounts into the dropdown
    async function loadAccounts() {
        try {
            const response = await fetch('/api/accounts/my-accounts', {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to load accounts');
            }
            
            const accounts = await response.json();
            accountNoSelect.innerHTML = '<option value="">Select an account</option>';
            
            accounts.forEach(account => {
                const option = document.createElement('option');
                option.value = account.accountNo;
                option.textContent = `${account.accountNo} - ${account.accountType} (${account.nickname || 'No Nickname'})`;
                accountNoSelect.appendChild(option);
            });
        } catch (error) {
            console.error('Error loading accounts:', error);
            showNotification('Failed to load accounts', 'error');
        }
    }

    // Load current budget for the selected account
    async function loadCurrentBudget() {
        try {
            // Get the currently selected account from localStorage (if any)
            const selectedAccount = localStorage.getItem('selectedAccount');
            let accountNo = null;
            
            if (selectedAccount) {
                const parsed = JSON.parse(selectedAccount);
                accountNo = parsed.accountNo;
            }
            
            // If no account is selected, use the first account in the dropdown
            if (!accountNo && accountNoSelect.options.length > 1) {
                accountNo = accountNoSelect.options[1].value;
                accountNoSelect.value = accountNo;
            }
            
            // Fetch budget for the selected account
            let url = '/api/smartspend/budget';
            if (accountNo) {
                url += `?accountNo=${encodeURIComponent(accountNo)}`;
            }
            
            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to load budget');
            }
            
            const budgetData = await response.json();
            
            if (budgetData.hasBudget) {
                // Populate form fields
                accountNoSelect.value = budgetData.accountNo;
                budgetLimitInput.value = parseFloat(budgetData.budgetLimit).toFixed(2);
                
                // Set dates
                startDateInput.value = budgetData.startDate;
                endDateInput.value = budgetData.endDate;
                
                // Update budget display
                updateBudgetDisplay(budgetData);
            } else {
                // Clear the form if no budget exists
                budgetLimitInput.value = '';
                updateBudgetDisplay(null);
            }
        } catch (error) {
            console.error('Error loading budget:', error);
            showNotification('Failed to load budget', 'error');
        }
    }

    // Handle budget form submission
    async function handleBudgetSubmit(event) {
        event.preventDefault();
        
        const accountNo = accountNoSelect.value;
        const budgetLimit = parseFloat(budgetLimitInput.value);
        const startDate = startDateInput.value;
        const endDate = endDateInput.value;
        
        // Validate inputs
        if (!accountNo) {
            showNotification('Please select an account', 'error');
            return;
        }
        
        if (!budgetLimit || budgetLimit <= 0) {
            showNotification('Please enter a valid budget limit', 'error');
            return;
        }
        
        if (!startDate || !endDate) {
            showNotification('Please select both start and end dates', 'error');
            return;
        }
        
        try {
            const response = await fetch('/api/smartspend/budget', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                },
                body: JSON.stringify({
                    accountNo: accountNo,
                    budgetLimit: budgetLimit,
                    startDate: startDate,
                    endDate: endDate
                })
            });
            
            const result = await response.json();
            
            if (response.ok) {
                showNotification('Budget set successfully!', 'success');
                updateBudgetDisplay(result);
            } else {
                showNotification(result.message || 'Failed to set budget', 'error');
            }
        } catch (error) {
            console.error('Error setting budget:', error);
            showNotification('An error occurred while setting the budget', 'error');
        }
    }

    // Update the budget display with current data
    function updateBudgetDisplay(budgetData) {
        if (budgetData && budgetData.hasBudget) {
            // Update displayed values
            budgetLimitDisplay.textContent = parseFloat(budgetData.budgetLimit).toLocaleString('en-LK', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });
            
            totalSpentDisplay.textContent = parseFloat(budgetData.totalSpent).toLocaleString('en-LK', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });
            
            remainingDisplay.textContent = parseFloat(budgetData.remaining).toLocaleString('en-LK', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });
            
            // Update circular progress
            const percentage = parseFloat(budgetData.percentageUsed).toFixed(1);
            percentageText.textContent = `${percentage}%`;
            
            // Update the horizontal percentage display
            document.querySelector('.percentage-display').textContent = `${percentage}%`;
            
            // Calculate stroke dasharray for the circle (0-100%)
            const circumference = 283; // Approximate circumference for radius 45 circle (2 * π * r ≈ 283)
            const offset = circumference - (percentage / 100) * circumference;
            
            circle.style.strokeDasharray = `${circumference} ${circumference}`;
            circle.style.strokeDashoffset = offset;
            
            // Remove existing color classes
            circle.classList.remove('warning', 'exhausted');
            
            // Change color based on percentage using new navy blue theme
            if (percentage >= 100) {
                circle.classList.add('exhausted');
                circle.style.stroke = '#EF4444'; // Red for exhausted
            } else if (percentage >= 80) {
                circle.classList.add('warning');
                circle.style.stroke = '#F59E0B'; // Yellow for warning
            } else {
                circle.style.stroke = '#3B82F6'; // Blue for normal
            }
            
            // Add appropriate classes to stat elements for themed borders
            const statElements = document.querySelectorAll('.stat');
            statElements.forEach(stat => stat.classList.remove('budget-limit', 'total-spent', 'remaining'));
            document.querySelector('.stat:nth-child(1)').classList.add('budget-limit');
            document.querySelector('.stat:nth-child(2)').classList.add('total-spent');
            document.querySelector('.stat:nth-child(3)').classList.add('remaining');
            
            // Check for alerts based on budget status
            checkBudgetAlerts(budgetData.alertStatus);
            
            // Load recent transactions
            loadRecentTransactions(budgetData.budgetId);
        } else {
            // Reset display if no budget
            budgetLimitDisplay.textContent = '0.00';
            totalSpentDisplay.textContent = '0.00';
            remainingDisplay.textContent = '0.00';
            percentageText.textContent = '0%';
            
            // Reset circle
            circle.style.strokeDasharray = '100 100';
            circle.style.strokeDashoffset = 100;
            circle.style.stroke = '#10B981'; // Green
            
            // Clear transactions
            transactionsBody.innerHTML = '<tr><td colspan="3">No transactions yet</td></tr>';
        }
    }

    // Check for budget alerts based on status
    function checkBudgetAlerts(alertStatus) {
        // Clear any existing budget alerts before showing new ones
        const notificationBanner = document.getElementById('notificationBanner');
        if (notificationBanner) {
            notificationBanner.style.display = 'none';
        }
        
        switch (alertStatus) {
            case 'INITIAL':
                // 0% - Budget set but no spending yet (initial state)
                break;
            case 'WARNING':
                // 80% - Warning alert when 80% of budget is consumed (heads-up notification)
                showNotification('You have used 80% of your budget! Be mindful of your spending.', 'warning');
                break;
            case 'EXHAUSTED':
                // 100% - Alert when budget is completely exhausted (budget reached notification)
                showNotification('Your budget has been completely used! Consider adjusting your budget or reducing expenses.', 'error');
                break;
            default:
                // ACTIVE or other status - no special alert needed
                break;
        }
    }

    // Load recent transactions for the budget
    async function loadRecentTransactions(budgetId) {
        try {
            // Get the account number from the currently selected option
            const accountNo = accountNoSelect.value;
            
            let url = `/api/smartspend/transactions?budgetId=${budgetId}`;
            if (accountNo) {
                url += `&accountNo=${encodeURIComponent(accountNo)}`;
            }
            
            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to load transactions');
            }
            
            const transactions = await response.json();
            
            if (transactions.length > 0) {
                transactionsBody.innerHTML = '';
                
                transactions.forEach(transaction => {
                    const row = document.createElement('tr');
                    
                    // Format date
                    const transactionDate = new Date(transaction.transactionDate);
                    const formattedDate = transactionDate.toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric'
                    });
                    
                    // Format amount
                    const formattedAmount = parseFloat(transaction.transactionAmount).toLocaleString('en-LK', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2
                    });
                    
                    row.innerHTML = `
                        <td>${formattedDate}</td>
                        <td>${transaction.description || 'N/A'}</td>
                        <td>LKR ${formattedAmount}</td>
                    `;
                    
                    transactionsBody.appendChild(row);
                });
            } else {
                transactionsBody.innerHTML = '<tr><td colspan="3">No transactions yet</td></tr>';
            }
        } catch (error) {
            console.error('Error loading transactions:', error);
            transactionsBody.innerHTML = '<tr><td colspan="3">Failed to load transactions</td></tr>';
        }
    }

    // Show notification using the existing function
    function showNotification(message, type) {
        // Try to use the new notification system first
        const notificationBanner = document.getElementById('notificationBanner');
        const notificationText = document.getElementById('notificationText');
        const closeNotification = document.getElementById('closeNotification');
        
        if (notificationBanner && notificationText) {
            notificationText.textContent = message;
            
            // Set appropriate styling based on type
            if (type === 'success') {
                notificationBanner.style.background = '#10b981'; // Green for success
            } else if (type === 'error') {
                notificationBanner.style.background = '#ef4444'; // Red for error
            } else if (type === 'warning') {
                notificationBanner.style.background = '#f59e0b'; // Yellow for warning
            } else {
                notificationBanner.style.background = '#3b82f6'; // Blue for other types
            }
            
            // Show the notification
            notificationBanner.style.display = 'block';
            
            // Auto-hide after 5 seconds
            setTimeout(() => {
                if (notificationBanner.style.display === 'block') {
                    notificationBanner.style.display = 'none';
                }
            }, 5000);
        } else {
            // Fallback to toast notification if banner is not available
            showToast(message, type);
        }
        
        // Set up close button functionality
        if (closeNotification) {
            closeNotification.onclick = function() {
                if (notificationBanner) {
                    notificationBanner.style.display = 'none';
                }
            };
        }
    }
})();