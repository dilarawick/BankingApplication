// Manual Expenses JavaScript functionality
(function(){
    // DOM Elements
    const monthlyBudgetForm = document.getElementById('monthlyBudgetForm');
    const manualExpenseForm = document.getElementById('manualExpenseForm');
    const manualExpensesBody = document.getElementById('manualExpensesBody');
    const cancelExpenseBtn = document.getElementById('cancelExpenseBtn');
    
    // Category chart elements
    const categoryCharts = document.querySelectorAll('.circular-progress');
    
    // Initialize the page
    document.addEventListener('DOMContentLoaded', function() {
        loadCurrentBudget();
        loadManualExpenses();
        setupEventListeners();
        
        // Set default date to today
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('expenseDate').value = today;
        document.getElementById('budgetStartDate').value = today;
        
        // Set end date to end of current month
        const endDate = new Date();
        endDate.setMonth(endDate.getMonth() + 1);
        endDate.setDate(0); // Last day of the month
        document.getElementById('budgetEndDate').value = endDate.toISOString().split('T')[0];
    });

    function setupEventListeners() {
        // Handle monthly budget form submission
        monthlyBudgetForm.addEventListener('submit', handleMonthlyBudgetSubmit);
        
        // Handle manual expense form submission
        manualExpenseForm.addEventListener('submit', handleManualExpenseSubmit);
        
        // Handle cancel button
        cancelExpenseBtn.addEventListener('click', resetManualExpenseForm);
    }

    // Load current budget for the user
    async function loadCurrentBudget() {
        try {
            // First, get the user's accounts
            const accountsResponse = await fetch('/api/accounts/my-accounts', {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!accountsResponse.ok) {
                throw new Error('Failed to load accounts');
            }
            
            const accounts = await accountsResponse.json();
            
            if (accounts.length === 0) {
                console.log('No accounts found');
                return;
            }
            
            // Use the first account to get the budget
            const accountNo = accounts[0].accountNo;
            
            const response = await fetch(`/api/smartspend/budget?accountNo=${accountNo}`, {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to load budget');
            }
            
            const budgetData = await response.json();
            
            if (budgetData.hasBudget) {
                document.getElementById('monthlyBudgetLimit').value = parseFloat(budgetData.budgetLimit).toFixed(2);
                document.getElementById('budgetStartDate').value = budgetData.startDate;
                document.getElementById('budgetEndDate').value = budgetData.endDate;
            }
        } catch (error) {
            console.error('Error loading budget:', error);
        }
    }

    // Handle monthly budget form submission
    async function handleMonthlyBudgetSubmit(event) {
        event.preventDefault();
        
        const budgetLimit = parseFloat(document.getElementById('monthlyBudgetLimit').value);
        const startDate = document.getElementById('budgetStartDate').value;
        const endDate = document.getElementById('budgetEndDate').value;
        
        // Validate inputs
        if (!budgetLimit || budgetLimit <= 0) {
            showNotification('Please enter a valid budget limit', 'error');
            return;
        }
        
        if (!startDate || !endDate) {
            showNotification('Please select both start and end dates', 'error');
            return;
        }
        
        try {
            // First, get the user's accounts to use one for the budget
            const accountsResponse = await fetch('/api/accounts/my-accounts', {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!accountsResponse.ok) {
                throw new Error('Failed to load accounts');
            }
            
            const accounts = await accountsResponse.json();
            
            if (accounts.length === 0) {
                showNotification('No accounts found. Please add an account first.', 'error');
                return;
            }
            
            // Use the first account for the budget
            const accountNo = accounts[0].accountNo;
            
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
                showNotification('Monthly budget set successfully!', 'success');
                updateBudgetDisplay(result);
            } else {
                showNotification(result.message || 'Failed to set budget', 'error');
            }
        } catch (error) {
            console.error('Error setting budget:', error);
            showNotification('An error occurred while setting the budget', 'error');
        }
    }

    // Handle manual expense form submission
    async function handleManualExpenseSubmit(event) {
        event.preventDefault();
        
        const amount = parseFloat(document.getElementById('expenseAmount').value);
        const category = document.getElementById('expenseCategory').value;
        const date = document.getElementById('expenseDate').value;
        const description = document.getElementById('expenseDescription').value;
        const paymentType = document.querySelector('input[name="paymentType"]:checked').value;
        
        // Validate inputs
        if (!amount || amount <= 0) {
            showNotification('Please enter a valid amount', 'error');
            return;
        }
        
        if (!category) {
            showNotification('Please select a category', 'error');
            return;
        }
        
        if (!date) {
            showNotification('Please select a date', 'error');
            return;
        }
        
        try {
            // Create expense object
            const expenseData = {
                amount: amount,
                category: category,
                date: date,
                description: description || '',
                paymentType: paymentType
            };
            
            // For now, we'll store locally and simulate saving to backend
            // In a real implementation, this would be sent to the backend
            saveManualExpense(expenseData);
            
            showNotification('Manual expense saved successfully!', 'success');
            resetManualExpenseForm();
            loadManualExpenses();
            updateBudgetCharts();
        } catch (error) {
            console.error('Error saving expense:', error);
            showNotification('An error occurred while saving the expense', 'error');
        }
    }

    // Save manual expense to backend
    async function saveManualExpense(expenseData) {
        try {
            const response = await fetch('/api/smartspend/manual-expense', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                },
                body: JSON.stringify({
                    amount: expenseData.amount,
                    description: expenseData.description,
                    category: expenseData.category,
                    paymentType: expenseData.paymentType
                })
            });
            
            const result = await response.json();
            
            if (response.ok) {
                showNotification('Manual expense saved successfully!', 'success');
                updateBudgetCharts(); // Update charts after adding expense
            } else {
                showNotification(result.message || 'Failed to save expense', 'error');
            }
        } catch (error) {
            console.error('Error saving expense:', error);
            showNotification('An error occurred while saving the expense', 'error');
        }
    }

    // Load manual expenses
    async function loadManualExpenses() {
        try {
            // First get the user's accounts
            const accountsResponse = await fetch('/api/accounts/my-accounts', {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!accountsResponse.ok) {
                throw new Error('Failed to load accounts');
            }
            
            const accounts = await accountsResponse.json();
            
            if (accounts.length === 0) {
                manualExpensesBody.innerHTML = '<tr><td colspan="5">No accounts found. Please add an account first.</td></tr>';
                return;
            }
            
            // Use the first account to get the budget
            const accountNo = accounts[0].accountNo;
            
            // First get the active budget to get the budget ID
            const budgetResponse = await fetch(`/api/smartspend/budget?accountNo=${accountNo}`, {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!budgetResponse.ok) {
                const budgetData = await budgetResponse.json();
                if (budgetResponse.status === 400 && budgetData.message && budgetData.message.includes('Account number is required')) {
                    // This happens when no account is specified, which is expected when no budget exists yet
                    manualExpensesBody.innerHTML = '<tr><td colspan="5">No budget set. Please set a budget first.</td></tr>';
                    return;
                }
                throw new Error('Failed to get budget');
            }
            
            const budgetData = await budgetResponse.json();
            
            if (!budgetData.hasBudget) {
                manualExpensesBody.innerHTML = '<tr><td colspan="5">No budget set. Please set a budget first.</td></tr>';
                return;
            }
            
            // Now get the manual expenses for this budget
            const response = await fetch(`/api/smartspend/manual-expenses?budgetId=${budgetData.budgetId}`, {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to load manual expenses');
            }
            
            const expenses = await response.json();
            
            if (expenses.length > 0) {
                manualExpensesBody.innerHTML = '';
                
                expenses.forEach(expense => {
                    const row = document.createElement('tr');
                    
                    // Format date
                    const expenseDate = new Date(expense.transactionDate);
                    const formattedDate = expenseDate.toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric'
                    });
                    
                    // Format amount
                    const formattedAmount = parseFloat(expense.transactionAmount).toLocaleString('en-LK', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2
                    });
                    
                    row.innerHTML = `
                        <td>${formattedDate}</td>
                        <td>${expense.description || 'N/A'}</td>
                        <td>${expense.category}</td>
                        <td>LKR ${formattedAmount}</td>
                        <td>${expense.paymentType}</td>
                    `;
                    
                    manualExpensesBody.appendChild(row);
                });
            } else {
                manualExpensesBody.innerHTML = '<tr><td colspan="5">No manual expenses yet</td></tr>';
            }
        } catch (error) {
            console.error('Error loading manual expenses:', error);
            manualExpensesBody.innerHTML = '<tr><td colspan="5">Failed to load expenses</td></tr>';
            // Don't show notification for this error to avoid spam
        }
    }

    // Update budget display with new budget data
    function updateBudgetDisplay(budgetData) {
        // This would update the budget tracking section
        // For now, we'll just update the charts
        updateBudgetCharts();
    }

    // Update budget charts
    async function updateBudgetCharts() {
        try {
            // First get the user's accounts
            const accountsResponse = await fetch('/api/accounts/my-accounts', {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!accountsResponse.ok) {
                throw new Error('Failed to load accounts');
            }
            
            const accounts = await accountsResponse.json();
            
            if (accounts.length === 0) {
                console.log('No accounts found');
                return;
            }
            
            // Use the first account to get the budget
            const accountNo = accounts[0].accountNo;
            
            // Get the active budget to get the budget ID
            const budgetResponse = await fetch(`/api/smartspend/budget?accountNo=${accountNo}`, {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                }
            });
            
            if (!budgetResponse.ok) {
                const budgetData = await budgetResponse.json();
                if (budgetResponse.status === 400 && budgetData.message && budgetData.message.includes('Account number is required')) {
                    // This happens when no account is specified, which is expected when no budget exists yet
                    console.log('No budget set, skipping chart update');
                    return;
                }
                throw new Error('Failed to get budget');
            }
            
            const budgetData = await budgetResponse.json();
            
            if (!budgetData.hasBudget) {
                console.log('No budget set, skipping chart update');
                return;
            }
            
            const budgetId = budgetData.budgetId;
            const categories = ['Food', 'Clothes', 'Transportation', 'Other'];
            
            // Update each category chart
            for (const category of categories) {
                const response = await fetch(`/api/smartspend/budget-progress-by-category?budgetId=${budgetId}&category=${category}`, {
                    headers: {
                        'Authorization': `Bearer ${sessionStorage.getItem('token')}`
                    }
                });
                
                if (response.ok) {
                    const categoryData = await response.json();
                    updateChartForCategory(
                        category, 
                        categoryData.percentageUsed, 
                        categoryData.totalSpent, 
                        categoryData.categoryLimit
                    );
                }
            }
        } catch (error) {
            console.error('Error updating budget charts:', error);
        }
    }

    // Update chart for a specific category
    function updateChartForCategory(categoryName, percentage, spent, limit) {
        // Find the chart element for this category
        const chartElement = document.querySelector(`.circular-progress[data-category="${categoryName}"]`);
        if (!chartElement) return;
        
        // Calculate remaining
        const remaining = Math.max(0, limit - spent);
        
        // Update the percentage text in the chart
        const percentageText = chartElement.querySelector('.percentage-text');
        if (percentageText) {
            percentageText.textContent = `${percentage.toFixed(1)}%`;
        }
        
        // Update the adjacent percentage display
        const progressPercentage = chartElement.closest('.chart-container').querySelector('.progress-percentage');
        if (progressPercentage) {
            progressPercentage.textContent = `${percentage.toFixed(1)}%`;
        }
        
        // Update the budget stats
        document.getElementById(`${categoryName.toLowerCase()}BudgetLimit`).textContent = 
            limit.toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        document.getElementById(`${categoryName.toLowerCase()}TotalSpent`).textContent = 
            spent.toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        document.getElementById(`${categoryName.toLowerCase()}Remaining`).textContent = 
            remaining.toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        
        // Calculate the stroke offset for the donut chart
        const circumference = 283; // Approximate circumference for radius 45 circle
        const offset = circumference - (percentage / 100) * circumference;
        
        // Update the circle stroke
        const circle = chartElement.querySelector('.circle');
        if (circle) {
            circle.style.strokeDasharray = `${circumference} ${circumference}`;
            circle.style.strokeDashoffset = offset;
        }
    }

    // Reset manual expense form
    function resetManualExpenseForm() {
        manualExpenseForm.reset();
        
        // Reset to default date
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('expenseDate').value = today;
        
        // Reset payment type to Cash
        document.querySelector('input[name="paymentType"][value="Cash"]').checked = true;
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
    
    // Fallback toast function
    function showToast(message, type) {
        const toast = document.getElementById('toast');
        if (toast) {
            toast.textContent = message;
            toast.className = 'toast active ' + type;
            
            setTimeout(() => {
                toast.className = 'toast';
            }, 3000);
        }
    }
})();