document.addEventListener('DOMContentLoaded', function() {
    console.log('Wishlist page loaded');
    
    // Xử lý xóa khỏi wishlist (nút trái tim)
    const removeButtons = document.querySelectorAll('.remove-from-wishlist-btn');
    console.log('Found remove-from-wishlist buttons:', removeButtons.length);
    removeButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.getAttribute('data-product-id');
            console.log('Remove from wishlist clicked for product:', productId);
            removeFromWishlist(productId);
        });
    });

});





// Xóa khỏi wishlist
function removeFromWishlist(productId) {
    fetch(`/wishlist/remove/${productId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        }
        throw new Error('Network response was not ok');
    })
    .then(result => {
        if (result.startsWith('success:')) {
            // Xóa sản phẩm khỏi DOM
            const productElement = document.querySelector(`[data-product-id="${productId}"]`).closest('.product');
            productElement.remove();
            
            // Cập nhật số lượng wishlist trên header
            if (typeof updateWishlistCount === 'function') {
                updateWishlistCount();
            }
            
            // Kiểm tra nếu không còn sản phẩm nào
            const remainingProducts = document.querySelectorAll('.product');
            if (remainingProducts.length === 0) {
                location.reload(); // Reload để hiển thị trang trống
            }
        } else {
            showToast(result.replace('error:', ''), 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra khi xóa khỏi danh sách yêu thích!', 'error');
    });
}

// Hiển thị toast notification
function showToast(message, type = 'info') {
    // Tạo toast element
    const toastHtml = `
        <div class="toast align-items-center text-white bg-${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'info'} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    
    // Tạo toast container nếu chưa có
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'toast-container position-fixed bottom-0 start-0 p-3';
        toastContainer.style.zIndex = '9999';
        document.body.appendChild(toastContainer);
    }
    
    // Thêm toast vào container
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    // Hiển thị toast
    const toastElement = toastContainer.lastElementChild;
    const toast = new bootstrap.Toast(toastElement);
    toast.show();
    
    // Tự động xóa toast sau khi ẩn
    toastElement.addEventListener('hidden.bs.toast', function() {
        toastElement.remove();
    });
} 