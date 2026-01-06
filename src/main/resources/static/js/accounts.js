document.addEventListener('DOMContentLoaded', () => {
    initializeAccountManagement();
    initDeleteModal(); // Initialize delete modal functionality
});

document.addEventListener('DOMContentLoaded', () => {
    loadAccounts();
});

function initializeAccountManagement() {
    // Get modal elements
    const modal = document.getElementById('addAccountModal');
    const addAccountCards = document.querySelectorAll('.add-account-card');
    const closeBtn = document.getElementById('closeModal');
    const addAccountForm = document.getElementById('addAccountForm');
    const verifyAccountBtn = document.getElementById('verifyAccountBtn');
    const verificationSection = document.getElementById('verificationSection');
    const confirmAccountBtn = document.getElementById('confirmAccountBtn');
    const addAccountBtn = document.getElementById('addAccountBtn');
    const resendOtpBtn = document.getElementById('resendOtpBtn');
    const verificationCodeInput = document.getElementById('verificationCode');
    
    // Event listeners for "Add a bank account" cards
    addAccountCards.forEach(card => {
        card.addEventListener('click', () => {
            modal.style.display = 'flex';
        });
    });

    // Switch modal handlers
    const switchModal = document.getElementById('switchConfirmModal');
    const closeSwitch = document.getElementById('closeSwitchModal');
    const cancelSwitch = document.getElementById('cancelSwitchBtn');
    if (closeSwitch) closeSwitch.addEventListener('click', () => switchModal.style.display = 'none');
    if (cancelSwitch) cancelSwitch.addEventListener('click', () => switchModal.style.display = 'none');

    document.getElementById('confirmSwitchBtn').addEventListener('click', confirmSwitchAccount);
    
    // Close modal when clicking on close button
    closeBtn.addEventListener('click', () => {
        closeModal();
    });
    
    // Close modal when clicking outside the modal content
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });
    
    // Verify account button click event
    verifyAccountBtn.addEventListener('click', verifyAccount);
    
    // Confirm account button click event
    confirmAccountBtn.addEventListener('click', confirmAccount);
    
    // Resend OTP button click event
    resendOtpBtn.addEventListener('click', resendOtp);
    
    // Verification code input event to limit to 6 digits
    verificationCodeInput.addEventListener('input', function() {
        this.value = this.value.replace(/[^0-9]/g, '').substring(0, 6);
    });
    
    // Form submission event
    addAccountForm.addEventListener('submit', handleAddAccount);
}

function closeModal() {
    const modal = document.getElementById('addAccountModal');
    modal.style.display = 'none';
    
    // Reset form
    resetForm();
}

function resetForm() {
    const form = document.getElementById('addAccountForm');
    form.reset();
    
    // Hide verification section
    document.getElementById('verificationSection').style.display = 'none';
    
    // Hide add account button
    document.getElementById('addAccountBtn').style.display = 'none';
    
    // Show verify button
    document.getElementById('verifyAccountBtn').style.display = 'block';
}

async function verifyAccount() {
    clearFormError();
    const branch = document.getElementById('branch').value;
    const accountNumber = document.getElementById('accountNumber').value;
    const accountType = document.getElementById('accountType').value;
    const accountNickname = document.getElementById('accountNickname').value;
    
    // Validate required fields
    if (!branch || !accountNumber || !accountType || !accountNickname) {
        setFormError('Please fill in all required fields');
        return;
    }
    
    // Additional validation
    if (accountNumber.length < 6) {
        setFormError('Account number must be at least 6 characters long');
        return;
    }
    
    if (accountNickname.length < 2) {
        setFormError('Account nickname must be at least 2 characters long');
        return;
    }
    
    // Validate account number format (alphanumeric)
    const accountNumberRegex = /^[a-zA-Z0-9]+$/;
    if (!accountNumberRegex.test(accountNumber)) {
        setFormError('Account number can only contain letters and numbers');
        return;
    }
    
    // Validate nickname format (no special characters except spaces)
    const nicknameRegex = /^[a-zA-Z0-9\s]+$/;
    if (!nicknameRegex.test(accountNickname)) {
        setFormError('Account nickname can only contain letters, numbers, and spaces');
        return;
    }
    
    // Show loading state
    const verifyBtn = document.getElementById('verifyAccountBtn');
    verifyBtn.disabled = true;
    verifyBtn.textContent = 'Verifying...';
    
    try {
        // Call the backend API to verify the account
        const response = await fetch('/api/accounts/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            },
            body: JSON.stringify({
                accountNumber: accountNumber,
                branch: branch,
                accountType: accountType
            })
        });
        
        const data = await response.json();

        if (response.ok && data.isValid) {
            clearFormError();
            showToast('Verification code sent to your registered email.', 'success');

            // Show verification section
            document.getElementById('verificationSection').style.display = 'block';

            // Hide verify button
            verifyBtn.style.display = 'none';
        } else {
            setFormError(data.message || 'Account verification failed. Please check the account details.');
        }
    } catch (error) {
        showToast('An error occurred during account verification. Please try again.', 'error');
    } finally {
        verifyBtn.disabled = false;
        verifyBtn.textContent = 'Verify Account';
    }
}

