// Pay Bills JavaScript functionality
(function(){
    const billerMap = {
        utilities: [
            { id: 'ceb', name: 'CEB', logo: 'img/providers/ceb.png' },
            { id: 'water', name: 'Water Board', logo: 'img/providers/water.png' }
        ],
        telephone: [
            { id: 'slt', name: 'SLT', logo: 'img/providers/slt.png' },
            { id: 'mobitel', name: 'Mobitel', logo: 'img/providers/mobitel.jpg' },
            { id: 'lankabell', name: 'Lanka Bell', logo: 'img/providers/lankabell.png' }
        ],
        mobile: [
            { id: 'mobitel', name: 'Mobitel', logo: 'img/providers/mobitel.jpg' },
            { id: 'dialog', name: 'Dialog', logo: 'img/providers/dialog.jpg' },
            { id: 'airtel', name: 'Airtel', logo: 'img/providers/airtel.png' }
        ]
    };

    let selectedCategory = null;
    let selectedBiller = null;

    function $(id){ return document.getElementById(id); }
    
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

    function renderBillers(category){
        const container = $('billerList');
        container.innerHTML = '';
        const list = billerMap[category] || [];
        list.forEach(b => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'biller-card';
            btn.dataset.biller = b.id;
            btn.title = b.name;

            const img = document.createElement('img');
            img.src = b.logo;
            img.alt = b.name;
            img.onerror = function(){ this.style.display='none'; };

            const span = document.createElement('span');
            span.textContent = b.name;

            btn.appendChild(img);
            btn.appendChild(span);

            btn.addEventListener('click', () => {
                Array.from(container.querySelectorAll('.biller-card')).forEach(c => c.classList.remove('active'));
                btn.classList.add('active');
                selectedBiller = b.id;
                fetchUnpaidBills(category, b.id);
            });

            container.appendChild(btn);
        });
    }

    async function fetchUnpaidBills(category, biller){
        const tbody = $('unpaidBillsTable').querySelector('tbody');
        tbody.innerHTML = '<tr><td colspan="4">Loading unpaid bills...</td></tr>';
        try{
            const res = await fetch(`/api/bills/unpaid?category=${encodeURIComponent(category)}&biller=${encodeURIComponent(biller)}`, {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('token')}` }
            });
            if(!res.ok){
                const errorData = await res.json().catch(() => ({message: 'Failed to load unpaid bills'}));
                tbody.innerHTML = `<tr><td colspan="4">${errorData.message || 'Failed to load unpaid bills'}</td></tr>`;
                return;
            }
            const bills = await res.json();
            if(!Array.isArray(bills) || bills.length===0){
                tbody.innerHTML = `<tr><td colspan="4">No unpaid bills</td></tr>`;
                return;
            }
            tbody.innerHTML = '';
            bills.forEach(bill => {
                const tr = document.createElement('tr');
                const radioTd = document.createElement('td');
                const r = document.createElement('input');
                r.type = 'radio'; r.name = 'selectedBill'; r.dataset.billId = bill.id; r.dataset.amount = bill.amount;
                r.addEventListener('change', () => {
                    $('amount').value = Number(bill.amount).toFixed(2);
                    $('amount').min = Number(bill.amount).toFixed(2);
                });
                radioTd.appendChild(r);

                const refTd = document.createElement('td'); refTd.textContent = bill.reference || bill.invoice || bill.id;
                const dueTd = document.createElement('td'); dueTd.textContent = bill.dueDate || '-';
                const amtTd = document.createElement('td'); amtTd.textContent = Number(bill.amount).toLocaleString('en-LK', {minimumFractionDigits:2, maximumFractionDigits:2});

                tr.appendChild(radioTd);
                tr.appendChild(refTd);
                tr.appendChild(dueTd);
                tr.appendChild(amtTd);
                tbody.appendChild(tr);
            });
        }catch(e){
            tbody.innerHTML = `<tr><td colspan="4">Error loading unpaid bills</td></tr>`;
            console.error(e);
            showNotification('Error loading unpaid bills', 'error');
        }
    }

    async function handlePay(){
        const sel = localStorage.getItem('selectedAccount');
        const accountInput = $('accountNumber').value.trim();
        if(!accountInput){ showNotification('Enter account number', 'error'); return; }

        if(!sel){ showNotification('Please select an account in Accounts page first', 'error'); return; }
        const parsed = JSON.parse(sel);
        if(parsed.accountNo !== accountInput){ showNotification('Account number must match selected account', 'error'); return; }

        const selectedRadio = document.querySelector('input[name="selectedBill"]:checked');
        if(!selectedRadio){ showNotification('Select an unpaid bill to pay', 'error'); return; }

        const billId = selectedRadio.dataset.billId;
        const billAmount = Number(selectedRadio.dataset.amount);
        const amount = Number($('amount').value);
        if(Number.isNaN(amount) || amount < billAmount){ showNotification('Amount must be equal or greater than bill amount', 'error'); return; }

        try{
            // check balance
            const balRes = await fetch(`/api/accounts/${encodeURIComponent(accountInput)}/balance`, {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('token')}` }
            });
            if(!balRes.ok){ 
                const errorData = await balRes.json().catch(() => ({message: 'Failed to check account balance'}));
                showNotification(errorData.message || 'Failed to check account balance', 'error'); 
                return; 
            }
            const balData = await balRes.json();
            const balance = Number(balData.accountBalance || 0);
            if(balance < amount){ showNotification('Insufficient funds', 'error'); return; }

            // perform payment
            const payRes = await fetch('/api/bills/pay', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${sessionStorage.getItem('token')}` },
                body: JSON.stringify({ accountNumber: accountInput, billId: billId, amount: amount })
            });
            const payData = await payRes.json();
            if(payRes.ok){
                showNotification(payData.message || 'Payment successful', 'success');
                // refresh unpaid bills for current selection
                if(selectedCategory && selectedBiller) fetchUnpaidBills(selectedCategory, selectedBiller);
            } else {
                showNotification(payData.message || 'Payment failed', 'error');
            }
        }catch(e){
            console.error(e);
            showNotification('Payment error', 'error');
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        const categoryEl = $('category');
        categoryEl.addEventListener('change', (e) => {
            selectedCategory = e.target.value;
            selectedBiller = null;
            $('unpaidBillsTable').querySelector('tbody').innerHTML = '<tr><td colspan="4">Select a biller to see unpaid bills</td></tr>';
            renderBillers(selectedCategory);
        });

        // prefill selected account if present
        const sel = localStorage.getItem('selectedAccount');
        if(sel){
            try{ const obj = JSON.parse(sel); if(obj && obj.accountNo) $('accountNumber').value = obj.accountNo; } catch(e){}
        }

        $('payBtn').addEventListener('click', handlePay);
        
        // Set up close notification functionality
        const closeNotification = document.getElementById('closeNotification');
        if (closeNotification) {
            closeNotification.onclick = function() {
                const notificationBanner = document.getElementById('notificationBanner');
                if (notificationBanner) {
                    notificationBanner.style.display = 'none';
                }
            };
        }
    });
})();