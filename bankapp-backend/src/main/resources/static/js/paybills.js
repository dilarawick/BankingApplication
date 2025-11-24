/* === paybills.js (clean, single-file) ===
   Replace your current js/paybills.js with this file.
   Notes:
   - This file assumes the exact IDs present in your HTML.
   - It is defensive (tries to find the select inside the active form).
   - It stores the currently-selected biller when a utility button is clicked.
*/

(function () {
  // --- helpers ---
  const $ = id => document.getElementById(id);
  const qs = sel => document.querySelector(sel);

  function safeText(el) { return el ? (el.textContent || el.value || "") : ""; }

  function show(el) { if (!el) return; el.style.display = "block"; }
  function hide(el) { if (!el) return; el.style.display = "none"; }

  // Map of biller forms and field ids (keeps logic explicit)
  const BILLERS = {
    ceb: {
      billerKey: "CEB",
      amountId: "cebAmount",
      bill1Id: "cebbillno",
      bill2Id: "recebbillno",
      formId: "cebForm",
      nextBtn: "cebnextBtn"
    },
    water: {
      billerKey: "NWSDB",
      amountId: "waterAmount",
      bill1Id: "waterbillno",
      bill2Id: "rewaterbbillno", // note name as in HTML
      formId: "nwsdbForm",
      nextBtn: "waternextBtn"
    },
    slt: {
      billerKey: "SLT",
      amountId: "sltAmount",
      bill1Id: "sltbillno",
      bill2Id: "resltbillno",
      formId: "sltform",
      nextBtn: "sltnextBtn"
    },
    dialog: {
      billerKey: "Dialog",
      amountId: "dialogAmount",
      bill1Id: "dialogbillno",
      bill2Id: "redialogbillno",
      formId: "dialogform",
      nextBtn: "dialognextbtn"
    }
  };

  // Convenience: get select (account) for a given form element
  function getAccountFromForm(formEl) {
    if (!formEl) return "";
    // prefer a select inside that form
    const sel = formEl.querySelector("select");
    if (sel && sel.value) return sel.value;
    // fallback to global accountSelect (CEB uses this)
    const globalSel = $("accountSelect");
    return globalSel ? globalSel.value : "";
  }

  // Clears form fields for a given config
  function clearFormFields(cfg) {
    const a = $(cfg.amountId);
    const b1 = $(cfg.bill1Id);
    const b2 = $(cfg.bill2Id);
    if (a) a.value = "";
    if (b1) b1.value = "";
    if (b2) b2.value = "";
  }

  // Show paydetails with values and save selectedPayment
  function showPayDetails(cfg, account, billNo, amount) {
    hide($(cfg.formId));
    show($("paydetails"));

    $("confirmName").textContent = billNo;
    $("confirmAmount").textContent = amount;
    $("bpdate").textContent = new Date().toLocaleString();

    // Save for later confirm step
    window.selectedPayment = {
      account: account,
      billNo: billNo,
      amount: amount,
      biller: cfg.billerKey,
      formId: cfg.formId
    };
  }

  // Generic validate-and-check function used by all Next buttons
  function validateAndCheck(cfg) {
    const formEl = $(cfg.formId);
    const account = getAccountFromForm(formEl);
    const amountEl = $(cfg.amountId);
    const bill1El = $(cfg.bill1Id);
    const bill2El = $(cfg.bill2Id);

    const amount = amountEl ? amountEl.value.trim() : "";
    const bill1 = bill1El ? bill1El.value.trim() : "";
    const bill2 = bill2El ? bill2El.value.trim() : "";
    const biller = cfg.billerKey;

    // Basic front-end validations
    if (!account) {
      alert("Please select an account.");
      clearFormFields(cfg);
      return;
    }
    if (!bill1 || !bill2 || bill1 !== bill2) {
      alert("Bill numbers do not match.");
      clearFormFields(cfg);
      return;
    }
    // ensure amount is a positive number
    const amountNum = Number(amount);
    if (!amount || isNaN(amountNum) || amountNum <= 0) {
      alert("Please enter a valid amount.");
      clearFormFields(cfg);
      return;
    }




    console.log("🔎 Checking Bill:", {
    billNo: bill1,
    biller: biller
});


    // Ask backend if bill exists (pre-confirm check)
    fetch("/api/bills/check", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ billNo: bill1, biller: biller })
    })
      .then(res => res.json())
      .then(data => {
        if (!data || !data.exists) {
          alert("This bill number does not exist.");
          clearFormFields(cfg);
          return;
        }
        // Success → show pay details and store state
        showPayDetails(cfg, account, bill1, amountNum);
      })
      .catch(err => {
        console.error("Error checking bill:", err);
        alert("An error occurred while checking the bill. Try again.");
        clearFormFields(cfg);
      });
  }

  // --- Setup UI: utility buttons (biller selection + show forms) ---
  function initUtilityButtons() {
    const buttons = document.querySelectorAll(".utility-btn");
    buttons.forEach(button => {
      button.addEventListener("click", () => {
        // hide all forms
        const forms = document.querySelectorAll(".bill-form");
        forms.forEach(f => f.style.display = "none");

        // show correct form via data-form
        const id = button.getAttribute("data-form");
        const targetForm = $(id);
        if (targetForm) {
          targetForm.style.display = "block";
        }

        // set window.selectedBiller based on the visible label in the same wrapper
        // button is inside .btn-wrapper; find its sibling .pbbtn-label
        const wrapper = button.closest(".btn-wrapper");
        let label = null;
        if (wrapper) {
          const l = wrapper.querySelector(".pbbtn-label");
          if (l) label = l.textContent.trim();
        }
        if (!label) {
          // fallback: read alt text of image
          const img = button.querySelector("img");
          if (img) label = (img.alt || "").trim();
        }
        if (label) {
          // normalize to DB expected values
          if (label.toUpperCase() === "CEB") window.selectedBiller = "CEB";
          else if (label.toUpperCase() === "SLT") window.selectedBiller = "SLT";
          else if (label.toUpperCase() === "NWSDB") window.selectedBiller = "NWSDB";
          else if (label.toUpperCase() === "DIALOG") window.selectedBiller = "Dialog";
          else window.selectedBiller = label; // fallback
        }
      });
    });
  }

  // --- Hook Next buttons to validation function ---
  function initNextButtons() {
    // explicit wiring for each biller (safer than trying to auto-infer)
    if ($(BILLERS.ceb.nextBtn)) {
      $(BILLERS.ceb.nextBtn).addEventListener("click", () => validateAndCheck(BILLERS.ceb));
    }
    if ($(BILLERS.water.nextBtn)) {
      $(BILLERS.water.nextBtn).addEventListener("click", () => validateAndCheck(BILLERS.water));
    }
    if ($(BILLERS.slt.nextBtn)) {
      $(BILLERS.slt.nextBtn).addEventListener("click", () => validateAndCheck(BILLERS.slt));
    }
    if ($(BILLERS.dialog.nextBtn)) {
      $(BILLERS.dialog.nextBtn).addEventListener("click", () => validateAndCheck(BILLERS.dialog));
    }
  }

  // --- Back button on paydetails: return to original form ---
  function initBackButton() {
    const back = $("backBtn");
    if (!back) return;
    back.addEventListener("click", () => {
      hide($("paydetails"));
      // return to original form if known
      const prev = window.selectedPayment && window.selectedPayment.formId;
      if (prev && $(prev)) {
        show($(prev));
      } else {
        // fallback: show the CEB form
        show($("cebForm"));
      }
    });
  }

  // --- OTP & Confirm flow ---
  function initOtpAndConfirm() {
    const sendOtpBtn = $("sendOtpBtn");
    const confirmPayBtn = $("confirmPayBtn");
    const otpInput = $("otpInput");

    // When send OTP clicked: call backend to send OTP to customer's email
    if (sendOtpBtn) {
      sendOtpBtn.addEventListener("click", () => {
        fetch("/api/payments/send-otp-for-payment", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({})
        })
          .then(res => res.json())
          .then(data => {
            if (data && data.ok) {
              alert("OTP sent to your email.");
              // Allow user to enter OTP and enable confirm when 6 digits typed
              if (otpInput) otpInput.focus();
            } else {
              alert("Failed to send OTP. Try again.");
            }
          })
          .catch(err => {
            console.error("send-otp error:", err);
            alert("Failed to send OTP. Try again.");
          });
      });
    }

    // enable confirm button only when OTP length looks correct
    if (otpInput && confirmPayBtn) {
      otpInput.addEventListener("input", () => {
        const v = otpInput.value.trim();
        confirmPayBtn.disabled = v.length !== 6;
      });
    }

    // Final confirm: send payment request with otp
    if (confirmPayBtn) {
      confirmPayBtn.addEventListener("click", () => {
        const p = window.selectedPayment;
        if (!p) {
          alert("No payment selected.");
          return;
        }
        const otp = otpInput ? otpInput.value.trim() : "";
        if (!otp) {
          alert("Enter the OTP.");
          return;
        }

        fetch("/api/payments/confirm", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            fromAccount: p.account,
            billNo: p.billNo,
            biller: p.biller,
            amount: p.amount,
            otp: otp
          })
        })
          .then(res => res.json())
          .then(data => {
            if (data && data.ok) {
              alert("Payment Successful! Confirmation #: " + data.confirmNo);
              // show success card if you want:
              // show($("paymentsuccess"));
              // hide($("paydetails"));
              // reset form(s)
              if (p.formId && $(p.formId)) {
                // clear the original form fields
                const cfgKey = Object.keys(BILLERS).find(k => BILLERS[k].formId === p.formId);
                if (cfgKey) clearFormFields(BILLERS[cfgKey]);
              }
              hide($("paydetails"));
              show($("cebForm")); // or go somewhere else
            } else {
              const msg = (data && data.message) ? data.message : "Payment failed";
              alert("Payment Failed: " + msg);
              // optionally show failure UI
              // show($("paymentfailed"));
            }
          })
          .catch(err => {
            console.error("confirm payment error:", err);
            alert("An error occurred while confirming the payment.");
          });
      });
    }
  }

  // --- Utility: load dashboard and account select (existing code adapted) ---
  function loadDashboard() {
    fetch("/api/dashboard/me")
      .then(res => res.json())
      .then(data => {
        if (!data || !data.ok) {
          const balanceEl = $("balance");
          if (balanceEl) balanceEl.innerText = "Not logged in";
          return;
        }
        // Fill account info (primary shown in balance areas)
        const primary = data.accounts && data.accounts.find(acc => acc.isPrimary === true);
        if (primary) {
          const balanceEls = document.querySelectorAll("#balance");
          balanceEls.forEach(e => e.innerText = "Rs. " + primary.balance);
          const accountTypeEls = document.querySelectorAll("#accountType");
          accountTypeEls.forEach(e => e.innerText = primary.accountType);
          // set some accountName if present
          const nameEl = $("accountName");
          if (nameEl) nameEl.innerText = data.name || "";
        } else {
          const balanceEl = $("balance");
          if (balanceEl) balanceEl.innerText = "No accounts found";
        }
      })
      .catch(err => {
        console.error("loadDashboard error:", err);
      });
  }

  function loadAccountSelect() {
    fetch("/api/dashboard/me")
      .then(res => res.json())
      .then(data => {
        if (!data || !data.ok) return;
        const select = $("accountSelect");
        if (!select) return;
        // clear previous options
        select.innerHTML = '<option value="">-- Choose Account --</option>';
        (data.accounts || []).forEach(acc => {
          const opt = document.createElement("option");
          opt.value = acc.accountNo;
          opt.textContent = `${acc.accountNo} - Rs. ${acc.balance}`;
          select.appendChild(opt);
        });
        // also update selects inside other forms if they have duplicate ids
        // Note: many forms have selects with id "pbbankacc" or duplicate - we won't overwrite those
      })
      .catch(err => console.error("loadAccountSelect error:", err));
  }

  const accountSelect = document.getElementById("accountSelect");