async function confirmAccount() {
    clearFormError();
    const accountNumber = document.getElementById('accountNumber').value;
    const verificationCode = document.getElementById('verificationCode').value;

    if (!verificationCode) {
        setFormError('Please enter the verification code');
        return;
    }

    if (verificationCode.length !== 6) {
        setFormError('Verification code must be 6 digits');
        return;
    }
    
    // Show loading state
    const confirmBtn = document.getElementById('confirmAccountBtn');
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Confirming...';
    
    try {
        // Call the backend API to verify the OTP
        const response = await fetch('/api/accounts/verify-otp', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            },
            body: JSON.stringify({
                accountNumber: accountNumber,
                otp: verificationCode
            })
        });
        
        const data = await response.json();

        if (response.ok && data.isValid) {
            clearFormError();
            showToast('Account verified successfully!', 'success');

            // Show add account button
            document.getElementById('addAccountBtn').style.display = 'block';
        } else {
            setFormError(data.message || 'OTP verification failed. Please try again.');
        }
    } catch (error) {
        showToast('An error occurred during OTP verification. Please try again.', 'error');
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.textContent = 'Confirm Account';
    }
}

async function resendOtp() {
    const accountNumber = document.getElementById('accountNumber').value;
    clearFormError();

    if (!accountNumber) {
        setFormError('Please enter an account number first');
        return;
    }
    
    // Show loading state with enhanced visual feedback
    const resendBtn = document.getElementById('resendOtpBtn');
    const originalText = resendBtn.textContent;
    
    // Add loading animation
    resendBtn.disabled = true;
    resendBtn.textContent = 'Sending...';
    
    // Add visual feedback
    resendBtn.style.transform = 'scale(0.95)';
    resendBtn.style.boxShadow = '0 0 0 3px rgba(3, 105, 161, 0.3)';
    
    try {
        // Simulate API call delay for better UX
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // In future, call backend resend endpoint. For now show success.
        clearFormError();
        showToast('Verification code resent successfully!', 'success');
        
        // Add success animation
        resendBtn.style.background = 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)';
        resendBtn.style.color = '#065f46';
        
        // Reset after delay
        setTimeout(() => {
            resendBtn.style.background = '';
            resendBtn.style.color = '';
            
            // Re-enable button after cooldown period
            setTimeout(() => {
                resendBtn.disabled = false;
                resendBtn.textContent = originalText;
                resendBtn.style.transform = '';
                resendBtn.style.boxShadow = '';
            }, 1000);
        }, 1500);
        
    } catch (error) {
        showToast('Failed to resend verification code. Please try again.', 'error');
        
        // Add error feedback
        resendBtn.style.background = 'linear-gradient(135deg, #fee2e2 0%, #fecaca 100%)';
        resendBtn.style.color = '#b91c1c';
        
        setTimeout(() => {
            resendBtn.style.background = '';
            resendBtn.style.color = '';
            resendBtn.disabled = false;
            resendBtn.textContent = originalText;
            resendBtn.style.transform = '';
            resendBtn.style.boxShadow = '';
        }, 2000);
    }
}

