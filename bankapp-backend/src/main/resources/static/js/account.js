// simple DOM handlers
document.addEventListener('DOMContentLoaded', function () {
    const editBtn = document.querySelector('.edit-btn');
    const deleteBtn = document.querySelector('.delete-btn');
    const addBtn = document.querySelector('.add-btn');

    if (editBtn) {
        editBtn.addEventListener('click', function (e) {
            e.preventDefault();
            alert('Edit clicked — open edit modal or page here.');
        });
    }

    if (deleteBtn) {
        deleteBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const ok = confirm('Are you sure you want to delete this account?');
            if (ok) {
                // simulate delete
                alert('Account deleted (simulate).');
                // optionally hide card:
                // document.querySelector('.account-card').style.display = 'none';
            }
        });
    }

    if (addBtn) {
        addBtn.addEventListener('click', function (e) {
            e.preventDefault();
            alert('Open "Add Account" form/modal here.');
        });
    }
});