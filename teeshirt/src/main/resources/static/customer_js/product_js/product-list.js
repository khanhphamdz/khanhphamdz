// Product List Filter JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeFilters();
    attachFilterEvents();
    attachPaginationEvents();
    attachSortingEvents();
    checkWishlistStatus(); // Kiểm tra trạng thái wishlist khi trang load
});

// Khởi tạo các bộ lọc
function initializeFilters() {
    // Chỉ cho phép chọn 1 danh mục
    initializeSingleSelectFilter('categoryId');
    
    // Chỉ cho phép chọn 1 size
    initializeSingleSelectFilter('sizeId');
    
    // Màu sắc có thể chọn nhiều (không cần xử lý đặc biệt)
}

// Khởi tạo filter chỉ chọn 1 giá trị (cho category và size)
function initializeSingleSelectFilter(filterName) {
    const filterInputs = document.querySelectorAll(`input[name="${filterName}"]`);
    if (filterInputs.length > 0) {
        filterInputs.forEach(function(input) {
            input.addEventListener('change', function() {
                if (this.checked) {
                    // Bỏ chọn tất cả các input khác cùng name
                    filterInputs.forEach(function(otherInput) {
                        if (otherInput !== input) {
                            otherInput.checked = false;
                        }
                    });
                }
            });
        });
    }
}

// Gắn sự kiện cho các filter inputs
function attachFilterEvents() {
    const filterForm = document.getElementById('filterForm');
    
    if (filterForm) {
        const filterInputs = filterForm.querySelectorAll('input[type="checkbox"], input[type="radio"], select');
        
        filterInputs.forEach((input) => {
            // Tránh gắn sự kiện trùng lặp
            if (!input.hasAttribute('data-filter-attached')) {
                input.addEventListener('change', function() {
                    submitFilterAjax();
                });
                input.setAttribute('data-filter-attached', 'true');
            }
        });
    }
}

function attachPaginationEvents() {
    const paginationLinks = document.querySelectorAll('#product-list-container .pagination a.page-link');
    paginationLinks.forEach(link => {
        if (!link.hasAttribute('data-pagination-attached')) {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const href = this.getAttribute('href');
                const url = new URL(href, window.location.origin);
                const page = url.searchParams.get('page');
                submitFilterAjax(page);
            });
            link.setAttribute('data-pagination-attached', 'true');
        }
    });
}

// Gắn sự kiện cho sorting
function attachSortingEvents() {
    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            submitFilterAjax();
        });
    }
}

