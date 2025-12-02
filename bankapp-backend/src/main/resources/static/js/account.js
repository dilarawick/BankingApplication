document.addEventListener("DOMContentLoaded", function () {
    loadAccounts();  // Load accounts when the page loads

    // Add event listener to the Add Account button
    document.getElementById("addAccountSubmitBtn").addEventListener("click", addAccount);

    // Add event listener to the cancel button in Add Account form
    document.getElementById("cancelBtn").addEventListener("click", function () {
        document.getElementById("addAccountForm").style.display = "none"; // Hide form
        loadAccounts();  // Reload accounts to show cards again
    });

    // Add event listener to the cancel button in Edit Account form
    document.getElementById("cancelEditBtn").addEventListener("click", function () {
        document.getElementById("editAccountForm").style.display = "none"; // Hide edit form
        loadAccounts();  // Reload accounts to show cards again
    });

    // Add event listener to the Edit Account button
    document.getElementById("editAccountBtn").addEventListener("click", editAccountSubmit);
});

// ================================
// LOAD ACCOUNTS
// ================================
function loadAccounts() {
    fetch("/api/accounts/my", {
        method: "GET",
        credentials: "include"
    })
    .then(res => res.json())
    .then(data => {
        const row = document.querySelector(".row");
        row.innerHTML = "";

        if (!Array.isArray(data) || data.length === 0) {
            row.innerHTML = "<p>No accounts added yet.</p>";
        } else {
            data.forEach(acc => row.appendChild(createAccountCard(acc)));
        }

        row.appendChild(createAddCard()); // Add the "Add Account" card at the end
    })
    .catch(err => console.error("Error loading accounts:", err));
}

// ================================
// CREATE ACCOUNT CARD
// ================================
function createAccountCard(acc) {
    const card = document.createElement("div");
    card.className = "card";

    card.innerHTML = `
        <div style="display:flex; justify-content:space-between;">
            <img src="img/name.png" style="width:70px;" />
            <div>
                <img 
                    src="img/edit.png" width="22" 
                    style="cursor:pointer; margin-right:10px;" 
                    onclick="editAccount('${acc.accountNo}')"
                />
                <img 
                    src="img/delete.png" width="22" 
                    style="cursor:pointer;" 
                    onclick="deleteAccount('${acc.accountNo}')"
                />
            </div>
        </div>

        <h2 style="margin-top:20px;">${acc.nickname}</h2>

        <p style="margin-top:10px; font-size:18px; color:#555;">
            <b>Account No:</b> ${acc.accountNo} <br>
            <b>Type:</b> ${acc.accountType}
        </p>
    `;
    return card;
}

// ================================
// ADD (+) CARD
// ================================
function createAddCard() {
    const add = document.createElement("div");
    add.className = "card add-card";

    add.innerHTML = `
        <div class="add-btn" style="cursor:pointer; font-size:50px;">+</div>
        <p style="font-size:18px; margin-top:10px;">Add a Bank Account</p>
    `;

    add.addEventListener("click", showAddAccountForm); // Show the form when clicked
    return add;
}

// ================================
// SHOW ADD ACCOUNT FORM
// ================================
function showAddAccountForm() {
    const addForm = document.getElementById("addAccountForm");
    const editForm = document.getElementById("editAccountForm");

    addForm.style.display = "block"; // Show add account form
    editForm.style.display = "none"; // Hide edit form

    // Hide all the other cards (accounts and add card)
    const cards = document.querySelectorAll(".card");
    cards.forEach(card => card.style.display = "none");

    // Clear any previous messages
    clearFormMessages("addAccountMessage");
}

// ================================
// ADD ACCOUNT FORM SUBMISSION
// ================================
function addAccount() {
    const acc1 = document.getElementById("acc1").value;
    const acc2 = document.getElementById("acc2").value;
    const email = document.getElementById("email").value;
    const nickname = document.getElementById("nickname").value;

    // Clear old messages before displaying new ones
    clearFormMessages("addAccountMessage");

    // Validate form fields
    if (!acc1 || !acc2 || !email) {
        displayFormMessage("addAccountMessage", "All fields except nickname are required.", "error");
        return;
    }

    if (acc1 !== acc2) {
        displayFormMessage("addAccountMessage", "Account numbers do not match.", "error");
        return;
    }

    // Create the request body
    const data = {
        accountNo: acc1,
        email: email,
        nickname: nickname || "My Account"
    };

    // Send the data to the backend
    fetch("/api/accounts/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data),
        credentials: "include"
    })
    .then(res => res.json())
    .then(result => {
        if (result.status === "ok") {
            displayFormMessage("addAccountMessage", "Account added successfully!", "success");
            showSuccessAlert("Account added successfully!"); // Show success alert
            loadAccounts();  // Reload accounts to show updated list
            document.getElementById("addAccountForm").style.display = "none"; // Hide add form
        } else {
            displayFormMessage("addAccountMessage", result.message || "Failed to add account.", "error");
        }
    })
    .catch(err => {
        console.error("Error adding account:", err);
        displayFormMessage("addAccountMessage", "Failed to add account. Please try again.", "error");
    });
}