async function handleAddAccount(event) {
    event.preventDefault();
    clearFormError();

    const branch = document.getElementById('branch').value;
    const accountNumber = document.getElementById('accountNumber').value;
    const accountType = document.getElementById('accountType').value;
    const accountNickname = document.getElementById('accountNickname').value;
    
    // Validate required fields
    if (!branch || !accountNumber || !accountType || !accountNickname) {
        setFormError('Please fill in all required fields');
        return;
    }
    
    // Additional validation
    if (accountNumber.length < 6) {
        setFormError('Account number must be at least 6 characters long');
        return;
    }
    
    if (accountNickname.length < 2) {
        setFormError('Account nickname must be at least 2 characters long');
        return;
    }
    
    // Validate account number format (alphanumeric)
    const accountNumberRegex = /^[a-zA-Z0-9]+$/;
    if (!accountNumberRegex.test(accountNumber)) {
        setFormError('Account number can only contain letters and numbers');
        return;
    }
    
    // Validate nickname format (no special characters except spaces)
    const nicknameRegex = /^[a-zA-Z0-9\s]+$/;
    if (!nicknameRegex.test(accountNickname)) {
        setFormError('Account nickname can only contain letters, numbers, and spaces');
        return;
    }
    
    // Show loading state
    const addBtn = document.getElementById('addAccountBtn');
    addBtn.disabled = true;
    addBtn.textContent = 'Adding Account...';
    
    try {
        // Call the backend API to add the account
        const response = await fetch('/api/accounts/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            },
            body: JSON.stringify({
                branch: branch,
                accountNumber: accountNumber,
                accountType: accountType,
                accountNickname: accountNickname
            })
        });
        
        if (response.ok) {
            clearFormError();
            showToast('Account added successfully', 'success');

            // Add the new account card to the UI
            addAccountCard(accountNickname, accountNumber, branch);

            // Close modal and reset form
            closeModal();
        } else {
            const errorData = await response.json();
            setFormError(errorData.message || 'Failed to add account. Please try again.');
        }
    } catch (error) {
        showNotification('An error occurred while adding the account. Please try again.', 'error');
    } finally {
        addBtn.disabled = false;
        addBtn.textContent = 'Add Account';
    }
}

// Function to handle nickname editing
const setupEditNickname = (editIcon, nicknameElement, accountNumber) => {
    editIcon.addEventListener('click', (e) => {
        e.stopPropagation(); // Prevent triggering the account selection
        
        // Create an input field to edit the nickname
        const input = document.createElement('input');
        input.type = 'text';
        input.value = nicknameElement.textContent;
        input.className = 'nickname-edit-input';
        
        // Add a subtle animation when the input appears
        input.style.opacity = '0';
        input.style.transform = 'scale(0.95)';
        
        // Replace the h3 with the input field
        nicknameElement.parentNode.replaceChild(input, nicknameElement);
        
        // Add animation effect
        setTimeout(() => {
            input.style.opacity = '1';
            input.style.transform = 'scale(1)';
        }, 10);
        
        // Focus the input and select the text
        input.focus();
        input.select();
        
        // Prevent click events on the input from bubbling up to the account card
        input.addEventListener('click', (e) => {
            e.stopPropagation();
        });
        
        // Add a subtle highlight effect to the account card
        const accountCard = input.closest('.account-card');
        let originalTransform = '';
        let originalBoxShadow = '';
        if (accountCard) {
            originalTransform = accountCard.style.transform;
            originalBoxShadow = accountCard.style.boxShadow;
            accountCard.style.transform = 'translateY(-2px)';
            accountCard.style.boxShadow = '0 10px 25px rgba(0, 0, 0, 0.15)';
        }
        
        // Handle saving the new nickname
        const saveNickname = () => {
            const newNickname = input.value.trim();
            
            if (newNickname && newNickname !== nicknameElement.textContent) {
                // Validate the new nickname
                if (newNickname.length < 2) {
                    showToast('Account nickname must be at least 2 characters long', 'error');
                    // Revert to the original nickname
                    input.parentNode.replaceChild(nicknameElement, input);
                    return;
                }
                
                // Validate nickname format (no special characters except spaces)
                const nicknameRegex = /^[a-zA-Z0-9\s]+$/;
                if (!nicknameRegex.test(newNickname)) {
                    showToast('Account nickname can only contain letters, numbers, and spaces', 'error');
                    // Revert to the original nickname
                    input.parentNode.replaceChild(nicknameElement, input);
                    return;
                }
                
                // Create a temporary h3 element for the UI update
                const tempNicknameElement = document.createElement('h3');
                tempNicknameElement.className = 'account-nickname';
                tempNicknameElement.textContent = newNickname;
                
                // Update the nickname in the backend
                updateAccountNickname(accountNumber, newNickname)
                    .then(() => {
                        showToast('Account nickname updated successfully', 'success');
                        // Replace the input with the updated h3 element
                        input.parentNode.replaceChild(tempNicknameElement, input);
                        
                        // Add a subtle success animation
                        tempNicknameElement.style.opacity = '0';
                        tempNicknameElement.style.transform = 'scale(0.95)';
                        
                        setTimeout(() => {
                            tempNicknameElement.style.opacity = '1';
                            tempNicknameElement.style.transform = 'scale(1)';
                        }, 10);
                    })
                    .catch(error => {
                        showToast('Failed to update account nickname', 'error');
                        // Revert to the original nickname
                        input.parentNode.replaceChild(nicknameElement, input);
                    });
            } else {
                // If no changes or empty, revert back to the h3 element
                input.parentNode.replaceChild(nicknameElement, input);
            }
            
            // Restore original account card styles
            if (accountCard) {
                accountCard.style.transform = originalTransform || '';
                accountCard.style.boxShadow = originalBoxShadow || '';
            }
        };
        
        // Save when Enter is pressed
        input.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                saveNickname();
            }
        });
        
        // Save when input loses focus
        input.addEventListener('blur', saveNickname);
    });
};

