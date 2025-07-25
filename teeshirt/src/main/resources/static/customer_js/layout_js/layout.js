
const initApp = () => {
    window.addEventListener('scroll', function () {
        const btnBackToTop = document.getElementById('back-to-top');
        if (window.scrollY > 500) {
            btnBackToTop.style.display = 'block';
        } else {
            btnBackToTop.style.display = 'none';
        }
    });
    document.getElementById('back-to-top').addEventListener('click', function () {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
    window.addEventListener('scroll', function () {
        const header = document.getElementById('main-header');
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
            header.classList.remove('transparent-header');
        } else {
            header.classList.remove('scrolled');
            header.classList.add('transparent-header');
        }
    });

    const button = document.getElementById("btn-search-product");
    button.addEventListener("click", getData);

    const inputSearch = document.getElementById("filter");
    inputSearch.addEventListener("input", debouce(getData, 1500));

    const btnShowCartItem = document.getElementById('btn-show-cart-item');
    btnShowCartItem.addEventListener('click', getCartItemFormUser);
}
async function getData() {
    const products = document.querySelector('.product-search-list')
    const filter = document.getElementById('filter')
    const listItems = []
    let keyword = filter.value;
    console.log(keyword);

    const res = await fetch(`http://localhost:8080/api/product/search?keyword=${encodeURIComponent(keyword)}`);
    console.log(res);

    const results = await res.json();
    console.log(results);
    // Clear products
    products.innerHTML = '';
    const listProduct = results.data || [];

    if (listProduct.length > 0) {
        const div = document.createElement('div');
        div.setAttribute('class', 'product-search-item');
        listItems.push(div);
        listProduct.forEach((product) => {
            const div = document.createElement('div');
            div.setAttribute('class', 'product-search-item');
            div.innerHTML = `
                    <a href="/product/detail/${product.productId}">
                        <img src="${product.images && product.images.length > 0 ? product.images[0].imageUrl : 'https://res.cloudinary.com/...'}" alt="">
                    </a>
                    <div class="product-search-item-detail">
                        <a href="/product/detail/${product.productId}" class="text-decoration-none">
                            <h4>${product.name ? product.name.slice(0, 30) : 'Không có tên'}</h4>
                        </a>
                        <span>${product.description ? product.description.slice(0, 30) : 'Không có mô tả'}</span>
                    </div>
                `;
            products.appendChild(div);
        });
    } else {
        products.innerHTML = `
            <h6 class="text-danger">Không tìm thấy sản phẩm liên quan</h6>
            `;
    }
}
document.addEventListener("DOMContentLoaded", initApp)
const debouce = (fn, delay) => {
    delay = delay || 0;
    let timerId;
    console.log("TimerId imadiate load:", timerId)
    return () => {
        console.log(`TimerId previous at: ${timerId}`);
        if (timerId) {
            clearTimeout(timerId)
            timerId = null;
        }
        timerId = setTimeout(() => {
            fn();
        }, delay)
    }

}
async function getCartItemFormUser() {
    const res = await fetch('/api/cart', {method : 'GET'})
    const data = await res.json()

    if (data.status === 'ok') {
        console.log( 'data',data.data);
        renderCartOffcanvas(data.data);
    }
}
// Render danh sách sản phẩm trong offcanvas
function renderCartOffcanvas(cart) {
    const body = document.getElementById('cart-offcanvas-body');
    if (!body) return;
    if (!cart.length) {
        body.innerHTML = '<div class="text-center text-muted">Giỏ hàng trống</div>';
        return;
    }
    console.log('cart:', cart);
    
    let html = '<ul class="list-group mb-3">';
    cart.forEach(item => {
        html += `
        <li class="list-group-item d-flex align-items-center">
            <img src="${item.img || '/images/product-03.jpg'}" style="width:50px;height:50px;object-fit:cover;" class="me-2 rounded">
            <div class="flex-grow-1">
                <div><strong>${item.name || ''}</strong></div>
                <div class="small text-muted">Màu: ${item.color || ''} | Size: ${item.size || ''}</div>
                <div>SL: ${item.quantity} x <span class="text-danger">${item.price}₫</span></div>
            </div>
            <button class="btn btn-sm btn-danger ms-2" onclick='removeCartItem(${item.variantId})'>&times;</button>
        </li>
        `;
    });
    html += '</ul>';
    // Tổng tiền
    const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    html += `<div class="fw-bold text-end">Tổng: <span class="text-danger">${total.toLocaleString()}₫</span></div>`;
    body.innerHTML = html;
}
// Xóa sản phẩm khỏi giỏ hàng
 async function removeCartItem(variantId) {
    const res = await fetch(`/api/cart/remove/${variantId}`, {method: 'DELETE'})
    const data = await res.json()
    console.log(data);
    
    if (data.status === 'ok') {
        console.log('xóa thành công biến thể ', variantId);
        getCartItemFormUser();
    }
}
// Lấy tổng số lượng sản phẩm trong giỏ hàng
function getCartCount() {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    return cart.reduce((sum, item) => sum + item.quantity, 0);
}

// Cập nhật badge số lượng trên icon giỏ hàng
function updateCartBadge() {
    const count = getCartCount();
    const badge = document.getElementById('cart-badge');
    if (badge) {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline-block' : 'none';
    }
}

// Layout JavaScript - Xử lý header và các thành phần chung
document.addEventListener('DOMContentLoaded', function() {
    updateWishlistCount();
    updateCartBadge();
});

// Cập nhật số lượng wishlist trên header
async function updateWishlistCount() {
    try {
        const response = await fetch('/wishlist/count');
        if (response.ok) {
            const count = await response.text();
            const wishlistBadge = document.getElementById('wishlist-badge');
            if (wishlistBadge) {
                wishlistBadge.textContent = count;
            }
        }
    } catch (error) {
        console.log('Error updating wishlist count:', error);
    }
}

// Cập nhật số lượng cart trên header
async function updateCartBadge() {
    try {
        const cartBadge = document.getElementById('cart-badge');
        if (cartBadge) {
            const cart = JSON.parse(localStorage.getItem('cart') || '[]');
            const totalItems = cart.reduce((sum, item) => sum + (item.quantity || 0), 0);
            cartBadge.textContent = totalItems;
        }
    } catch (error) {
        console.log('Error updating cart badge:', error);
    }
}

// Export functions để sử dụng từ các file khác
window.updateWishlistCount = updateWishlistCount;
window.updateCartBadge = updateCartBadge;