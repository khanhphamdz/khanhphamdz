
// Hiệu ứng submit form
document.querySelector('.checkout-form').addEventListener('submit', function (e) {
    e.preventDefault();
    document.getElementById('checkout-success').style.display = 'flex';
    window.scrollTo({ top: 0, behavior: 'smooth' });
});

// Sử dụng API https://provinces.open-api.vn để lấy tỉnh/thành, quận/huyện, phường/xã
const provinceSelect = document.getElementById('province');
const districtSelect = document.getElementById('district');
const wardSelect = document.getElementById('ward');

// Load tỉnh/thành phố
fetch('https://provinces.open-api.vn/api/p/')
    .then(res => res.json())
    .then(data => {
        data.forEach(province => {
            const opt = document.createElement('option');
            opt.value = province.code;
            opt.textContent = province.name;
            provinceSelect.appendChild(opt);
        });
    });

// Khi chọn tỉnh/thành, load quận/huyện
provinceSelect.addEventListener('change', function () {
    const provinceCode = this.value;
    districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
    wardSelect.innerHTML = '<option value="">Chọn phường/xã</option>';
    if (!provinceCode) return;
    fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`)
        .then(res => res.json())
        .then(data => {
            if (data.districts && Array.isArray(data.districts)) {
                data.districts.forEach(district => {
                    const opt = document.createElement('option');
                    opt.value = district.code;
                    opt.textContent = district.name;
                    districtSelect.appendChild(opt);
                });
            }
        });
});

// Khi chọn quận/huyện, load phường/xã
districtSelect.addEventListener('change', function () {
    const districtCode = this.value;
    wardSelect.innerHTML = '<option value="">Chọn phường/xã</option>';
    if (!districtCode) return;
    fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`)
        .then(res => res.json())
        .then(data => {
            if (data.wards && Array.isArray(data.wards)) {
                data.wards.forEach(ward => {
                    const opt = document.createElement('option');
                    opt.value = ward.code;
                    opt.textContent = ward.name;
                    wardSelect.appendChild(opt);
                });
            }
        });
});

// Xử lý mã giảm giá (demo: mã 'SALE10' giảm 10%)
document.getElementById('apply-discount').onclick = function () {
    const code = document.getElementById('discount-select').value.trim();
    const message = document.getElementById('discount-message');
    const totalSpan = document.querySelector('.order-total-price');
    let total = 1299000;
    if (code === 'SALE10') {
        const discount = Math.round(total * 0.1);
        totalSpan.textContent = (total - discount).toLocaleString('vi-VN') + ' ₫';
        message.textContent = 'Áp dụng mã SALE10 thành công! Đã giảm 10%.';
        message.style.display = 'block';
        message.className = 'text-success mt-1';
    } else if (code === 'SALE20') {
        const discount = Math.round(total * 0.2);
        totalSpan.textContent = (total - discount).toLocaleString('vi-VN') + ' ₫';
        message.textContent = 'Áp dụng mã SALE20 thành công! Đã giảm 20%.';
        message.style.display = 'block';
        message.className = 'text-success mt-1';
    } else if (code) {
        message.textContent = 'Mã giảm giá không hợp lệ.';
        message.style.display = 'block';
        message.className = 'text-danger mt-1';
        totalSpan.textContent = total.toLocaleString('vi-VN') + ' ₫';
    } else {
        message.style.display = 'none';
        totalSpan.textContent = total.toLocaleString('vi-VN') + ' ₫';
    }
};