// Lấy tổng số lượng sản phẩm trong giỏ hàng
const fetchCartItems = async () => {
    const res = await fetch('/api/cart', { method: 'GET' });

    if (res.status === 401) {
        console.warn("Người dùng chưa đăng nhập");
        showCartNotLoggedIn();
        return [];
    }

    const data = await res.json();
    return data.status === 'ok' ? data.data : []
}
async function updateCartBadge() {
    const cartItems = await fetchCartItems();
    localStorage.setItem("cart", JSON.stringify(cartItems));
    const cartItemQuantity = cartItems.length;
    const badge = document.getElementById('cart-badge');
    if (badge) {
        badge.textContent = cartItemQuantity;
        badge.style.display = cartItemQuantity > 0 ? 'inline-block' : 'none';
    }
}
function showCartNotLoggedIn() {
    const body = document.getElementById('cart-offcanvas-body');
    if (body) {
        body.innerHTML = `
            <div class="text-center text-muted">
                Bạn chưa đăng nhập. <br>
                <a href="/account/login" class="btn btn-sm btn-primary mt-2">Đăng nhập</a>
            </div>
        `;
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    updateCartBadge();
    renderCartOffcanvas();
});

// Render danh sách sản phẩm trong offcanvas
// Render giỏ hàng trong offcanvas
async function renderCartOffcanvas() {
    const body = document.getElementById('cart-offcanvas-body');
    if (!body) return;
    const loggedIn = await isUserLoggedIn();
    if (!loggedIn) {
        body.innerHTML = '<div class="text-center text-muted">Vui lòng đăng nhập để xem giỏ hàng</div>';
        return;
    }

    const cart = await fetchCartItems();
    if (!cart.length) {
        body.innerHTML = '<div class="text-center text-muted">Giỏ hàng trống</div>';
        return;
    }

    let html = '<ul class="list-group mb-3">';
    cart.forEach(item => {
        html += `
        <li class="list-group-item d-flex align-items-center">
            <img src="${item.img || '/images/product-03.jpg'}" style="width:50px;height:50px;object-fit:cover;" class="me-2 rounded">
            <div class="flex-grow-1">
                <div><strong>${item.productName || ''}</strong></div>
                <div class="small text-muted">Màu: ${item.color || item.colorId} | Size: ${item.size || item.sizeId}</div>
                <div>SL: ${item.quantity} x <span class="text-danger">${item.price}₫</span></div>
            </div>
            <button class="btn btn-sm btn-danger ms-2" type="button" onclick='removeCartItem(${item.variantId})'>&times;</button>
        </li>
        `;
    });
    html += '</ul>';

    const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    html += `<div class="fw-bold text-end">Tổng: <span class="text-danger">${total.toLocaleString()}₫</span></div>`;
    html += `
      <div class="text-end mt-3">
        <a href="/shopping-cart" class="btn btn-primary">Thanh toán</a>
      </div>
    `;
    body.innerHTML = html;
}

// Xóa sản phẩm khỏi giỏ hàng
async function removeCartItem(variantId) {
    console.log('delete cart item with variantId: ', variantId);
    
    const res = await fetch(`/api/cart/remove/${variantId}`, { method: 'DELETE' });
    const data = await res.json();
    console.log('data delete cart item: ', data);
    
    if (data.status === 'ok') {
        await renderCartOffcanvas();
        await updateCartBadge();
    }
}

async function isUserLoggedIn() {
    try {
        const res = await fetch('/api/auth/me', { method: 'GET' })
        if (!res.ok) {
            return false;
        }
        const data = await res.json()
        return data;
    } catch (error) {
        console.log('error checking login: ', error);
        return false;
    }
}
// Tự động render lại khi mở offcanvas
const offcanvasCart = document.getElementById('offcanvasCart');
if (offcanvasCart) {
    offcanvasCart.addEventListener('show.bs.offcanvas', renderCartOffcanvas);
} 