document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('btn-forgot-password').addEventListener('click', getForgotPassword);
})
function getForgotPassword() {
    const email = document.getElementById('email').value;
    fetch('/api/auth/forgot-pasword', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: email,
        })
    })
        .then(response => response.json())
        .then(data => {
            if(data.status === "ok") {
                alert(data.message)
            }
            console.log(data);
        })
        .catch(error => console.error('Lỗi:', error));
}