// ================================
// SHOW EDIT ACCOUNT FORM
// ================================
function editAccount(accountNo) {
    const form = document.getElementById("editAccountForm");
    form.style.display = "block";  // Show edit form
    const addForm = document.getElementById("addAccountForm");
    addForm.style.display = "none";  // Hide add form

    const accountNumberField = document.getElementById("editAccNo");
    accountNumberField.value = accountNo;  // Set the account number for editing

    // Hide all the other cards (accounts and add card)
    const cards = document.querySelectorAll(".card");
    cards.forEach(card => card.style.display = "none");

    // Clear any previous messages
    clearFormMessages("editAccountMessage");
}

// ================================
// UPDATE ACCOUNT FORM SUBMISSION
// ================================
function editAccountSubmit() {
    const accountNo = document.getElementById("editAccNo").value;
    const nickname = document.getElementById("editNickname").value;

    if (!nickname) {
        displayFormMessage("editAccountMessage", "Nickname is required.", "error");
        return;
    }

    const data = {
        accountNo: accountNo,
        nickname: nickname
    };

    // Send the data to the backend
    fetch("/api/accounts/update-nickname", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data),
        credentials: "include"
    })
    .then(res => res.json())
    .then(result => {
        if (result.status === "ok") {
            displayFormMessage("editAccountMessage", "Nickname updated successfully!", "success");
            showSuccessAlert("Nickname updated successfully!"); // Show success alert
            loadAccounts();  // Reload accounts to show updated list
            document.getElementById("editAccountForm").style.display = "none"; // Hide edit form
        } else {
            displayFormMessage("editAccountMessage", result.message || "Failed to update nickname.", "error");
        }
    })
    .catch(err => {
        console.error("Error updating nickname:", err);
        displayFormMessage("editAccountMessage", "Failed to update nickname. Please try again.", "error");
    });
}

// ================================
// DELETE ACCOUNT
// ================================
function deleteAccount(accountNo) {
    // Create confirmation card element
    const confirmationCard = document.createElement("div");
    confirmationCard.className = "confirmation-card";
    confirmationCard.innerHTML = `
        <div class="confirmation-card-content">
            <p>Are you sure you want to delete this account?</p>
            <button onclick="confirmDeleteAccount('${accountNo}')">Yes, Delete</button>
            <button onclick="cancelDelete()">Cancel</button>
        </div>
    `;
    
    // Append confirmation card to the body
    document.body.appendChild(confirmationCard);

    // Apply blur effect to the background
    document.querySelector("main").style.filter = "blur(5px)";
    document.querySelector("aside").style.filter = "blur(5px)";

    // Center the confirmation card
    confirmationCard.style.display = "flex";
    confirmationCard.style.position = "fixed";
    confirmationCard.style.top = "50%";
    confirmationCard.style.left = "50%";
    confirmationCard.style.transform = "translate(-50%, -50%)";
    confirmationCard.style.zIndex = "9999"; // Ensure it appears on top of the blurred background
}

// ================================
// CONFIRM DELETE ACTION
// ================================
function confirmDeleteAccount(accountNo) {
    fetch(`/api/accounts/delete/${accountNo}`, {
        method: "DELETE",
        credentials: "include"
    })
    .then(res => res.json())
    .then(result => {
        if (result.status === "ok") {
            loadAccounts();  // Reload accounts to show updated list
            cancelDelete();  // Hide the confirmation card
            showSuccessAlert("Account deleted successfully!"); // Show success alert
        } else {
            alert("Failed to delete account.");
        }
    })
    .catch(err => {
        console.error("Error deleting account:", err);
        alert("Failed to delete account. Please try again.");
    });
}

// ================================
// CANCEL DELETE ACTION
// ================================
function cancelDelete() {
    const confirmationCard = document.querySelector(".confirmation-card");
    if (confirmationCard) {
        confirmationCard.remove(); // Remove the confirmation card
    }

    // Remove the background blur
    document.querySelector("main").style.filter = "none";
    document.querySelector("aside").style.filter = "none";
}

// ================================
// SUCCESS ALERT FUNCTION
// ================================
function showSuccessAlert(message) {
    const alert = document.createElement("div");
    alert.classList.add("success-alert");
    alert.textContent = message;

    // Append the alert to the body
    document.body.appendChild(alert);

    // Set a timeout to hide the alert after 3 seconds
    setTimeout(() => {
        alert.classList.add("fade-out"); // Add fade-out class to hide the alert
        // After the fade-out transition is complete, remove the alert from DOM
        setTimeout(() => {
            alert.remove();
        }, 500); // Time for the fade-out effect to complete (500ms)
    }, 3000); // Show the alert for 3 seconds
}

// ================================
// DISPLAY FORM MESSAGE (SUCCESS/ERROR)
// ================================
function displayFormMessage(formId, message, type) {
    const formMessage = document.getElementById(formId);
    formMessage.textContent = message;
    formMessage.className = `form-message ${type}`;
}

// ================================
// CLEAR FORM MESSAGES
// ================================
function clearFormMessages(formId) {
    const formMessage = document.getElementById(formId);
    formMessage.textContent = "";
    formMessage.className = "form-message"; // Reset to default style
}
