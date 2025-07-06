// Product List Filter JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing filters...');
    initializeFilters();
    attachFilterEvents();
    attachPaginationEvents();
    attachSortingEvents();
    console.log('All filters initialized');
});

// Khởi tạo các bộ lọc
function initializeFilters() {
    // Chỉ cho phép chọn 1 danh mục
    initializeSingleSelectFilter('categoryId');
    
    // Chỉ cho phép chọn 1 size
    initializeSingleSelectFilter('sizeId');
    
    // Màu sắc có thể chọn nhiều (không cần xử lý đặc biệt)
    console.log('Filters initialized');
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
    console.log('Attaching filter events...');
    const filterForm = document.getElementById('filterForm');
    console.log('Filter form found:', filterForm);
    
    if (filterForm) {
        const filterInputs = filterForm.querySelectorAll('input[type="checkbox"], input[type="radio"], select');
        console.log('Filter inputs found:', filterInputs.length);
        
        filterInputs.forEach((input, index) => {
            console.log(`Input ${index}:`, input.name, input.type, input);
            // Tránh gắn sự kiện trùng lặp
            if (!input.hasAttribute('data-filter-attached')) {
                input.addEventListener('change', function() {
                    console.log('Filter changed:', this.name, this.value, this.checked);
                    submitFilterAjax();
                });
                input.setAttribute('data-filter-attached', 'true');
            }
        });
    } else {
        console.error('Filter form with ID "filterForm" not found!');
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
    console.log('submitFilterAjax called with page:', customPage);
    
    const filterForm = document.getElementById('filterForm');
    if (!filterForm) {
        console.error('Filter form not found');
        return;
    }

    // Tạo FormData từ form
    const formData = new FormData(filterForm);
    const params = new URLSearchParams();

    // Thêm sort value
    const sortSelect = document.getElementById('sortSelect');
    const sortValue = sortSelect ? sortSelect.value : 'default';
    params.append('sort', sortValue);

    // Thêm tất cả form data vào params
    console.log('Form data entries:');
    for (const [key, value] of formData.entries()) {
        console.log(`${key}: ${value}`);
        params.append(key, value);
    }

    // Thêm page nếu có
    if (customPage !== undefined && customPage !== null) {
        params.set('page', customPage);
    }

    const url = '/product/filter?' + params.toString();
    console.log('Sending request to:', url);

    // Hiển thị loading indicator (optional)
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
        console.log('Response status:', response.status);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    })
    .then(html => {
        console.log('Received HTML length:', html.length);
        console.log('HTML preview:', html.substring(0, 200) + '...');
        
        // Cập nhật nội dung product list
        const productListContainer = document.getElementById('product-list-container');
        if (productListContainer) {
            productListContainer.innerHTML = html;
            
            // Gắn lại sự kiện phân trang sau khi load mới
            attachPaginationEvents();
            
            // Không scroll, giữ nguyên vị trí hiện tại để UX tốt hơn
            console.log('Product list updated successfully');
        } else {
            console.error('Product list container not found!');
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

// Export functions for external use (nếu cần)
window.productListFilter = {
    submitFilter: submitFilterAjax,
    resetFilters: resetAllFilters,
    hasActiveFilters: hasActiveFilters
};