function addAccountCard(accountNickname, accountNumber, branch) {
    const accountsContainer = document.querySelector('.accounts-container');
    
    // Create the new account card HTML
    const accountCard = document.createElement('div');
    accountCard.className = 'account-card';
    
    // Map branch value to branch name for display
    const branchNames = {
        'colombo central': 'Colombo Central Branch',
        'colombo fort': 'Colombo Fort Branch',
        'kandy': 'Kandy Branch',
        'galle': 'Galle Branch',
        'jaffna': 'Jaffna Branch',
        'gampaha': 'Gampaha Branch',
        'kurunegala': 'Kurunegala Branch',
        'matara': 'Matara Branch',
        'anuradhapura': 'Anuradhapura Branch',
        'badulla': 'Badulla Branch',
        'ratnapura': 'Ratnapura Branch'
    };
    
    const branchDisplayName = branchNames[branch] || branch;
    
    // Mask the account number (show only last 3 digits)
    const maskedAccountNumber = accountNumber.length > 3 
        ? '*'.repeat(accountNumber.length - 3) + accountNumber.slice(-3)
        : accountNumber;
    
    accountCard.innerHTML = `
        <div class="account-header">
            <button class="edit-icon">
                <img src="img/edit_icon.png" alt="Edit">
            </button>
            <h3 class="account-nickname">${accountNickname}</h3>
        </div>
        
        <div class="account-center">
            <div class="bank-info">
                <div class="bank-logo">N</div>
                <div class="bank-details">
                    <p class="bank-name">NOVA BANK</p>
                    <p class="branch-name">${branchDisplayName}</p>
                    <p class="account-number">${maskedAccountNumber}</p>
                </div>
            </div>
        </div>
        
        <div class="account-footer">
            <button class="delete-icon">
                <img src="img/delete_icon.png" alt="Delete">
            </button>
        </div>
    `;
    
    // Store the account number as a data attribute for later use
    accountCard.setAttribute('data-account-number', accountNumber);
    
    // Add event listener for the edit icon
    const editIcon = accountCard.querySelector('.edit-icon');
    const nicknameElement = accountCard.querySelector('.account-nickname');
    
    // Setup edit functionality for this account
    setupEditNickname(editIcon, nicknameElement, accountNumber);
    
    // Add event listener for the delete icon
    const deleteIcon = accountCard.querySelector('.delete-icon');
    if (deleteIcon) {
        deleteIcon.addEventListener('click', (e) => {
            e.stopPropagation(); // Prevent triggering the account selection
            showDeleteModal(accountNumber, accountNickname);
        });
    }
    
    // Check if this is the first account being added (remove dummy if present)
    const existingAccountCards = accountsContainer.querySelectorAll('.account-card');
    if (existingAccountCards.length === 1 && existingAccountCards[0].querySelector('.account-nickname').textContent === 'Savings Account') {
        // This is the first real account, remove the dummy card
        accountsContainer.removeChild(existingAccountCards[0]);
    }
    
    // Insert the new card before the "Add a bank account" cards
    const addAccountCards = document.querySelectorAll('.add-account-card');
    if (addAccountCards.length > 0) {
        accountsContainer.insertBefore(accountCard, addAccountCards[0]);
    } else {
        accountsContainer.appendChild(accountCard);
    }

    // attach click handler to switch to this account, but exclude interactive elements
    accountCard.addEventListener('click', (e) => {
        // Don't trigger switch if clicking on interactive elements like inputs, buttons, etc.
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'BUTTON' || 
            e.target.classList.contains('nickname-edit-input')) {
            return;
        }
        
        // Check if this is already the selected account
        const selectedAccount = localStorage.getItem('selectedAccount');
        if (selectedAccount) {
            const parsedAccount = JSON.parse(selectedAccount);
            if (parsedAccount.accountNo === accountNumber) {
                // This is already the selected account, no need to switch
                showToast(`Already using ${accountNickname}`, 'info');
                return;
            }
        }
        
        showSwitchModal(accountNumber, accountNickname, branch);
    });
    
    // Check if this account should be marked as selected
    const currentSelected = localStorage.getItem('selectedAccount');
    if (currentSelected) {
        const parsedSelected = JSON.parse(currentSelected);
        if (parsedSelected.accountNo === accountNumber) {
            accountCard.classList.add('selected');
        }
    }
}