if (accountSelect) {
    accountSelect.addEventListener("change", () => {
        const selectedAccNo = accountSelect.value;

        if (!selectedAccNo || !window.allAccounts) return;

        // find selected account
        const acc = window.allAccounts.find(a => a.accountNo === selectedAccNo);

        if (acc) {
            document.getElementById("balance").textContent = "Rs. " + acc.balance;
            document.getElementById("accountType").textContent = acc.accountType;
        }
    });
}


  // --- Initialization ---
  function initAll() {
    initUtilityButtons();
    initNextButtons();
    initBackButton();
    initOtpAndConfirm();
    loadDashboard();
    loadAccountSelect();

    // initial UI: hide all bill forms and show nothing, or show cebForm by default
    const forms = document.querySelectorAll(".bill-form");
    forms.forEach(f => f.style.display = "none");
    // show ceb by default
    const defaultForm = $("cebForm");
    if (defaultForm) defaultForm.style.display = "block";

    // default selectedBiller
    window.selectedBiller = "CEB";

    // reset OTP UI
    const otpArea = $("otpArea");
    if (otpArea) {
      // keep visible but confirm disabled until OTP typed
      const cp = $("confirmPayBtn");
      if (cp) cp.disabled = true;
    }
  }

  // run on DOM ready
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initAll);
  } else {
    initAll();
  }
})();
