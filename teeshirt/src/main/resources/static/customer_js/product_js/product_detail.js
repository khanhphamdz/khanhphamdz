// Dữ liệu variants từ server 
let variants = [];
let basePrice = 299000;
let productId = 0;

// Lấy dữ liệu từ server khi trang load
document.addEventListener('DOMContentLoaded', function() {
    if (window.productData) {
        variants = window.productData.variants || [];
        basePrice = window.productData.basePrice || 299000;
        productId = window.productData.productId || 0;
    }
    
    // Debug: Kiểm tra dữ liệu variants
    console.log('Variants data:', variants);
    console.log('Base price:', basePrice);
    console.log('Product ID:', productId);
    
    // Khởi tạo các event listeners
    initializeEventListeners();
    autoSelectFirstOptions();
    initializeWishlistState();
});

let selectedColorId = null;
let selectedSizeId = null;
let currentVariant = null;

// Khởi tạo event listeners
function initializeEventListeners() {
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
}

// Cập nhật thông tin sản phẩm theo variant
function updateProductInfo() {
    console.log('updateProductInfo called');
    console.log('Selected color ID:', selectedColorId, typeof selectedColorId);
    console.log('Selected size ID:', selectedSizeId, typeof selectedSizeId);
    console.log('Available variants:', variants);

    const addToCartBtn = document.getElementById('add-to-cart-btn');
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
            if (currentVariant.quantityInStock > 0) {
                addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Thêm vào giỏ hàng';
                addToCartBtn.disabled = false;
            } else {
                addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Hết hàng';
                addToCartBtn.disabled = true;
            }

            // Cập nhật max quantity input
            document.getElementById('quantity-input').max = currentVariant.quantityInStock;
        } else {
            console.log('No variant found for selected color and size');
            document.getElementById('stock-quantity').textContent = 'Không có sẵn';
            addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Hết hàng';
            addToCartBtn.disabled = true;
        }
    } else {
        // Reset về giá gốc
        document.getElementById('current-price').textContent =
            new Intl.NumberFormat('vi-VN').format(basePrice) + ' VNĐ';
        document.getElementById('stock-quantity').textContent = '--';
        addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Thêm vào giỏ hàng';
        addToCartBtn.disabled = true;
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

// Toggle wishlist (thêm hoặc xóa)
async function addToWishlist() {
    if (!productId) {
        showToast('Không tìm thấy thông tin sản phẩm', 'error');
        return;
    }
    
    try {
        const wishlistBtn = document.getElementById('wishlist-btn');
        const wishlistText = document.getElementById('wishlist-text');
        
        // Kiểm tra trạng thái hiện tại
        const isInWishlist = wishlistBtn.classList.contains('btn-danger');
        const url = isInWishlist ? `/wishlist/remove/${productId}` : `/wishlist/add/${productId}`;
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const result = await response.text();
        
        if (result.startsWith('success:')) {
            if (isInWishlist) {
                // Xóa khỏi wishlist
                wishlistBtn.classList.remove('btn-danger');
                wishlistBtn.classList.add('btn-success');
                wishlistText.textContent = 'Yêu thích';
            } else {
                // Thêm vào wishlist
                wishlistBtn.classList.remove('btn-success');
                wishlistBtn.classList.add('btn-danger');
                wishlistText.textContent = 'Đã yêu thích';
            }
            // Cập nhật số lượng wishlist trên header
            if (typeof updateWishlistCount === 'function') {
                updateWishlistCount();
            }
        } else {
            showToast(result.replace('error:', ''), 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra', 'error');
    }
}

// Khởi tạo trạng thái wishlist từ server
async function initializeWishlistState() {
    if (!productId) return;
    
    try {
        console.log('Checking wishlist status for product:', productId);
        const response = await fetch(`/wishlist/check/${productId}`);
        
        if (response.ok) {
            const isInWishlist = await response.text() === 'true';
            console.log('Product is in wishlist:', isInWishlist);
            
            const wishlistBtn = document.getElementById('wishlist-btn');
            const wishlistText = document.getElementById('wishlist-text');
            
            if (isInWishlist) {
                wishlistBtn.classList.remove('btn-success');
                wishlistBtn.classList.add('btn-danger');
                wishlistText.textContent = 'Đã yêu thích';
            } else {
                wishlistBtn.classList.remove('btn-danger');
                wishlistBtn.classList.add('btn-success');
                wishlistText.textContent = 'Yêu thích';
            }
        }
    } catch (error) {
        console.log('Error checking wishlist status:', error);
    }
}

// Auto-select first options if available và hiển thị giá đúng
function autoSelectFirstOptions() {
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
}

document.getElementById('add-to-cart-btn').addEventListener('click', function () {
    if (!currentVariant) {
        showToast('Vui lòng chọn đầy đủ màu sắc và kích thước!', 'error');
        return;
    }
    if (currentVariant.quantityInStock <= 0) {
        showToast('Sản phẩm đã hết hàng!', 'error');
        return;
    }
    const quantity = parseInt(document.getElementById('quantity-input').value) || 1;
    if (!isValidQuantity(quantity) || !isStockAvailable(quantity)) return;
    addToCart(currentVariant.variantId, quantity);
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
    toast.style.background = type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#17a2b8';
    toast.style.display = 'block';
    setTimeout(() => {
        toast.style.display = 'none';
    }, 1800);
}

async function addToCart(variantId, quantity) {
    const loggedIn = await isUserLoggedIn();
    console.log("login: ", loggedIn);

    if (loggedIn && loggedIn.isLoggedIn === true) {
        // Đã đăng nhập: gọi API backend
        const res = await fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                variantId: variantId,
                quantity: quantity
            })
        });
        console.log(res);
        
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