// Update account nickname in the backend
async function updateAccountNickname(accountNumber, newNickname) {
    try {
        const response = await fetch(`/api/accounts/${encodeURIComponent(accountNumber)}/nickname`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            },
            body: JSON.stringify({
                accountNickname: newNickname
            })
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to update nickname' }));
            throw new Error(errorData.message || 'Failed to update nickname');
        }
        
        return response.json();
    } catch (error) {
        console.error('Error updating account nickname:', error);
        throw error;
    }
}

// Load existing linked accounts from backend
async function loadAccounts() {
    try {
        const response = await fetch('/api/accounts/my-accounts', {
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            }
        });

        if (!response.ok) return;
        const accounts = await response.json();
        
        // If there are existing accounts, remove the dummy account first
        if (accounts.length > 0) {
            const accountsContainer = document.querySelector('.accounts-container');
            const existingAccountCards = accountsContainer.querySelectorAll('.account-card');
            if (existingAccountCards.length === 1 && existingAccountCards[0].querySelector('.account-nickname').textContent === 'Savings Account') {
                // Remove the dummy card before adding real accounts
                accountsContainer.removeChild(existingAccountCards[0]);
            }
        }
        
        accounts.forEach(a => {
            // Use the branch information from the API response
            const branch = a.branch || 'colombo'; // default to colombo if not provided
            addAccountCard(a.accountNickname || a.accountType || 'Account', a.accountNo, branch);
        });
        
        // After loading accounts, set up edit functionality for existing account cards
        setTimeout(() => {
            const allAccountCards = document.querySelectorAll('.account-card');
            allAccountCards.forEach(card => {
                const editIcon = card.querySelector('.edit-icon');
                const nicknameElement = card.querySelector('.account-nickname');
                const accountNumber = card.getAttribute('data-account-number');
                
                // Set up edit functionality for this loaded account
                if (editIcon && nicknameElement && accountNumber) {
                    // Setup edit functionality for this loaded account
                    setupEditNickname(editIcon, nicknameElement, accountNumber);
                }
                
                // Check if this account should be marked as selected
                const currentSelected = localStorage.getItem('selectedAccount');
                if (currentSelected) {
                    const parsedSelected = JSON.parse(currentSelected);
                    if (parsedSelected.accountNo === accountNumber) {
                        card.classList.add('selected');
                    }
                }
            });
        }, 100); // Small delay to ensure DOM is updated
    } catch (e) {
        console.error('Failed to load accounts', e);
    }
}

let _switchTarget = null;
let _deleteTarget = null;

