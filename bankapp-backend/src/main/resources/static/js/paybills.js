/* paybills.js — single dynamic form version (final)
   - Uses Nova Bank logo (local path) and bank name in printed receipt
   - OTP "sent" message goes to #otpMsg (falls back to #pbError for general messages)
   - Print receipt opens receipt.html with query params (receipt page should read them)
   - Separated message boxes:
       - #billFormMsg for validation / bill-form errors
       - #otpMsg for OTP & paydetails messages
       - #pbError remains for general/global messages (kept for backwards compatibility)
*/

(function () {
  const $ = id => document.getElementById(id);

  // Local logo path (from uploaded files)
  const BANK_NAME = "Nova Bank";
  const BANK_LOGO_URL = "/img/j_logo.png";

  // mapping of biller keys to display name and "biller" used by backend
  const BILLER_MAP = {
    ceb: { title: "Ceylon Electricity Board", billerKey: "CEB", billPlaceholder: "Electricity Bill Account No" },
    water: { title: "National Water Supply and Drainage Board", billerKey: "NWSDB", billPlaceholder: "Water Bill Account No" },
    slt: { title: "Sri Lanka Telecom", billerKey: "SLT", billPlaceholder: "SLT Account No" },
    dialog: { title: "Dialog", billerKey: "Dialog", billPlaceholder: "Dialog Account No" }
  };

  let currentBiller = "ceb"; // default
  window.selectedPayment = null;
  window.allAccounts = [];

  /* -------------------------
     MESSAGE / ERROR HELPERS
     ------------------------- */

  // per-area message helpers (these are what you asked for)
  function showBillFormError(msg, autoHideMs = 7000) {
    const el = $("billFormMsg");
    if (!el) return console.error("BillFormError:", msg);
    el.className = "pb-error pb-error--error";
    el.textContent = msg;
    el.style.display = "block";
    clearTimeout(el._hideTimer);
    if (autoHideMs) el._hideTimer = setTimeout(() => { el.style.display = "none"; }, autoHideMs);
  }

  function showOtpInfo(msg, autoHideMs = 6000) {
    const el = $("otpMsg");
    if (!el) return console.info("OTP info:", msg);
    el.className = "pb-error pb-error--info";
    el.textContent = msg;
    el.style.display = "block";
    clearTimeout(el._hideTimer);
    if (autoHideMs) el._hideTimer = setTimeout(() => { el.style.display = "none"; }, autoHideMs);
  }

  function showOtpError(msg, autoHideMs = 7000) {
    const el = $("otpMsg");
    if (!el) return console.error("OTP error:", msg);
    el.className = "pb-error pb-error--error";
    el.textContent = msg;
    el.style.display = "block";
    clearTimeout(el._hideTimer);
    if (autoHideMs) el._hideTimer = setTimeout(() => { el.style.display = "none"; }, autoHideMs);
  }

  // existing global message helpers (keep for general messages/backwards compatibility)
  function _msgEl() { return $("pbError"); }
  function _otpInfoEl() { return $("otpInfo"); }

  function showError(msg, autoHideMs = 8000) {
    const el = _msgEl();
    if (!el) { console.error("Error:", msg); return; }
    el.className = "pb-error pb-error--error";
    el.textContent = msg;
    el.style.display = "block";
    clearTimeout(el._hideTimer);
    if (autoHideMs) el._hideTimer = setTimeout(() => { el.style.display = "none"; }, autoHideMs);
  }

  function showInfo(msg, autoHideMs = 6000) {
    // first try otpInfo area (if message is OTP related and element exists)
    const otpEl = _otpInfoEl();
    if (otpEl) {
      otpEl.textContent = msg;
      otpEl.style.display = "block";
      clearTimeout(otpEl._hideTimer);
      if (autoHideMs) otpEl._hideTimer = setTimeout(() => { otpEl.style.display = "none"; }, autoHideMs);
      return;
    }
    // fallback to main message box
    const el = _msgEl();
    if (!el) { console.info(msg); return; }
    el.className = "pb-error pb-error--info";
    el.textContent = msg;
    el.style.display = "block";
    clearTimeout(el._hideTimer);
    if (autoHideMs) el._hideTimer = setTimeout(() => { el.style.display = "none"; }, autoHideMs);
  }

  function clearMessage() {
    const el = _msgEl();
    if (el) { el.style.display = "none"; el.textContent = ""; clearTimeout(el._hideTimer); }
    const otpEl = _otpInfoEl();
    if (otpEl) { otpEl.style.display = "none"; otpEl.textContent = ""; clearTimeout(otpEl._hideTimer); }
    const billEl = $("billFormMsg");
    if (billEl) { billEl.style.display = "none"; billEl.textContent = ""; clearTimeout(billEl._hideTimer); }
    const otpMsgEl = $("otpMsg");
    if (otpMsgEl) { otpMsgEl.style.display = "none"; otpMsgEl.textContent = ""; clearTimeout(otpMsgEl._hideTimer); }
  }

  /* -------------------------
     DOM SHOW / HIDE HELPERS
     ------------------------- */
  function show(el) { if (el) el.style.display = "block"; }
  function hide(el) { if (el) el.style.display = "none"; }

  /* -------------------------
     BALANCE CARD
     ------------------------- */
  function updateBalanceCard(accountNo) {
    if (!accountNo || !window.allAccounts) return;
    const acc = window.allAccounts.find(a => a.accountNo === accountNo);
    if (!acc) return;
    const balanceEl = $("balance");
    const typeEl = $("accountType");
    if (balanceEl) balanceEl.textContent = "Rs. " + acc.balance;
    if (typeEl) typeEl.textContent = acc.accountType || "";
  }

  /* -------------------------
     LOAD ACCOUNTS
     ------------------------- */
  function loadAccounts() {
    fetch("/api/dashboard/me")
      .then(r => r.json())
      .then(data => {
        if (!data || !data.ok) {
          console.warn("dashboard/me returned no accounts or not ok", data);
          showError("Unable to load accounts. Please login.");
          return;
        }
        window.allAccounts = data.accounts || [];
        const select = $("univAccountSelect");
        if (!select) return;
        select.innerHTML = '<option value="">-- Choose Account --</option>';
        window.allAccounts.forEach(acc => {
          const opt = document.createElement("option");
          opt.value = acc.accountNo;
          opt.textContent = `${acc.accountNo} - Rs. ${acc.balance}`;
          opt.dataset.balance = acc.balance;
          opt.dataset.type = acc.accountType;
          select.appendChild(opt);
        });
        const primary = window.allAccounts.find(a => a.isPrimary) || window.allAccounts[0];
        if (primary) {
          select.value = primary.accountNo;
          updateBalanceCard(primary.accountNo);
        }
      })
      .catch(err => {
        console.error("loadAccounts error:", err);
        showError("Network error while loading accounts.");
      });
  }

  /* -------------------------
     SWITCH BILLER (update UI / placeholders)
     ------------------------- */
  function switchToBiller(billerKey) {
    clearMessage();
    const cfg = BILLER_MAP[billerKey] || BILLER_MAP["ceb"];
    currentBiller = billerKey in BILLER_MAP ? billerKey : "ceb";
    const titleEl = $("formTitle");
    const billNoEl = $("univBillNo");
    const billNoReEl = $("univBillNoRe");
    if (titleEl) titleEl.textContent = cfg.title;
    if (billNoEl) billNoEl.placeholder = cfg.billPlaceholder;
    if (billNoReEl) billNoReEl.placeholder = "Re-enter " + cfg.billPlaceholder;
    const amt = $("univAmount"); if (amt) amt.value = "";
    if (billNoEl) billNoEl.value = "";
    if (billNoReEl) billNoReEl.value = "";
    hide($("paydetails"));
    hide($("paymentsuccess"));
    hide($("paymentfailed"));
    show($("universalForm"));
  }

  /* -------------------------
     VALIDATE + CHECK BILL THEN SHOW PAYDETAILS
     ------------------------- */
  function onNextClicked(ev) {
    if (ev && ev.preventDefault) ev.preventDefault();
    clearMessage();
    const accountEl = $("univAccountSelect");
    const account = accountEl ? accountEl.value : "";
    const amountEl = $("univAmount");
    const amountStr = amountEl ? ("" + amountEl.value).trim() : "";
    const bill1El = $("univBillNo");
    const bill2El = $("univBillNoRe");
    const bill1 = bill1El ? bill1El.value.trim() : "";
    const bill2 = bill2El ? bill2El.value.trim() : "";
    const biller = (BILLER_MAP[currentBiller] && BILLER_MAP[currentBiller].billerKey) || "CEB";

    if (!account) { showBillFormError("Please select an account.");
         return; 
        }
    if (!bill1) {
        showBillFormError("Please enter the bill number.");
        return;
    }

    if (!bill2) {
        showBillFormError("Please re-enter the bill number.");
        return;
    }
     if (!amountStr) {
        showBillFormError("Please enter an amount.");
        return;
    }
    if (!bill1 || !bill2 || bill1 !== bill2) { showBillFormError("Bill numbers do not match."); return; }
    const amount = Number(amountStr);
    if (!amountStr || isNaN(amount) || amount <= 0) { showBillFormError("Please enter a valid amount."); return; }

    console.log("🔎 /api/bills/check ->", { billNo: bill1, biller });

    fetch("/api/bills/check", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ billNo: bill1, biller: biller })
    })
      .then(res => {
        if (!res.ok) throw new Error("Network response not ok: " + res.status);
        return res.json();
      })
      .then(data => {
        console.log("🔎 /api/bills/check response:", data);
        if (!data || !data.exists) { showBillFormError("This bill number does not exist."); return; }
        const confirmName = $("confirmName"), confirmAmount = $("confirmAmount"), bpdate = $("bpdate");
        if (confirmName) confirmName.textContent = bill1;
        if (confirmAmount) confirmAmount.textContent = amount;
        if (bpdate) bpdate.textContent = new Date().toLocaleString();
        hide($("universalForm"));
        hide($("paymentsuccess"));
        hide($("paymentfailed"));
        show($("paydetails"));
        window.selectedPayment = { account: account, billNo: bill1, amount: amount, biller: biller };
      })
      .catch(err => {
        console.error("Error /api/bills/check:", err);
        showBillFormError("Unable to check bill right now. Please try later.");
      });
  }

  /* -------------------------
     Initialize biller buttons
     ------------------------- */
  function initBillerButtons() {
    const buttons = document.querySelectorAll(".utility-btn");
    if (!buttons) return;
    buttons.forEach(btn => {
      btn.addEventListener("click", (e) => {
        e.preventDefault && e.preventDefault();
        const key = btn.dataset.biller || btn.getAttribute("data-biller");
        switchToBiller(key);
      });
    });
  }

  /* -------------------------
     OTP + Confirm Payment
     ------------------------- */
  function initOtpAndConfirm() {
    const sendBtn = $("sendOtpBtn");
    const confirmBtn = $("confirmPayBtn");
    const otpInput = $("otpInput");

    if (sendBtn) {
      sendBtn.addEventListener("click", (e) => {
        e && e.preventDefault && e.preventDefault();
        clearMessage();
        console.log("📨 Sending OTP request...");
        fetch("/api/payments/send-otp-for-payment", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({})
        })
          .then(res => {
            if (!res.ok) throw new Error("OTP endpoint returned " + res.status);
            return res.json();
          })
          .then(data => {
            console.log("📨 send-otp response:", data);
            if (data && data.ok) {
              // prefer showing OTP info near the OTP input (#otpMsg)
              showOtpInfo("OTP sent to your registered email.");
              otpInput && otpInput.focus();
            } else {
              // show OTP-specific error
              showOtpError("Failed to send OTP. " + (data && data.message ? data.message : ""));
            }
          })
          .catch(err => {
            console.error("send-otp error:", err);
            showOtpError("Failed to send OTP. Try again later.");
          });
      });
    }

    if (otpInput && confirmBtn) {
      otpInput.addEventListener("input", () => {
        confirmBtn.disabled = otpInput.value.trim().length !== 6;
      });
    }

    if (confirmBtn) {
      confirmBtn.addEventListener("click", (e) => {
        e && e.preventDefault && e.preventDefault();
        clearMessage();
        const p = window.selectedPayment;
        if (!p) { showOtpError("No payment selected."); return; }
        const otp = otpInput ? otpInput.value.trim() : "";
        if (!otp) { showOtpError("Please enter the OTP."); return; }

        console.log("💳 /api/payments/confirm ->", { fromAccount: p.account, billNo: p.billNo, biller: p.biller, amount: p.amount, otp });

        fetch("/api/payments/confirm", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ fromAccount: p.account, billNo: p.billNo, biller: p.biller, amount: p.amount, otp })
        })
          .then(res => {
            if (!res.ok) throw new Error("Confirm endpoint returned " + res.status);
            return res.json();
          })
          .then(data => {
            console.log("💳 /api/payments/confirm response:", data);
            hide($("paydetails"));
            if (data && data.ok) {
              const successConfirmEl = $("successConfirmNo");
              if (successConfirmEl) successConfirmEl.textContent = "Confirmation #: " + (data.confirmNo || "—");
              show($("paymentsuccess"));
            } else {
              const failedMsgEl = $("failedMessage");
              if (failedMsgEl) failedMsgEl.textContent = (data && data.message) ? data.message : "Payment failed.";
              show($("paymentfailed"));
            }
          })
          .catch(err => {
            console.error("confirm error:", err);
            showOtpError("Error confirming payment. Try again.");
          });
      });
    }
  }

  /* -------------------------
     Back button: return to form
     ------------------------- */
  function initBack() {
    const back = $("backBtn");
    if (!back) return;
    back.addEventListener("click", (e) => {
      e && e.preventDefault && e.preventDefault();
      clearMessage();
      hide($("paydetails"));
      hide($("paymentsuccess"));
      hide($("paymentfailed"));
      show($("universalForm"));
    });
  }

  /* -------------------------
     Account select update
     ------------------------- */
  function initAccountSelectListener() {
    const sel = $("univAccountSelect");
    if (!sel) return;
    sel.addEventListener("change", (e) => {
      updateBalanceCard(e.target.value);
    });
  }

  /* -------------------------
     Close Buttons + Print Receipt
     ------------------------- */
  function initCloseAndPrint() {
    const successClose = $("successCloseBtn");
    const failedClose = $("failedCloseBtn");
    const printBtn = $("printReceiptBtn");

    if (successClose) {
      successClose.addEventListener("click", (e) => {
        e && e.preventDefault && e.preventDefault();
        location.reload();
      });
    }

    if (failedClose) {
      failedClose.addEventListener("click", (e) => {
        e && e.preventDefault && e.preventDefault();
        location.reload();
      });
    }

    if (printBtn) {
      printBtn.addEventListener("click", (e) => {
        e && e.preventDefault && e.preventDefault();
        const p = window.selectedPayment;
        if (!p) return showError("Nothing to print.");

        const confirmText = $("successConfirmNo") ? $("successConfirmNo").textContent : "";
        const dateTime = new Date().toLocaleString();

        // Build URL for receipt.html
        const receiptUrl = `receipt.html?date=${encodeURIComponent(dateTime)}&biller=${encodeURIComponent(p.biller)}&billNo=${encodeURIComponent(p.billNo)}&account=${encodeURIComponent(p.account)}&amount=${encodeURIComponent(p.amount)}&confirm=${encodeURIComponent(confirmText)}`;

        window.open(receiptUrl, "_blank");
      });
    }
  }

  /* -------------------------
     Next button wiring helper
     ------------------------- */
  function initNext() {
    const next = $("univNextBtn");
    if (!next) return;
    next.addEventListener("click", onNextClicked);
  }

  /* -------------------------
     Init all
     ------------------------- */
  function init() {
    initBillerButtons();
    loadAccounts();
    initAccountSelectListener();
    initNext();
    initOtpAndConfirm();
    initBack();
    initCloseAndPrint();

    // default UI state
    switchToBiller("ceb");
    show($("universalForm"));
    hide($("paydetails"));
    hide($("paymentsuccess"));
    hide($("paymentfailed"));
  }

  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", init);
  else init();

})();
