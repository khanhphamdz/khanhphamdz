// Dữ liệu variants từ server 
const variants = /*[[${variants}]][];
const basePrice = /*[[${product.basePrice}]]*/ 299000;

// Debug: Kiểm tra dữ liệu variants
console.log('Variants data:', variants);
console.log('Base price:', basePrice);

let selectedColorId = null;
let selectedSizeId = null;
let currentVariant = null;

// Xử lý chọn màu
document.querySelectorAll('input[name="selectedColor"]').forEach(colorInput => {
    colorInput.addEventListener('change', function () {
        selectedColorId = this.value;
        console.log('Selected color ID:', selectedColorId);
        document.querySelectorAll('.color-circle').forEach(circle => circle.classList.remove('active'));
        this.nextElementSibling.classList.add('active');
        updateProductInfo();
    });
});

// Xử lý chọn size
document.querySelectorAll('.size-option').forEach(sizeDiv => {
    sizeDiv.addEventListener('click', function () {
        selectedSizeId = this.dataset.sizeId;
        console.log('Selected size ID:', selectedSizeId);
        document.querySelectorAll('.size-option').forEach(div => div.classList.remove('active'));
        this.classList.add('active');
        updateProductInfo();
    });
});

// Cập nhật thông tin sản phẩm theo variant
function updateProductInfo() {
    console.log('updateProductInfo called');
    console.log('Selected color ID:', selectedColorId, typeof selectedColorId);
    console.log('Selected size ID:', selectedSizeId, typeof selectedSizeId);
    console.log('Available variants:', variants);

    if (selectedColorId && selectedSizeId) {
        currentVariant = variants.find(v => {
            console.log(`Checking variant: colorId=${v.colorId} (${typeof v.colorId}), sizeId=${v.sizeId} (${typeof v.sizeId})`);
            return v.colorId == selectedColorId && v.sizeId == selectedSizeId;
        });

        console.log('Found variant:', currentVariant);

        if (currentVariant) {
            // Cập nhật giá
            document.getElementById('current-price').textContent =
                new Intl.NumberFormat('vi-VN').format(currentVariant.price || basePrice) + ' VNĐ';

            // Cập nhật tồn kho
            console.log('Updating stock quantity:', currentVariant.quantityInStock);
            document.getElementById('stock-quantity').textContent = currentVariant.quantityInStock || 0;

            // Cập nhật trạng thái nút add to cart
            const addToCartBtn = document.getElementById('add-to-cart-btn');
            if (currentVariant.quantityInStock > 0) {
                addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Thêm vào giỏ hàng';
            } else {
                addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Hết hàng';
            }

            // Cập nhật max quantity input
            document.getElementById('quantity-input').max = currentVariant.quantityInStock;
        } else {
            console.log('No variant found for selected color and size');
            document.getElementById('stock-quantity').textContent = 'Không có sẵn';
        }
    } else {
        // Reset về giá gốc
        document.getElementById('current-price').textContent =
            new Intl.NumberFormat('vi-VN').format(basePrice) + ' VNĐ';
        document.getElementById('stock-quantity').textContent = '--';
        console.log('Not enough selections - color:', selectedColorId, 'size:', selectedSizeId);
    }
}

// Validation cho quantity input
function validateQuantityInput(input) {
    // Loại bỏ ký tự không phải số và dấu ch ấm/phẩy
    let value = input.value.replace(/[^0-9]/g, '');

    // Nếu rỗng, set về 1
    if (value === '') {
        value = '1';
    }

    // Parse thành số
    let num = parseInt(value);

    // Đảm bảo min = 1
    if (num < 1) {
        num = 1;
    }

    // Đảm bảo không vượt quá stock nếu có
    const max = parseInt(input.max) || 999;
    if (num > max && max !== 999) {
        num = max;
        alert(`Chỉ còn ${max} sản phẩm trong kho`);
    }

    input.value = num;
}

// Validation cho số lượng
function isValidQuantity(quantity) {
    // Kiểm tra rỗng
    if (!quantity || quantity.toString().trim() === '') {
        alert('Vui lòng nhập số lượng');
        return false;
    }

    // Parse thành số
    const num = parseInt(quantity);

    // Kiểm tra không phải số
    if (isNaN(num)) {
        alert('Số lượng phải là số');
        return false;
    }

    // Kiểm tra số âm hoặc 0
    if (num <= 0) {
        alert('Số lượng phải lớn hơn 0');
        return false;
    }

    // Kiểm tra số thập phân (chứa dấu . hoặc ,)
    if (quantity.toString().includes('.') || quantity.toString().includes(',')) {
        alert('Số lượng phải là số nguyên');
        return false;
    }

    return true;
}

