
// URL API cơ bản (điều chỉnh theo backend của bạn)
const API_URL = 'http://localhost:8080/api/auth';

// Xử lý submit form Đăng Nhập
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const errorDiv = document.getElementById('loginError');
    const successDiv = document.getElementById('loginSuccess');
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    const formData = new FormData(form);
    const data = {
        email: formData.get('email'),
        password: formData.get('password')
    };

    try {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Đăng nhập thất bại');
        }

        const result = await response.json();
        successDiv.textContent = 'Đăng nhập thành công!';
        successDiv.style.display = 'block';
        // Lưu dữ liệu người dùng hoặc token vào localStorage hoặc chuyển hướng
        localStorage.setItem('user', JSON.stringify(result));
        setTimeout(() => window.location.href = '/dashboard', 1000);
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.style.display = 'block';
    }
});

// Xử lý submit form Đăng Ký
document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const errorDiv = document.getElementById('registerError');
    const successDiv = document.getElementById('registerSuccess');
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    const formData = new FormData(form);
    const data = {
        name: formData.get('name'),
        email: formData.get('email'),
        password: formData.get('password'),
        confirmPassword: formData.get('confirmPassword')
    };

    try {
        const response = await fetch(`${API_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Đăng ký thất bại');
        }

        const result = await response.json();
        successDiv.textContent = 'Đăng ký thành công!';
        successDiv.style.display = 'block';
        // Lưu dữ liệu người dùng hoặc chuyển hướng
        localStorage.setItem('user', JSON.stringify(result));
        setTimeout(() => window.location.href = '/dashboard', 1000);
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.style.display = 'block';
    }
});
