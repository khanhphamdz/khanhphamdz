document.addEventListener("DOMContentLoaded", function () {
    const registerForm = document.getElementById("registerForm");
    const registerError = document.getElementById("registerError");
    const registerSuccess = document.getElementById("registerSuccess");

    registerForm.addEventListener("submit", async function (e) {
        e.preventDefault(); // Ngăn reload trang

        // Lấy dữ liệu từ form
        const name = document.getElementById("registerName").value.trim();
        const email = document.getElementById("registerEmail").value.trim();
        const password = document.getElementById("registerPassword").value;
        const confirmPassword = document.getElementById("registerConfirmPassword").value;

        // Xóa thông báo cũ
        registerError.style.display = "none";
        registerSuccess.style.display = "none";

        // Kiểm tra mật khẩu
        if (password !== confirmPassword) {
            registerError.textContent = "Mật khẩu xác nhận không khớp.";
            registerError.style.display = "block";
            return;
        }

        try {
            const res = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    name,
                    email,
                    password
                })
            });

            const data = await res.json();
            console.log(data);
            
            if (data.status === "ok") {
                registerSuccess.textContent = data.message || "Đăng ký thành công!";
                registerSuccess.style.display = "block";
                registerForm.reset();
            } else {
                registerError.textContent = data.message || "Đăng ký thất bại.";
                registerError.style.display = "block";
            }

        } catch (error) {
            console.error("Lỗi khi đăng ký:", error);
            registerError.textContent = "Đã xảy ra lỗi, vui lòng thử lại sau.";
            registerError.style.display = "block";
        }
    });
});