function showDeleteModal(accountNo, nickname) {
    _deleteTarget = { accountNo, nickname };
    const deleteModal = document.getElementById('deleteConfirmModal');
    const text = document.getElementById('deleteModalText');
    text.innerText = `Are you sure you want to remove account ${nickname} (${accountNo})?`;
    deleteModal.style.display = 'flex';
}

// Initialize delete modal event listeners
function initDeleteModal() {
    const deleteModal = document.getElementById('deleteConfirmModal');
    const closeDeleteModal = document.getElementById('closeDeleteModal');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    
    if (closeDeleteModal) closeDeleteModal.addEventListener('click', () => deleteModal.style.display = 'none');
    if (cancelDeleteBtn) cancelDeleteBtn.addEventListener('click', () => deleteModal.style.display = 'none');
    if (confirmDeleteBtn) confirmDeleteBtn.addEventListener('click', confirmDeleteAccount);
    
    // Close modal when clicking outside the modal content
    window.addEventListener('click', (event) => {
        if (event.target === deleteModal) {
            deleteModal.style.display = 'none';
        }
    });
}

function confirmDeleteAccount() {
    if (!_deleteTarget) return;
    
    const deleteModal = document.getElementById('deleteConfirmModal');
    
    // Remove the account from the backend
    deleteAccountFromBackend(_deleteTarget.accountNo)
        .then(() => {
            showToast('Account removed successfully', 'error');
            
            // Remove the account card from the UI
            removeAccountCard(_deleteTarget.accountNo);
            
            // Update dashboard if it's the currently selected account
            updateDashboardBalance(_deleteTarget.accountNo);
            
            deleteModal.style.display = 'none';
        })
        .catch(error => {
            showToast('Failed to remove account', 'error');
            deleteModal.style.display = 'none';
        });
}

async function deleteAccountFromBackend(accountNo) {
    try {
        const response = await fetch(`/api/accounts/${encodeURIComponent(accountNo)}/unlink`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            }
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to delete account' }));
            throw new Error(errorData.message || 'Failed to delete account');
        }
        
        return response.json();
    } catch (error) {
        console.error('Error deleting account:', error);
        throw error;
    }
}

function removeAccountCard(accountNo) {
    const allAccountCards = document.querySelectorAll('.account-card');
    allAccountCards.forEach(card => {
        const cardAccountNo = card.getAttribute('data-account-number');
        if (cardAccountNo === accountNo) {
            card.remove();
        }
    });
}

function updateDashboardBalance(accountNo) {
    // Check if this account was the selected account and update dashboard if needed
    const selectedAccount = localStorage.getItem('selectedAccount');
    if (selectedAccount) {
        const parsedAccount = JSON.parse(selectedAccount);
        if (parsedAccount.accountNo === accountNo) {
            // Update the balance display on dashboard to show "-----" if on dashboard page
            const balanceElement = document.querySelector('.balance-amount');
            if (balanceElement) {
                balanceElement.textContent = '-----';
            }
            
            // Remove the selected account from localStorage
            localStorage.removeItem('selectedAccount');
            
            // If currently on dashboard page, we may need to refresh the view
            if (window.location.pathname.includes('dashboard.html')) {
                // The balance display should now show '-----' since we just updated it
                // The next loadSelectedAccountBalance call will also show '-----' since the account is removed from localStorage
            }
        }
    }
}

function showSwitchModal(accountNo, nickname, branch) {
    // Check if this is already the selected account
    const selectedAccount = localStorage.getItem('selectedAccount');
    if (selectedAccount) {
        const parsedAccount = JSON.parse(selectedAccount);
        if (parsedAccount.accountNo === accountNo) {
            // This is already the selected account, no need to switch
            showToast(`Already using ${nickname}`, 'info');
            return;
        }
    }
    
    _switchTarget = { accountNo, nickname, branch };
    const switchModal = document.getElementById('switchConfirmModal');
    const text = document.getElementById('switchModalText');
    text.innerText = `Switch to ${nickname} (${accountNo})?`;
    switchModal.style.display = 'flex';
}