// Validation cho tồn kho
function isStockAvailable(quantity) {
    if (!currentVariant) {
        return false;
    }

    const stock = currentVariant.quantityInStock || 0;
    const num = parseInt(quantity);

    if (stock <= 0) {
        alert('Sản phẩm hiện đã hết hàng');
        return false;
    }

    if (num > stock) {
        alert(`Chỉ còn ${stock} sản phẩm trong kho, không thể thêm ${num} sản phẩm`);
        return false;
    }

    return true;
}

// Xử lý quantity cải thiện
function increaseQuantity() {
    const input = document.getElementById('quantity-input');
    const current = parseInt(input.value) || 1;
    const max = parseInt(input.max) || 999;

    if (current < max) {
        input.value = current + 1;
    } else if (max !== 999) {
        alert(`Chỉ còn ${max} sản phẩm trong kho`);
    }
}

function decreaseQuantity() {
    const input = document.getElementById('quantity-input');
    const current = parseInt(input.value) || 1;

    if (current > 1) {
        input.value = current - 1;
    }
    // Không cho xuống dưới 1
}

document.addEventListener('DOMContentLoaded', function () {
    const firstColor = document.querySelector('input[name="selectedColor"]');
    const firstSize = document.querySelector('.size-option');

    if (firstColor && firstSize) {
        // Chọn màu và size đầu tiên
        firstColor.checked = true;
        selectedColorId = firstColor.value;
        selectedSizeId = firstSize.dataset.sizeId;

        // Trigger events để cập nhật UI
        firstColor.dispatchEvent(new Event('change'));
        firstSize.click();

        // Cập nhật giá ngay lập tức
        updateProductInfo();
    } else {
        // Nếu không có variant, hiển thị thông báo
        document.getElementById('stock-quantity').textContent = 'Liên hệ';
        document.getElementById('add-to-cart-btn').innerHTML = '<i class="fas fa-shopping-cart"></i> Liên hệ để đặt hàng';
    }
});

document.getElementById('add-to-cart-btn').addEventListener('click',  function () {
    // Lấy dữ liệu từ giao diện
    console.log('add to cart');

});
async function isUserLoggedIn() {
    try {
        const res = await fetch('/api/auth/me', { method: 'GET' })
        if (!res.ok) {
            return false;
        }
        const data = await res.json()
        return data;
        console.log(data);
        
    } catch (error) {
        console.log('error checking login: ', error);
        return false;
    }
}
isUserLoggedIn();
// Hàm hiển thị toast thông báo
function showToast(message, type = 'success') {
    let toast = document.getElementById('custom-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'custom-toast';
        toast.style.position = 'fixed';
        toast.style.top = '20px';
        toast.style.right = '20px';
        toast.style.zIndex = '9999';
        toast.style.minWidth = '200px';
        toast.style.padding = '12px 24px';
        toast.style.borderRadius = '8px';
        toast.style.color = '#fff';
        toast.style.fontSize = '16px';
        toast.style.boxShadow = '0 2px 8px rgba(0,0,0,0.15)';
        document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.style.background = type === 'success' ? '#28a745' : '#dc3545';
    toast.style.display = 'block';
    setTimeout(() => {
        toast.style.display = 'none';
    }, 1800);
}

async function addToCart(variantId, sizeId, colorId, quantity, price, img, name, colorName, sizeName) {
    const loggedIn = await isUserLoggedIn();
    console.log("login: ", loggedIn);

    if (loggedIn && loggedIn.isLoggedIn === true) {
        // Đã đăng nhập: gọi API backend
        await fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                variantId: variantId,
                quantity: quantity
            })
        });
        // Sau khi thêm, lấy lại giỏ hàng từ DB để đồng bộ localStorage
        const response = await fetch('/api/cart', {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });
        const dbCart = await response.json();
        // dbCart.data là mảng CartItemDTO
        localStorage.setItem('cart', JSON.stringify(dbCart.data || []));
    }
    updateCartBadge();
    renderCartOffcanvas();
    showToast('Đã thêm vào giỏ hàng!');
}


