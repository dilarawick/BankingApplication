console.log('bank_transfer.js loaded');
document.addEventListener('DOMContentLoaded', () => {
    // Only Nova Bank branches are needed since inter-bank transfers are not allowed
    const bankBranches = {
        'Nova Bank': ['Colombo', 'Colombo Central', 'Colombo Fort', 'Kandy', 'Galle', 'Jaffna', 'Matara']
    };

    const transferForm = document.getElementById('transferForm');
    const summarySection = document.getElementById('summarySection');
    const nextBtn = document.getElementById('nextBtn');
    const backBtn = document.getElementById('backBtn');
    const confirmBtn = document.getElementById('confirmBtn');
    console.log('confirmBtn element:', confirmBtn);
    const failedSection = document.getElementById('failedSection');
    const failedMessage = document.getElementById('failedMessage');
    const failedBalance = document.getElementById('failedBalance');
    const failedBackBtn = document.getElementById('failedBackBtn');
    const recipientBank = document.getElementById('recipientBank');
    const recipientBranch = document.getElementById('recipientBranch');
    const transferAmountEl = document.getElementById('transferAmount');
    const currencySuffixEl = document.querySelector('.currency-input .currency-suffix');

    // Last completed transfer receipt data for PDF generation
    let lastReceipt = null;

    function updateCurrencySuffix() {
        if (!transferAmountEl || !currencySuffixEl) return;
        const v = transferAmountEl.value;
        if (!v || isNaN(Number(v))) {
            currencySuffixEl.textContent = '.00';
            return;
        }
        const fixed = (Math.round(Number(v) * 100) / 100).toFixed(2);
        const dotPart = fixed.substring(fixed.indexOf('.'));
        currencySuffixEl.textContent = dotPart;
    }

    if (transferAmountEl) {
        transferAmountEl.addEventListener('input', updateCurrencySuffix);
        // initialize suffix on load
        updateCurrencySuffix();
    }

    function showToast(message, type) {
        const toast = document.getElementById('toast');
        if (!toast) return;
        toast.textContent = message;
        toast.className = 'toast show ' + type;
        setTimeout(() => { toast.className = 'toast'; }, 3000);
    }

    // Branch population
    if (recipientBank && recipientBranch) {
        recipientBank.addEventListener('change', () => {
            const selected = (recipientBank.value || '').trim();
            showToast('Bank selected: ' + (selected || 'none'), 'info');
            recipientBranch.innerHTML = '<option value="" disabled selected>Select branch</option>';
            const branches = bankBranches[selected] || bankBranches[Object.keys(bankBranches).find(k => k.toLowerCase() === selected.toLowerCase())];
            if (branches && branches.length) {
                branches.forEach(b => {
                    const opt = document.createElement('option'); opt.value = b; opt.textContent = b; recipientBranch.appendChild(opt);
                });
            }
        });
    }

    // Track current transfer amount precisely as string to avoid float rounding
    let currentTransferAmountNumber = 0;
    let currentTransferAmountStr = '0.00';

    // Next button validations and show summary
    if (nextBtn) {
        nextBtn.addEventListener('click', async () => {
            showToast('Next clicked — validating...', 'info');
            nextBtn.disabled = true;
            try {
                const sel = localStorage.getItem('selectedAccount');
                if (!sel) { showToast('Please select a sender account before transferring', 'error'); nextBtn.disabled = false; return; }
                const senderAccount = JSON.parse(sel).accountNo;

                // transferAmountEl is declared above and reused here
                const recipientNameEl = document.getElementById('recipientName');
                const recipientAccountEl = document.getElementById('recipientAccount');
                const referenceEl = document.getElementById('reference');

                if (!transferAmountEl.value || parseFloat(transferAmountEl.value) <= 0) { showToast('Please enter a valid transfer amount greater than 0', 'error'); nextBtn.disabled = false; return; }
                if (!recipientBank.value) { showToast('Please select recipient bank', 'error'); nextBtn.disabled = false; return; }
                if (!recipientBranch.value) { showToast('Please select recipient branch', 'error'); nextBtn.disabled = false; return; }

                const recipientAcc = (recipientAccountEl.value || '').trim();
                if (recipientAcc.length !== 6 || !/^[a-zA-Z0-9]+$/.test(recipientAcc)) { showToast('Recipient account number must be exactly 6 alphanumeric characters', 'error'); nextBtn.disabled = false; return; }

                const recipientName = (recipientNameEl.value || '').trim();
                if (!recipientName || recipientName.length < 2 || recipientName.length > 100 || !/^[a-zA-Z\s\.\-']+$/.test(recipientName)) { showToast('Recipient name must be 2-100 characters and contain valid characters', 'error'); nextBtn.disabled = false; return; }

                const reference = (referenceEl.value || '').trim();
                if (reference.length > 255) { showToast('Reference must not exceed 255 characters', 'error'); nextBtn.disabled = false; return; }

                // Optional balance check
                try {
                    const token = sessionStorage.getItem('token');
                    const balRes = await fetch(`/api/accounts/${encodeURIComponent(senderAccount)}/balance`, { headers: { ...(token ? { 'Authorization': `Bearer ${token}` } : {}) } });
                    if (balRes.ok) {
                        const balData = await balRes.json();
                        if (Number(balData.accountBalance) < parseFloat(transferAmountEl.value)) { showToast('Insufficient balance in the selected account', 'error'); nextBtn.disabled = false; return; }
                    }
                } catch (e) { console.warn('Balance check failed', e); }

                // Populate summary
                // Store precise amount for later (use string with two decimals)
                currentTransferAmountNumber = parseFloat(transferAmountEl.value);
                currentTransferAmountStr = currentTransferAmountNumber.toFixed(2);
                document.getElementById('summaryAmount').textContent = 'Rs. ' + currentTransferAmountStr;
                document.getElementById('summaryName').textContent = recipientName;
                document.getElementById('summaryBank').textContent = recipientBank.value;
                document.getElementById('summaryBranch').textContent = recipientBranch.value;
                document.getElementById('summaryAccount').textContent = (recipientAcc.length > 4) ? '*'.repeat(recipientAcc.length - 4) + recipientAcc.slice(-4) : recipientAcc;
                document.getElementById('summaryReference').textContent = reference || 'N/A';

                transferForm.style.display = 'none'; summarySection.style.display = 'block';
            } catch (err) { console.error(err); showToast('Validation failed', 'error'); }
            nextBtn.disabled = false;
        });
    }

    if (backBtn) backBtn.addEventListener('click', () => { summarySection.style.display = 'none'; transferForm.style.display = 'block'; });

    // Confirm transfer
    if (confirmBtn) {
        confirmBtn.addEventListener('click', async (e) => {
            console.log('confirmBtn clicked (handler)');
            showToast('Confirm clicked', 'info');
            try {
                const sel = localStorage.getItem('selectedAccount'); if (!sel) { showToast('Please select a sender account before transferring', 'error'); return; }
                const senderAccount = JSON.parse(sel).accountNo;
                // Use stored precise amount
                const amount = currentTransferAmountNumber || 0;

                // Pre-check balance and minimum required balance (Rs.1000)
                try {
                    const token = sessionStorage.getItem('token');
                    const balRes = await fetch(`/api/accounts/${encodeURIComponent(senderAccount)}/balance`, { headers: { ...(token ? { 'Authorization': `Bearer ${token}` } : {}) } });
                    if (balRes.ok) {
                        const balData = await balRes.json();
                        const balance = Number(balData.accountBalance);
                        // If transfer amount exceeds balance -> failed
                        if (amount > balance) {
                            console.log('Amount exceeds balance', amount, balance);
                            if (failedSection && failedMessage && failedBalance) {
                                failedMessage.textContent = 'Insufficient Balance';
                                failedBalance.textContent = `Current Balance is: Rs.${Number(balance).toLocaleString('en-LK')}`;
                                failedSection.style.display = 'flex';
                            } else {
                                showToast('Insufficient balance', 'error');
                            }
                            return;
                        }
                        // If remaining balance would go below minimum (1000)
                        if ((balance - amount) < 1000) {
                            console.log('Remaining balance below minimum', balance, amount);
                            if (failedSection && failedMessage && failedBalance) {
                                failedMessage.textContent = 'Minimum balance requirement not met';
                                failedBalance.textContent = `Current Balance is: Rs.${Number(balance).toLocaleString('en-LK')}`;
                                failedSection.style.display = 'flex';
                            } else {
                                showToast('Minimum balance not met', 'error');
                            }
                            return;
                        }
                    } else {
                        console.warn('Unable to fetch balance before confirm; proceeding to server validation');
                    }
                } catch (e) {
                    console.warn('Error fetching balance before confirm', e);
                }
                const payload = {
                    senderAccountNo: senderAccount,
                    recipientAccountNo: (document.getElementById('recipientAccount').value || '').trim(),
                    recipientBank: (document.getElementById('recipientBank').value || '').trim(),
                    recipientBranch: (document.getElementById('recipientBranch').value || '').trim(),
                    recipientName: (document.getElementById('recipientName').value || '').trim(),
                    // Send amount as a string with two decimals to preserve exact value
                    transferAmount: currentTransferAmountStr,
                    reference: (document.getElementById('reference').value || '').trim()
                };
                const token = sessionStorage.getItem('token');
                if (!token) {
                    console.warn('No auth token found in sessionStorage');
                    // Use visible failed overlay for auth problems so user sees it clearly
                    if (failedSection && failedMessage) {
                        failedMessage.textContent = 'Authentication required. Please log in again.';
                        if (failedBalance) failedBalance.textContent = '';
                        failedSection.style.display = 'flex';
                    } else {
                        showToast('You are not authenticated. Please log in.', 'error');
                    }
                    return;
                }

                console.log('Initiating transfer payload:', payload);
                const initRes = await fetch('/api/transfers/initiate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify(payload)
                });
                console.log('initiate status:', initRes.status);
                let initJson = null;
                try { initJson = await initRes.json(); console.log('initiate response json:', initJson); } catch (e) { console.warn('initiate response not json', e); }
                if (!initRes.ok || (initJson && initJson.transferStatus === 'FAILED')) {
                    const msg = (initJson && initJson.message) || 'Transfer initiation failed';
                    console.warn('Initiation failed:', msg);
                    if (failedSection && failedMessage) {
                        failedMessage.textContent = msg;
                        if (failedBalance) failedBalance.textContent = '';
                        failedSection.style.display = 'flex';
                    } else {
                        showToast(msg, 'error');
                    }
                    return;
                }
                const transferId = initJson ? initJson.transferId : null;
                if (!transferId) { showToast('Transfer initiation did not return an ID', 'error'); return; }

                const confirmRes = await fetch(`/api/transfers/confirm/${encodeURIComponent(transferId)}`, {
                    method: 'POST',
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                console.log('confirm status:', confirmRes.status);
                let confirmJson = null;
                try { confirmJson = await confirmRes.json(); console.log('confirm response json:', confirmJson); } catch (e) { console.warn('confirm response not json', e); }
                if (!confirmRes.ok || (confirmJson && confirmJson.transferStatus !== 'COMPLETED')) {
                    const msg = (confirmJson && confirmJson.message) || 'Transfer confirmation failed';
                    console.warn('Confirmation failed:', msg);
                    if (failedSection && failedMessage) {
                        failedMessage.textContent = msg;
                        if (failedBalance) failedBalance.textContent = '';
                        failedSection.style.display = 'flex';
                    } else {
                        showToast(msg, 'error');
                    }
                    return;
                }
                const txId = confirmJson.transferId || '';
                const txDate = new Date();
                document.getElementById('successConfirmationNumber').textContent = txId;
                // show amount and date
                const successAmountEl = document.getElementById('successAmount');
                const successDateEl = document.getElementById('successDate');
                if (successAmountEl) successAmountEl.textContent = 'Rs. ' + currentTransferAmountStr;
                if (successDateEl) successDateEl.textContent = txDate.toLocaleString();

                // Store receipt details for PDF generation
                lastReceipt = {
                    transferId: txId,
                    amount: currentTransferAmountStr,
                    dateISO: txDate.toISOString(),
                    dateDisplay: txDate.toLocaleString(),
                    recipientName: payload.recipientName,
                    recipientAccount: payload.recipientAccountNo,
                    recipientBank: payload.recipientBank,
                    recipientBranch: payload.recipientBranch,
                    reference: payload.reference || ''
                };

                transferForm.style.display = 'none'; summarySection.style.display = 'none'; document.getElementById('successSection').style.display = 'flex';
                document.getElementById('transferForm').reset(); if (recipientBranch) recipientBranch.innerHTML = '<option value="" disabled selected>Select branch</option>';
            } catch (err) { console.error('Transfer error', err); showToast('Transfer failed: ' + (err.message || ''), 'error'); }
        });
    } else {
        // fallback: delegate click in case element wasn't present at query time
        document.addEventListener('click', (ev) => {
            const t = ev.target;
            if (t && (t.id === 'confirmBtn' || t.closest && t.closest('#confirmBtn'))) {
                console.log('confirmBtn clicked (delegated)');
                showToast('Confirm clicked (delegated)', 'info');
            }
        });
    }

    const backToHomeBtn = document.getElementById('backToHomeBtn');
    if (backToHomeBtn) backToHomeBtn.addEventListener('click', () => window.location.href = 'dashboard.html');
    if (failedBackBtn) failedBackBtn.addEventListener('click', () => window.location.href = 'dashboard.html');

    // PDF generation using jsPDF
    async function generateReceiptPDF(receipt) {
        try {
            const { jsPDF } = window.jspdf;
            const doc = new jsPDF({ unit: 'pt', format: 'A4' });

            // Draw header logo
            const logoImg = document.getElementById('novaLogoImg');
            if (logoImg) {
                // drawImage accepts HTMLImageElement; ensure it's loaded
                try { doc.addImage(logoImg, 'PNG', 40, 40, 80, 80); } catch (e) { console.warn('Logo draw failed', e); }
            }

            // Title
            doc.setFontSize(18); doc.text('NOVA BANK - Transaction Receipt', 140, 70);

            doc.setFontSize(12);
            let y = 140;
            const pad = 20;

            doc.text(`Transaction ID: ${receipt.transferId}`, 40, y); y += pad;
            doc.text(`Amount: Rs. ${receipt.amount}`, 40, y); y += pad;
            doc.text(`Date: ${receipt.dateDisplay}`, 40, y); y += pad;
            doc.text(`Recipient Name: ${receipt.recipientName}`, 40, y); y += pad;
            doc.text(`Recipient Bank: ${receipt.recipientBank}`, 40, y); y += pad;
            doc.text(`Recipient Branch: ${receipt.recipientBranch}`, 40, y); y += pad;
            doc.text(`Recipient Account: ${receipt.recipientAccount}`, 40, y); y += pad;
            doc.text(`Reference: ${receipt.reference || 'N/A'}`, 40, y); y += pad;

            // Footer
            doc.setFontSize(10);
            doc.text('This is a system generated receipt from Nova Bank.', 40, 760);

            return doc;
        } catch (err) {
            console.error('PDF generation failed', err);
            return null;
        }
    }

    const downloadReceiptBtn = document.getElementById('downloadReceiptBtn');
    if (downloadReceiptBtn) {
        downloadReceiptBtn.addEventListener('click', async () => {
            if (!lastReceipt) { showToast('No receipt available to download', 'error'); return; }
            const doc = await generateReceiptPDF(lastReceipt);
            if (!doc) { showToast('Failed to generate PDF', 'error'); return; }
            const filename = `NovaBank_Receipt_${lastReceipt.transferId || Date.now()}.pdf`;
            doc.save(filename);
            showToast('PDF downloaded', 'success');
        });
    }
});