// Gửi AJAX request cho filter
function submitFilterAjax(customPage) {
    const filterForm = document.getElementById('filterForm');
    if (!filterForm) return;

    // Tạo FormData từ form
    const formData = new FormData(filterForm);
    const params = new URLSearchParams();

    // Thêm sort value
    const sortSelect = document.getElementById('sortSelect');
    const sortValue = sortSelect ? sortSelect.value : 'default';
    params.append('sort', sortValue);

    // Thêm tất cả form data vào params
    for (const [key, value] of formData.entries()) {
        params.append(key, value);
    }

    // Thêm page nếu có
    if (customPage !== undefined && customPage !== null) {
        params.set('page', customPage);
    }

    const url = '/product/filter?' + params.toString();

    // Hiển thị loading indicator
    showLoadingIndicator();

    // Gửi AJAX request
    fetch(url, {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Accept': 'text/html'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    })
    .then(html => {
        // Cập nhật nội dung product list
        const productListContainer = document.getElementById('product-list-container');
        if (productListContainer) {
            productListContainer.innerHTML = html;
            attachPaginationEvents();
            attachSortingEvents();
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Có lỗi xảy ra khi tải dữ liệu. Vui lòng thử lại.');
    })
    .finally(() => {
        hideLoadingIndicator();
    });
}

// Hiển thị loading indicator
function showLoadingIndicator() {
    const productListContainer = document.getElementById('product-list-container');
    if (productListContainer) {
        // Chỉ làm mờ phần danh sách sản phẩm, không ảnh hưởng filter
        productListContainer.style.opacity = '0.5';
        productListContainer.style.pointerEvents = 'none';
        
        // Thêm loading overlay chỉ cho product list
        let loadingOverlay = productListContainer.querySelector('.loading-overlay');
        if (!loadingOverlay) {
            loadingOverlay = document.createElement('div');
            loadingOverlay.className = 'loading-overlay';
            loadingOverlay.innerHTML = `
                <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                    <div class="spinner-border text-primary" role="status">
                        <span class="sr-only">Đang tải...</span>
                    </div>
                </div>
            `;
            loadingOverlay.style.cssText = `
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(255, 255, 255, 0.8);
                z-index: 10;
                display: flex;
                align-items: center;
                justify-content: center;
            `;
            productListContainer.style.position = 'relative';
            productListContainer.appendChild(loadingOverlay);
        }
        loadingOverlay.style.display = 'flex';
    }
}

// Ẩn loading indicator
function hideLoadingIndicator() {
    const productListContainer = document.getElementById('product-list-container');
    if (productListContainer) {
        productListContainer.style.opacity = '1';
        productListContainer.style.pointerEvents = 'auto';
        
        // Ẩn loading overlay
        const loadingOverlay = productListContainer.querySelector('.loading-overlay');
        if (loadingOverlay) {
            loadingOverlay.style.display = 'none';
        }
    }
}

// Reset tất cả filters
function resetAllFilters() {
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        // Reset form
        filterForm.reset();
        
        // Reset sort select
        const sortSelect = document.getElementById('sortSelect');
        if (sortSelect) {
            sortSelect.value = 'default';
        }
        
        // Gửi AJAX để load lại danh sách
        submitFilterAjax();
    }
}

// Kiểm tra xem có filter nào được chọn không
function hasActiveFilters() {
    const filterForm = document.getElementById('filterForm');
    if (!filterForm) return false;
    
    const checkedInputs = filterForm.querySelectorAll('input:checked');
    const selectedSelects = filterForm.querySelectorAll('select option:checked:not([value=""])');
    
    return checkedInputs.length > 0 || selectedSelects.length > 0;
}

// Toggle wishlist cho sản phẩm trong danh sách
async function toggleWishlistProduct(element, event) {
    event.preventDefault();
    const productId = element.dataset.productId;
    if (!productId) return;
    try {
        const isInWishlist = element.classList.contains('active');
        const url = isInWishlist ? `/wishlist/remove/${productId}` : `/wishlist/add/${productId}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const result = await response.text();
        if (result.startsWith('success:')) {
            if (isInWishlist) {
                element.classList.remove('active');
            } else {
                element.classList.add('active');
            }
            if (typeof updateWishlistCount === 'function') updateWishlistCount();
        }
    } catch (error) {}
}

// Kiểm tra trạng thái wishlist cho tất cả sản phẩm
async function checkWishlistStatus() {
    try {
        const wishlistButtons = document.querySelectorAll('.btn-addwish-b2');
        for (let button of wishlistButtons) {
            const productId = button.dataset.productId;
            if (productId) {
                const response = await fetch(`/wishlist/check/${productId}`);
                if (response.ok) {
                    const isInWishlist = await response.text() === 'true';
                    if (isInWishlist) {
                        button.classList.add('active');
                    } else {
                        button.classList.remove('active');
                    }
                }
            }
        }
    } catch (error) {}
}

// Hàm hiển thị toast thông báo
function showToast(message, type = 'success') {
    let toast = document.getElementById('custom-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'custom-toast';
        toast.style.position = 'fixed';
        toast.style.bottom = '20px';
        toast.style.left = '20px';
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

// Export functions for external use (nếu cần)
window.productListFilter = {
    submitFilter: submitFilterAjax,
    resetFilters: resetAllFilters,
    hasActiveFilters: hasActiveFilters
};

window.toggleWishlistProduct = toggleWishlistProduct;
window.checkWishlistStatus = checkWishlistStatus;