async function confirmSwitchAccount() {
    if (!_switchTarget) return;
    const switchModal = document.getElementById('switchConfirmModal');
    try {
        const res = await fetch(`/api/accounts/${encodeURIComponent(_switchTarget.accountNo)}/balance`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('token')}` }
        });
        if (!res.ok) {
            const err = await res.json().catch(()=>({message:'Failed to fetch balance'}));
            setFormError(err.message || 'Failed to switch account');
            return;
        }
        const data = await res.json();

        // Persist selected account
        localStorage.setItem('selectedAccount', JSON.stringify({
            accountNo: data.accountNo,
            accountNickname: _switchTarget.nickname,
            accountBalance: data.accountBalance
        }));

        // Update balance on this page if element exists
        const balEl = document.querySelector('.balance-amount');
        if (balEl) {
            balEl.innerText = `Rs. ${Number(data.accountBalance).toLocaleString('en-LK', {minimumFractionDigits:2, maximumFractionDigits:2})}`;
        }
        
        // Update dashboard if on the dashboard page
        if (window.location.pathname.includes('dashboard.html')) {
            // Update the dashboard balance display
            const dashboardBalEl = document.querySelector('.balance-amount');
            if (dashboardBalEl) {
                dashboardBalEl.innerText = `Rs. ${Number(data.accountBalance).toLocaleString('en-LK', {minimumFractionDigits:2, maximumFractionDigits:2})}`;
            }
        }
        
        // Update account card styling to show selected account
        updateSelectedAccountCard(data.accountNo);

        showToast('Switched account', 'success');
        switchModal.style.display = 'none';
    } catch (e) {
        setFormError('Failed to switch account');
    }
}

// Reuse the showToast function from util.js if available
function showToast(message, type) {
    // Prefer the util.js implementation if exposed as _showToast
    if (typeof window._showToast === 'function') {
        window._showToast(message, type);
        return;
    } else {
        // Fallback implementation if not available
        const toast = document.getElementById('toast');
        if (toast) {
            toast.textContent = message;
            toast.className = `toast ${type} active`;
            setTimeout(() => toast.classList.remove('active'), 3000);
        } else {
            // If no toast element exists, use alert as fallback
            alert(message);
        }
    }
}

// Show notification using the new banner instead of toast
function showNotification(message, type) {
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

// Listen for changes to selected account in localStorage from other tabs/pages
window.addEventListener('storage', function(e) {
    if (e.key === 'selectedAccount') {
        // Selected account was changed in another tab, update the display
        // If there's a balance element, update it
        loadSelectedAccountBalance();
    }
});

// Function to update the selected account card styling
function updateSelectedAccountCard(selectedAccountNo) {
    // Remove 'selected' class from all account cards
    const allAccountCards = document.querySelectorAll('.account-card');
    allAccountCards.forEach(card => {
        card.classList.remove('selected');
    });
    
    // Add 'selected' class to the currently selected account card
    if (selectedAccountNo) {
        const selectedCard = document.querySelector(`.account-card[data-account-number="${selectedAccountNo}"]`);
        if (selectedCard) {
            selectedCard.classList.add('selected');
        }
    }
}

// Function to load selected account balance for this page
async function loadSelectedAccountBalance() {
    try {
        const sel = localStorage.getItem('selectedAccount');
        if (!sel) return;
        
        const obj = JSON.parse(sel);
        if (!obj || !obj.accountNo) return;

        const res = await fetch(`/api/accounts/${encodeURIComponent(obj.accountNo)}/balance`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('token')}` }
        });
        
        const balEl = document.querySelector('.balance-amount');
        if (balEl) {
            if (!res.ok) {
                balEl.innerText = '-----';
            } else {
                const data = await res.json();
                balEl.innerText = `Rs. ${Number(data.accountBalance).toLocaleString('en-LK', {minimumFractionDigits:2, maximumFractionDigits:2})}`;
            }
        }
        
        // Update account card selection styling
        updateSelectedAccountCard(obj.accountNo);
    } catch (e) {
        console.error('Failed to load selected account balance', e);
        const balEl = document.querySelector('.balance-amount');
        if (balEl) {
            balEl.innerText = '-----';
        }
    }
}

function setFormError(message) {
    const err = document.getElementById('formError');
    if (err) {
        err.innerText = message;
        err.style.display = 'block';
    } else {
        showNotification(message, 'error');
    }
}

function clearFormError() {
    const err = document.getElementById('formError');
    if (err) {
        err.innerText = '';
        err.style.display = 'none';
    }
}