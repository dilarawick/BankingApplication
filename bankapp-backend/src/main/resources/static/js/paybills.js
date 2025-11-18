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
        