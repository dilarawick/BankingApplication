const buttons = document.querySelectorAll(".utility-btn");
        const forms = document.querySelectorAll(".bill-form");

        buttons.forEach(button => {
            button.addEventListener("click", () => {
                // hide all
                forms.forEach(form => form.style.display = "none");

                // show correct form
                const id = button.getAttribute("data-form");
                document.getElementById(id).style.display = "block";
            });
        });

        // NEXT BUTTON LOGIC
const nextButtons = document.querySelectorAll(".pbnext-btn");

nextButtons.forEach(btn => {
    btn.addEventListener("click", () => {
        // find parent form
        const currentForm = btn.closest(".bill-form");
        // hide current form
        currentForm.style.display = "none";

        // get next form ID
        const nextID = btn.getAttribute("data-next");
        // show next form
        document.getElementById(nextID).style.display = "block";
    });
});

const nextBtn = document.getElementById('cebnextBtn');
const backBtn = document.getElementById('backBtn');
const confirmBtn = document.getElementById('confirmBtn');
const cebForm = document.getElementById('cebForm');
const form2 = document.getElementById('form2');

nextBtn.addEventListener('click', () => {
    const nameValue = document.getElementById('cebName').value;
    const amountValue = document.getElementById('cebAmount').value;

    // Hide first form, show confirmation
    cebForm.style.display = 'none';
    form2.style.display = 'block';

    // Fill confirmation details
    document.getElementById('confirmName').textContent = nameValue;
    document.getElementById('confirmAmount').textContent = amountValue;
});

// Back button: go back to edit
backBtn.addEventListener('click', () => {
    form2.style.display = 'none';
    cebForm.style.display = 'block';
});

// Confirm button: handle confirmation
confirmBtn.addEventListener('click', () => {
    alert("Payment Confirmed!\nName: " + document.getElementById('confirmName').textContent +
          "\nAmount: $" + document.getElementById('confirmAmount').textContent);

    // Optionally, reset forms
    cebForm.reset();
    form2.style.display = 'none';
    cebForm.style.display = 'block';
});
