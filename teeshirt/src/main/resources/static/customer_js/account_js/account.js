document.addEventListener('DOMContentLoaded', function () {
    // ========== LOAD ORDER HISTORY TABS ==========
    const orderHistoryTabs = document.getElementById('orderHistoryTabs');
    if (orderHistoryTabs) {
        // Mặc định load tab "Tất cả" khi vào trang lịch sử đơn hàng
        loadOrdersByStatus('all');

        // Bắt sự kiện khi chọn tab
        orderHistoryTabs.addEventListener('click', function (e) {
            if (e.target.classList.contains('nav-link')) {
                const status = e.target.getAttribute('data-status');
                if (status) {
                    loadOrdersByStatus(status);
                }
            }
        });
    }

    // ========== KHÔI PHỤC TAB SAU KHI RELOAD ==========
    const activeTab = localStorage.getItem('activeTab');
    if (activeTab === 'address-book') {
        const addressBookTab = document.getElementById('v-pills-address-tab');
        if (addressBookTab) {
            const tabInstance = new bootstrap.Tab(addressBookTab);
            tabInstance.show();
        }
        localStorage.removeItem('activeTab');
    }

    // ========== ĐỊA CHỈ MẶC ĐỊNH ==========
    function updateDefaultDisplay() {
        // Reset tất cả badge và radio
        document.querySelectorAll('.default-badge').forEach(badge => badge.style.display = 'none');
        document.querySelectorAll('.default-address-radio').forEach(radio => {
            // Nếu radio được checked (do Thymeleaf render isDefault), hiển thị badge
            if (radio.checked) {
                const badge = radio.closest('.address-item')?.querySelector('.default-badge');
                if (badge) badge.style.display = 'inline-block';
            }
        });
    }

    document.querySelectorAll('.default-address-radio').forEach(radio => {
        radio.addEventListener('change', function () {
            if (this.checked) {
                const addressId = this.getAttribute('data-address-id');
                fetch(`/account/address/set-default/${addressId}`, { method: 'PUT' })
                    .then(res => res.text())
                    .then(() => {
                        // Cập nhật giao diện: chỉ radio này checked, các radio khác bỏ checked, badge "Mặc định" đúng
                        document.querySelectorAll('.default-address-radio').forEach(r => r.checked = false);
                        this.checked = true;
                        document.querySelectorAll('.default-badge').forEach(badge => badge.style.display = 'none');
                        const badge = this.closest('.address-item')?.querySelector('.default-badge');
                        if (badge) badge.style.display = 'inline-block';
                    });
            }
        });
    });

    updateDefaultDisplay();

    // ========== API TỈNH/PHƯỜNG/XÃ ==========
    function createOption(value, text) {
        const option = document.createElement('option');
        option.value = value;
        option.textContent = text;
        return option;
    }

    function loadProvinces(selectElement) {
        fetch('https://provinces.open-api.vn/api/p/')
            .then(res => res.json())
            .then(data => {
                selectElement.innerHTML = '<option value="">Chọn tỉnh/thành phố</option>';
                data.forEach(province => {
                    selectElement.appendChild(createOption(province.code, province.name));
                });
            })
            .catch(error => {
                console.error('Error loading provinces:', error);
                selectElement.innerHTML = '<option value="">Không thể tải danh sách tỉnh/thành phố</option>';
            });
    }

    function loadDistricts(provinceCode, selectElement) {
        selectElement.innerHTML = '<option value="">Chọn quận/huyện</option>';
        if (!provinceCode) return;

        fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`)
            .then(res => res.json())
            .then(data => {
                if (data.districts && Array.isArray(data.districts)) {
                    data.districts.forEach(district => {
                        selectElement.appendChild(createOption(district.code, district.name));
                    });
                }
            })
            .catch(error => {
                console.error('Error loading districts:', error);
                selectElement.innerHTML = '<option value="">Không thể tải danh sách quận/huyện</option>';
            });
    }

    function loadWards(districtCode, selectElement) {
        selectElement.innerHTML = '<option value="">Chọn phường/xã</option>';
        if (!districtCode) return;

        fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`)
            .then(res => res.json())
            .then(data => {
                if (data.wards && Array.isArray(data.wards)) {
                    data.wards.forEach(ward => {
                        selectElement.appendChild(createOption(ward.code, ward.name));
                    });
                }
            })
            .catch(error => {
                console.error('Error loading wards:', error);
                selectElement.innerHTML = '<option value="">Không thể tải danh sách phường/xã</option>';
            });
    }

    // ========== MODAL SETUP ==========
    const addAddressModal = document.getElementById('addAddressModal');
    const editAddressModal = document.getElementById('editAddressModal');

    if (addAddressModal) {
        addAddressModal.addEventListener('show.bs.modal', function () {
            loadProvinces(document.getElementById('addProvinceSelect'));
        });
    }

    if (editAddressModal) {
        editAddressModal.addEventListener('show.bs.modal', function () {
            loadProvinces(document.getElementById('editProvinceSelect'));
        });
    }

    // Province/District/Ward change handlers
    ['add', 'edit'].forEach(prefix => {
        const provinceSelect = document.getElementById(`${prefix}ProvinceSelect`);
        const districtSelect = document.getElementById(`${prefix}DistrictSelect`);
        const wardSelect = document.getElementById(`${prefix}WardSelect`);

        if (provinceSelect) {
            provinceSelect.addEventListener('change', function () {
                loadDistricts(this.value, districtSelect);
                wardSelect.innerHTML = '<option value="">Chọn phường/xã</option>';
            });
        }

        if (districtSelect) {
            districtSelect.addEventListener('change', function () {
                loadWards(this.value, wardSelect);
            });
        }
    });

    // ========== FORM SUBMISSIONS ==========
    function handleFormSubmission(url, method, formData, successMessage) {
        return fetch(url, {
            method: method,
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message || successMessage);
                    localStorage.setItem('activeTab', 'address-book');
                    location.reload();
                } else {
                    alert('Lỗi: ' + (data.error || 'Có lỗi xảy ra'));
                }
            });
    }

    const addAddressForm = document.getElementById('addAddressForm');
    if (addAddressForm) {
        addAddressForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const formData = new FormData(this);

            handleFormSubmission('/account/address/add', 'POST', formData, 'Thêm địa chỉ thành công!')
                .catch(error => {
                    console.error('Error:', error);
                    alert('Có lỗi xảy ra khi thêm địa chỉ. Vui lòng thử lại.');
                });
        });
    }

    const editAddressForm = document.getElementById('editAddressForm');
    if (editAddressForm) {
        editAddressForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const formData = new FormData(this);
            const addressId = document.getElementById('editAddressId').value;
            fetch(`/account/address/${addressId}`, {
                method: 'POST',
                body: formData
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert(data.message || 'Cập nhật địa chỉ thành công!');
                        localStorage.setItem('activeTab', 'address-book');
                        location.reload();
                    } else {
                        alert('Lỗi: ' + (data.error || data.message || 'Có lỗi xảy ra khi cập nhật địa chỉ.'));
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Có lỗi xảy ra khi cập nhật địa chỉ. Vui lòng thử lại.');
                });
        });
    }

    // ========== EVENT DELEGATION ==========
    document.addEventListener('click', function (e) {
        // Xử lý nút sửa địa chỉ
        if (e.target.classList.contains('edit-address-btn')) {
            const button = e.target;
            const addressId = button.getAttribute('data-id');
            const name = button.getAttribute('data-name') || '';
            const phone = button.getAttribute('data-phone') || '';
            const provinceId = button.getAttribute('data-province') || '';
            const districtId = button.getAttribute('data-district') || '';
            const wardId = button.getAttribute('data-ward') || '';
            const specificAddress = button.getAttribute('data-specific') || '';

            document.getElementById('editAddressId').value = addressId;
            document.getElementById('editInputName').value = name;
            document.getElementById('editInputPhone').value = phone;
            document.getElementById('editInputSpecificAddress').value = specificAddress;

            // Set giá trị cho các select sau khi load
            setTimeout(() => {
                if (provinceId) {
                    document.getElementById('editProvinceSelect').value = provinceId;
                    loadDistricts(provinceId, document.getElementById('editDistrictSelect'));

                    setTimeout(() => {
                        if (districtId) {
                            document.getElementById('editDistrictSelect').value = districtId;
                            loadWards(districtId, document.getElementById('editWardSelect'));

                            setTimeout(() => {
                                if (wardId) {
                                    document.getElementById('editWardSelect').value = wardId;
                                }
                            }, 500);
                        }
                    }, 500);
                }
            }, 500);
        }

        // Xử lý nút xóa địa chỉ
        if (e.target.classList.contains('delete-address-btn')) {
            const button = e.target;
            const addressId = button.getAttribute('data-id');

            if (confirm('Bạn có chắc chắn muốn xóa địa chỉ này?')) {
                fetch(`/account/address/${addressId}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            alert(data.message || 'Xóa địa chỉ thành công!');
                            localStorage.setItem('activeTab', 'address-book');
                            location.reload();
                        } else {
                            alert('Lỗi: ' + (data.error || 'Không thể xóa địa chỉ'));
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('Có lỗi xảy ra khi xóa địa chỉ. Vui lòng thử lại.');
                    });
            }
        }
    });

    // ========== AVATAR UPLOAD ==========
    const avatarImage = document.getElementById('avatarImage');
    const avatarInput = document.getElementById('avatarInput');

    if (avatarImage && avatarInput) {
        avatarImage.addEventListener('click', function () {
            avatarInput.click();
        });

        avatarInput.addEventListener('change', function (e) {
            const file = e.target.files[0];
            if (file) {
                const formData = new FormData();
                formData.append('avatar', file);

                fetch('/account/upload-avatar', {
                    method: 'POST',
                    body: formData
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            avatarImage.src = data.avatarUrl;
                            alert('Cập nhật ảnh đại diện thành công!');
                        } else {
                            alert('Lỗi: ' + data.error);
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('Có lỗi xảy ra khi upload ảnh đại diện. Vui lòng thử lại.');
                    });
            }
        });
    }

    // ========== ORDER HISTORY TABS ==========
    let returnRequestOrderIds = [];
    function fetchReturnRequestsAndUpdate(callback) {
        fetch('/account/return-requests')
            .then(response => response.json())
            .then(data => {
                if (data.status === 'ok' && data.data) {
                    returnRequestOrderIds = data.data.map(rr => rr.orderId).filter(Boolean);
                } else {
                    returnRequestOrderIds = [];
                }
                if (typeof callback === 'function') callback();
            })
            .catch(() => {
                returnRequestOrderIds = [];
                if (typeof callback === 'function') callback();
            });
    }

    function loadOrdersByStatus(status) {
        const orderListId = status + 'Orders';
        const orderList = document.getElementById(orderListId);
        if (!orderList) return;
        const apiEndpoint = status === 'all' ? '/account/orders' : `/account/orders/${status}`;
        orderList.innerHTML = `
            <div class="text-center py-5">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
                <p class="mt-3">Đang tải đơn hàng...</p>
            </div>
        `;
        fetchReturnRequestsAndUpdate(() => {
            fetch(apiEndpoint)
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'ok' && data.data && data.data.length > 0) {
                        displayOrders(data.data, orderList, status);
                    } else {
                        displayEmptyOrders(orderList, status);
                    }
                })
                .catch(error => {
                    console.error('Error loading orders:', error);
                    orderList.innerHTML = `
                        <div class="text-center py-5 text-muted">
                            <i class="fa-solid fa-exclamation-triangle fs-1 mb-3"></i>
                            <p>Có lỗi xảy ra khi tải đơn hàng</p>
                        </div>
                    `;
                });
        });
    }

    function displayOrders(orders, container, status) {
        let filteredOrders = orders;
        // Nếu là tab 'cancelled', lấy cả các status 'cancelled', 'đã huỷ', 'da huy', 'da hủy'... (không phân biệt hoa thường, có dấu)
        if (status === 'cancelled') {
            filteredOrders = orders.filter(order => {
                const s = (order.status || '').toLowerCase();
                return s === 'cancelled' || s === 'đã huỷ' || s === 'da huy' || s === 'da hủy';
            });
        }
        // Các tab khác giữ nguyên
        const orderCards = filteredOrders.map(order => createOrderCard(order, status)).join('');
        container.innerHTML = `<div class="row gy-4">${orderCards}</div>`;
    }

    function displayEmptyOrders(container, status) {
        const messages = {
            'all': { icon: 'list', text: 'Chưa có đơn hàng nào' },
            'pending': { icon: 'clock', text: 'Chưa có đơn hàng nào đang chờ xác nhận' },
            'processing': { icon: 'gear', text: 'Chưa có đơn hàng nào đang xử lý' },
            'shipped': { icon: 'truck', text: 'Chưa có đơn hàng nào đang giao' },
            'delivered': { icon: 'check-circle', text: 'Chưa có đơn hàng nào đã giao' },
            'cancelled': { icon: 'times-circle', text: 'Chưa có đơn hàng nào bị hủy' }
        };

        const message = messages[status] || { icon: 'box', text: 'Không có đơn hàng' };

        container.innerHTML = `
            <div class="text-center py-5 text-muted">
                <i class="fa-solid fa-${message.icon} fs-1 mb-3"></i>
                <p>${message.text}</p>
            </div>
        `;
    }

    function createOrderCard(order, status) {
        const statusColors = {
            'pending': 'warning',
            'processing': 'primary',
            'shipped': 'info',
            'delivered': 'success',
            'cancelled': 'danger',
            'đã huỷ': 'danger',
            'da huy': 'danger',
            'da hủy': 'danger',
            'returned': 'secondary'
        };
        const statusLabels = {
            'pending': 'Chờ xác nhận',
            'processing': 'Đang xử lý',
            'shipped': 'Đang giao',
            'delivered': 'Đã giao',
            'cancelled': 'Đã huỷ',
            'đã huỷ': 'Đã huỷ',
            'da huy': 'Đã huỷ',
            'da hủy': 'Đã huỷ',
            'returned': 'Hoàn trả'
        };
        // Gộp các status huỷ về 1 label
        let statusKey = (order.status || '').toLowerCase();
        if (['cancelled', 'đã huỷ', 'da huy', 'da hủy'].includes(statusKey)) statusKey = 'cancelled';
        const color = statusColors[statusKey] || 'secondary';
        const label = statusLabels[statusKey] || order.status;
        let returnBtnHtml = '';
        if (order.status === 'delivered' && returnRequestOrderIds.includes(order.orderId)) {
            returnBtnHtml = `<button class="btn btn-secondary rounded-pill px-4 fw-bold me-2" disabled>Đã yêu cầu hoàn trả</button>`;
        } else if (order.status === 'delivered') {
            returnBtnHtml = `<button class="btn btn-danger rounded-pill px-4 fw-bold" onclick="openReturnModal(${order.orderId})">Hoàn trả hàng</button>`;
        }
        return `
            <div class="col-12">
                <div class="card rounded-4 border-0 shadow-sm mb-4">
                    <div class="card-header bg-${color} text-white rounded-top-4 d-flex justify-content-between align-items-center">
                        <div>
                            <a href="#" class="fw-bold text-white text-decoration-underline"
                                onclick="loadOrderDetail(${order.orderId})">#${order.orderId}</a>
                            <div class="small text-white-50">${formatDate(order.createdAt)}</div>
                        </div>
                        <span class="badge bg-white text-${color} fw-semibold px-4 py-2 rounded-pill">${label}</span>
                    </div>
                    <div class="card-body bg-warning-subtle">
                        ${createOrderItems(order.items || [])}
                    </div>
                    <div class="card-footer bg-light d-flex align-items-center rounded-bottom-4">
                        <div class="d-flex gap-2">
                            <button class="btn btn-outline-dark rounded-pill px-4 fw-bold">Cần hỗ trợ</button>
                            <button class="btn btn-dark rounded-pill px-4 fw-bold">Mua lại</button>
                            ${(order.status === 'pending' || order.status === 'processing') ? `<button class="btn btn-danger rounded-pill px-4 fw-bold" onclick="cancelOrder(${order.orderId})">Hủy đơn hàng</button>` : ''}
                            ${returnBtnHtml}
                        </div>
                        <div class="ms-auto fw-bold fs-5">${formatCurrency(order.finalAmount)}đ</div>
                    </div>
                </div>
            </div>
        `;
    }

    function createOrderItems(items) {
        if (!items || items.length === 0) {
            return '<div class="text-muted">Không có sản phẩm</div>';
        }

        return items.map(item => {
            const variant = item.variant || {};
            const images = variant.images || [];
            const imageUrl = images.length > 0 ? images[0].imageUrl : '/images/no-image.png';
            const productName = variant.name || 'Sản phẩm';
            const colorName = variant.colorName || '';
            const sizeName = variant.sizeName || '';
            const attributes = [colorName, sizeName].filter(attr => attr).join(' / ');

            return `
                <div class="d-flex align-items-center gap-4 mb-3">
                    <img src="${imageUrl}" 
                         alt="product" class="rounded-3 border" width="110" height="110">
                    <div class="flex-grow-1">
                        <div class="fw-semibold text-primary mb-1">${productName}</div>
                        <div class="text-secondary small">${attributes || 'Không có thuộc tính'}</div>
                        <div class="text-secondary small">x${item.quantity}</div>
                        <div class="fw-bold mt-1">${formatCurrency(item.priceAtPurchase)}đ</div>
                    </div>
                </div>
            `;
        }).join('');
    }

    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    }

    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount);
    }

    // Định nghĩa loadOrderDetail ở global scope
    window.loadOrderDetail = function (orderId) {
        fetch(`/account/orders/${orderId}/detail`)
            .then(response => response.json())
            .then(data => {
                if (data.status === 'ok' && data.data) {
                    populateOrderDetailModal(data.data);
                    // Mở modal
                    const modal = new bootstrap.Modal(document.getElementById('orderDetailModal'));
                    modal.show();
                } else {
                    alert('Không thể tải chi tiết đơn hàng: ' + (data.message || 'Lỗi không xác định'));
                }
            })
            .catch(error => {
                alert('Có lỗi xảy ra khi tải chi tiết đơn hàng');
            });
    };

    function populateOrderDetailModal(order) {
        // Cập nhật tiêu đề modal
        document.getElementById('orderDetailModalLabel').textContent = `Chi tiết đơn hàng #${order.orderId}`;

        // Lấy thông tin cơ bản
        const customerName = order.customerName || 'Không có thông tin';
        const customerEmail = order.customerEmail || 'Không có thông tin';
        const customerPhone = order.customerPhone || 'Không có thông tin';
        const customerAddress = order.customerDefaultAddress || 'Không có thông tin';

        // Cập nhật modal body
        const modalBody = document.getElementById('orderDetailModalBody');
        modalBody.innerHTML = `
            <div class="row mb-4">
                <div class="col-md-6">
                    <div class="mb-2"><span class="fw-semibold">Ngày đặt hàng:</span></div>
                    <div class="mb-3">${formatDate(order.createdAt)}</div>
                    <div class="mb-2"><span class="fw-semibold">Tên người nhận:</span></div>
                    <div class="mb-3">${customerName}</div>
                    <div class="mb-2"><span class="fw-semibold">Số điện thoại:</span></div>
                    <div class="mb-3">${customerPhone}</div>
                    <div class="mb-2"><span class="fw-semibold">Địa chỉ giao hàng:</span></div>
                    <div>${customerAddress}</div>
                </div>
                <div class="col-md-6">
                    <div class="mb-2"><span class="fw-semibold">Phương thức thanh toán:</span></div>
                    <div class="mb-3">COD</div>
                    <div class="mb-2"><span class="fw-semibold">Email:</span></div>
                    <div class="mb-3">${customerEmail}</div>
                    <div class="mb-2"><span class="fw-semibold">Ghi chú:</span></div>
                    <div>${order.note || '-'}</div>
                </div>
            </div>
            <div class="mb-4">
                <h5 class="fw-bold mb-3">Tình trạng đơn hàng</h5>
                <div class="d-flex flex-wrap gap-2 align-items-center">
                    <span class="badge bg-primary fw-semibold px-4 py-2">${getStatusLabel(order.status)}</span>
                    <span class="text-secondary">${formatDateTime(order.createdAt)}</span>
                </div>
            </div>
            <div class="table-responsive">
                <table class="table align-middle bg-white rounded-4 overflow-hidden">
                    <thead class="table-light">
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Thuộc tính</th>
                            <th>Số lượng</th>
                            <th class="text-end">Thành tiền</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${createOrderDetailItems(order.items || [])}
                    </tbody>
                </table>
            </div>
        `;

        // Cập nhật modal footer
        const modalFooter = document.getElementById('orderDetailModalFooter');
        modalFooter.innerHTML = `
            <div>
                <button class="btn btn-outline-dark rounded-pill px-4 fw-bold">Cần hỗ trợ</button>
                ${(order.status === 'pending' || order.status === 'processing') ? `<button class="btn btn-danger rounded-pill px-4 fw-bold" onclick="cancelOrder(${order.orderId})">Hủy đơn hàng</button>` : ''}
                ${order.status === 'delivered' && returnRequestOrderIds.includes(order.orderId) ? `<button class="btn btn-warning rounded-pill px-4 fw-bold" disabled>Đã yêu cầu hoàn trả</button>` : ''}
                <button class="btn btn-dark rounded-pill px-4 fw-bold">Mua lại</button>
            </div>
            <div class="fw-bold fs-5">Tổng tiền: ${formatCurrency(order.finalAmount)}đ</div>
        `;
    }

    function createOrderDetailItems(items) {
        return items.map(item => {
            const variant = item.variant || {};
            const images = variant.images || [];
            const imageUrl = images.length > 0 ? images[0].imageUrl : '/images/no-image.png';
            const productName = variant.name || 'Sản phẩm';
            const colorName = variant.colorName || '';
            const sizeName = variant.sizeName || '';
            const attributes = [colorName, sizeName].filter(attr => attr).join(' / ');

            return `
                <tr>
                    <td class="d-flex align-items-center gap-2">
                        <img src="${imageUrl}" 
                             alt="product" class="rounded-3 border" width="90" height="90">
                        <span class="fw-semibold text-primary">${productName}</span>
                    </td>
                    <td>${attributes || 'Không có thuộc tính'}</td>
                    <td>${item.quantity}</td>
                    <td class="fw-bold text-end">${formatCurrency(item.priceAtPurchase * item.quantity)}đ</td>
                </tr>
            `;
        }).join('');
    }

    function getStatusLabel(status) {
        const statusLabels = {
            'pending': 'Chờ xác nhận',
            'processing': 'Đang xử lý',
            'shipped': 'Đang giao',
            'delivered': 'Đã giao',
            'cancelled': 'Đã huỷ'
        };
        return statusLabels[status] || status;
    }

    function formatDateTime(dateString) {
        const date = new Date(dateString);
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')} ${date.toLocaleDateString('vi-VN')}`;
    }

    // Hủy đơn hàng
    window.cancelOrder = function (orderId) {
        if (confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
            fetch(`/account/orders/${orderId}/cancel`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'ok') {
                        alert('Hủy đơn hàng thành công!');
                        // Reload lại tab hiện tại
                        const activeTab = document.querySelector('#orderHistoryTabs .nav-link.active');
                        if (activeTab) {
                            const status = activeTab.getAttribute('data-status');
                            loadOrdersByStatus(status);
                        }
                    } else {
                        alert('Lỗi: ' + (data.message || 'Không thể hủy đơn hàng'));
                    }
                })
                .catch(error => {
                    alert('Có lỗi xảy ra khi hủy đơn hàng');
                });
        }
    }

    // Event listeners cho order history tabs
    document.querySelectorAll('#orderHistoryTabs button[data-bs-toggle="tab"]').forEach(tab => {
        tab.addEventListener('shown.bs.tab', function (e) {
            const status = this.getAttribute('data-status');
            if (status) {
                loadOrdersByStatus(status);
            }
        });
    });

    // Load đơn hàng cho tab đầu tiên khi vào trang lịch sử đơn hàng
    const orderHistoryTab = document.getElementById('v-pills-order-history-tab');
    if (orderHistoryTab) {
        orderHistoryTab.addEventListener('shown.bs.tab', function () {
            // Load tab đầu tiên (pending) ngay khi vào trang lịch sử đơn hàng
            loadOrdersByStatus('pending');
        });
    }

    // ========== PROFILE FORM ==========
    // Xử lý trường nhập liệu có thể chỉnh sửa khi click
    document.querySelectorAll('.editable-field').forEach(field => {
        const fieldText = field.querySelector('.field-text');
        const fieldEdit = field.querySelector('.field-edit');
        const input = fieldEdit.querySelector('input');

        // Khi click vào text, hiện input để chỉnh sửa
        fieldText.addEventListener('click', function () {
            fieldText.style.display = 'none';
            fieldEdit.style.display = 'block';
            input.focus();
        });
    });

    // Xử lý form cập nhật thông tin
    const updateProfileBtn = document.getElementById('updateProfileBtn');

    /**
     * Hàm kiểm tra các trường thông tin cá nhân
     * @param {string} name - Họ tên người dùng
     * @param {string} phone - Số điện thoại người dùng
     * @return {Object} Kết quả kiểm tra với các thuộc tính isValid và message
     */
    function validateProfileData(name, phone) {
        // Kiểm tra họ tên
        if (!name || name.trim().length === 0) {
            return {
                isValid: false,
                message: 'Vui lòng nhập họ và tên của bạn'
            };
        }

        // Kiểm tra độ dài họ tên
        if (name.trim().length < 2) {
            return {
                isValid: false,
                message: 'Họ tên phải có ít nhất 2 ký tự'
            };
        }

        if (name.trim().length > 50) {
            return {
                isValid: false,
                message: 'Họ tên không được vượt quá 50 ký tự'
            };
        }

        // Kiểm tra số điện thoại nếu có nhập
        if (phone && phone.trim().length > 0) {
            if (!/^0[0-9]{9}$/.test(phone)) {
                return {
                    isValid: false,
                    message: 'Số điện thoại phải có 10 số và bắt đầu bằng số 0'
                };
            }
        }

        // Nếu tất cả đều hợp lệ
        return {
            isValid: true,
            message: ''
        };
    }

    if (updateProfileBtn) {
        updateProfileBtn.addEventListener('click', function () {
            // Lấy giá trị từ các input
            const nameInput = document.getElementById('customerName');
            const phoneInput = document.getElementById('customerPhone');

            const name = nameInput ? nameInput.value.trim() : '';
            const phone = phoneInput ? phoneInput.value.trim() : '';

            // Kiểm tra dữ liệu đầu vào
            const validationResult = validateProfileData(name, phone);
            if (!validationResult.isValid) {
                alert(validationResult.message);
                return;
            }

            // Hiển thị trạng thái đang xử lý
            const originalBtnText = updateProfileBtn.innerHTML;
            updateProfileBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-2"></i>Đang cập nhật...';
            updateProfileBtn.disabled = true;

            // Chuẩn bị dữ liệu để gửi
            const data = {
                name: name,
                phone: phone
            };

            // Gửi request cập nhật
            fetch('/account/update-profile', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(result => {
                    if (result.status === 'ok') {
                        // Cập nhật lại text hiển thị
                        document.querySelectorAll('.editable-field').forEach(field => {
                            const fieldName = field.getAttribute('data-field');
                            const fieldText = field.querySelector('.field-text');
                            const input = field.querySelector('input');

                            if (fieldName === 'name' && name) {
                                fieldText.textContent = name;
                            } else if (fieldName === 'phone') {
                                // Nếu phone có giá trị thì hiển thị, nếu không thì hiện "Chưa cập nhật"
                                fieldText.textContent = phone || 'Chưa cập nhật';
                            }

                            // Hiển thị lại text, ẩn input
                            fieldText.style.display = '';
                            field.querySelector('.field-edit').style.display = 'none';
                        });

                        // Hiển thị thông báo với Toast
                        alert('Thành công', 'Cập nhật thông tin thành công!');
                    } else {
                        // Hiển thị lỗi nếu có với Toast
                        const errorMessage = result.message || 'Có lỗi xảy ra khi cập nhật thông tin';
                        alert('Lỗi', errorMessage);
                    }

                    // Khôi phục trạng thái nút
                    updateProfileBtn.innerHTML = originalBtnText;
                    updateProfileBtn.disabled = false;
                })
                .catch(error => {
                    alert('Có lỗi xảy ra khi cập nhật thông tin');

                    // Khôi phục trạng thái nút
                    updateProfileBtn.innerHTML = originalBtnText;
                    updateProfileBtn.disabled = false;
                });
        });
    }

    // Hàm mở modal hoàn trả hàng: fetch chi tiết đơn hàng và render sản phẩm vào modal
    window.openReturnModal = function (orderId) {
        // Reset form hoàn trả
        const returnForm = document.getElementById('returnForm');
        if (returnForm) returnForm.reset();
        if (returnForm) returnForm.setAttribute('data-order-id', orderId);
        // Xóa preview ảnh cũ nếu có
        const imagePreviewContainer = document.getElementById('imagePreviewContainer');
        if (imagePreviewContainer) imagePreviewContainer.innerHTML = '';
        // Reset input file
        const returnImagesInput = document.getElementById('returnImages');
        if (returnImagesInput) returnImagesInput.value = '';
        // Fetch chi tiết đơn hàng để render danh sách sản phẩm
        fetch(`/account/orders/${orderId}/detail`)
            .then(response => response.json())
            .then(data => {
                if (data.status === 'ok' && data.data && Array.isArray(data.data.items)) {
                    const orderItems = data.data.items;
                    const productListContainer = document.querySelector('#returnModal .product-selection .row.g-3');
                    if (productListContainer) {
                        productListContainer.innerHTML = orderItems.map((item, idx) => {
                            const variant = item.variant || {};
                            const images = variant.images || [];
                            const imageUrl = images.length > 0 ? images[0].imageUrl : '/images/no-image.png';
                            const productName = variant.name || 'Sản phẩm';
                            const colorName = variant.colorName || '';
                            const sizeName = variant.sizeName || '';
                            const attributes = [colorName, sizeName].filter(attr => attr).join(' / ');
                            // Mỗi sản phẩm 1 dòng ngang, input số lượng + select tình trạng chỉ hiện khi tick, ẩn hoàn toàn khi chưa tick
                            return `
                                <div class="col-12">
                                    <div class="d-flex align-items-center gap-3 py-2 px-3 mb-2 product-return-item" style="border-bottom:1px solid #eee; position:relative;" data-index="${idx}" data-price="${item.priceAtPurchase}" data-variant-id="${variant.variantId || ''}">
                                        <input type="checkbox" class="form-check-input return-product-checkbox" id="returnProduct${idx}" data-index="${idx}">
                                        <img src="${imageUrl}" alt="product" class="rounded-3 border" width="60" height="60">
                                        <div class="flex-grow-1 d-flex flex-column flex-md-row align-items-md-center gap-2">
                                            <label for="returnProduct${idx}" class="fw-semibold text-primary mb-0">${productName}</label>
                                            <span class="text-secondary small">${attributes || 'Không có thuộc tính'}</span>
                                            <span class="text-secondary small">x${item.quantity}</span>
                                            <span class="fw-bold">${formatCurrency(item.priceAtPurchase)}đ</span>
                                        </div>
                                        <div class="return-extra-inputs d-flex flex-column align-items-end ms-auto" style="display:none; min-width:130px;">
                                            <input type="number" class="form-control form-control-sm rounded-pill return-quantity-input mb-1" min="1" max="${item.quantity}" value="1" style="width:120px;">
                                            <select class="form-select form-select-sm rounded-pill return-condition-select" style="width:120px;">
                                                <option value="new">Mới</option>
                                                <option value="used">Đã sử dụng</option>
                                                <option value="damaged">Hư hỏng</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            `;
                        }).join('');

                        // Hàm cập nhật tổng tiền hoàn trả
                        function updateReturnTotal() {
                            let total = 0;
                            productListContainer.querySelectorAll('.product-return-item').forEach(itemDiv => {
                                const checkbox = itemDiv.querySelector('.return-product-checkbox');
                                if (checkbox && checkbox.checked) {
                                    const price = parseFloat(itemDiv.getAttribute('data-price')) || 0;
                                    const qtyInput = itemDiv.querySelector('.return-quantity-input');
                                    const qty = qtyInput ? parseInt(qtyInput.value) : 1;
                                    total += price * qty;
                                }
                            });
                            document.getElementById('returnTotal').textContent = formatCurrency(total) + 'đ';
                        }

                        // Thêm sự kiện cho checkbox để hiện/ẩn input số lượng + select tình trạng, và cập nhật tổng tiền
                        productListContainer.querySelectorAll('.return-product-checkbox').forEach(checkbox => {
                            checkbox.addEventListener('change', function () {
                                const parent = this.closest('.product-return-item');
                                const extraInputs = parent.querySelector('.return-extra-inputs');
                                if (this.checked) {
                                    extraInputs.style.display = 'flex';
                                } else {
                                    extraInputs.style.display = 'none';
                                }
                                updateReturnTotal();
                            });
                        });
                        // Sự kiện thay đổi số lượng cũng cập nhật tổng tiền
                        productListContainer.querySelectorAll('.return-quantity-input').forEach(input => {
                            input.addEventListener('input', function () {
                                updateReturnTotal();
                            });
                        });
                        updateReturnTotal();
                    }
                }
            });
        // Hiển thị modal
        const modal = new bootstrap.Modal(document.getElementById('returnModal'));
        modal.show();
    };

    // ========== GỬI YÊU CẦU HOÀN TRẢ ==========
    const submitReturnBtn = document.getElementById('submitReturnBtn');
    if (submitReturnBtn) {
        submitReturnBtn.onclick = function () {
            const returnForm = document.getElementById('returnForm');
            const orderId = returnForm.getAttribute('data-order-id');
            // Lấy danh sách sản phẩm hoàn trả
            const selectedProducts = [];
            document.querySelectorAll('#returnModal .product-return-item').forEach(itemDiv => {
                const checkbox = itemDiv.querySelector('.return-product-checkbox');
                if (checkbox && checkbox.checked) {
                    const variantId = itemDiv.getAttribute('data-variant-id');
                    const qtyInput = itemDiv.querySelector('.return-quantity-input');
                    const qty = qtyInput ? parseInt(qtyInput.value) : 1;
                    const conditionSelect = itemDiv.querySelector('.return-condition-select');
                    const condition = conditionSelect ? conditionSelect.value : '';
                    // Sửa lại field cho đúng DTO backend
                    selectedProducts.push({
                        variantId: variantId,
                        returnQuantity: qty,
                        productCondition: condition,
                        itemReturnReason: ''
                    });
                }
            });
            if (selectedProducts.length === 0) {
                alert('Vui lòng chọn ít nhất 1 sản phẩm để hoàn trả!');
                return;
            }
            // Lý do, mô tả
            const reasonSelect = returnForm.querySelector('select[required]');
            const reason = reasonSelect ? reasonSelect.value : '';
            const description = returnForm.querySelector('textarea[required]')?.value || '';
            // Ảnh
            const images = window._returnImagesFiles ? window._returnImagesFiles.slice(0, 5) : [];
            // Tạo FormData để gửi cả ảnh
            const formData = new FormData();
            // Lấy loại yêu cầu
            const returnType = returnForm.querySelector('input[name="returnType"]:checked')?.value || 'REFUND';
            formData.append('orderId', orderId);
            formData.append('reason', reason);
            formData.append('description', description);
            formData.append('returnType', returnType);
            formData.append('products', JSON.stringify(selectedProducts));
            images.forEach((file, idx) => {
                formData.append('images', file);
            });
            // Gửi lên server
            fetch('/account/return-request', {
                method: 'POST',
                body: formData
            })
                .then(res => res.json())
                .then(data => {
                    if (data.status === 'ok') {
                        alert('Gửi yêu cầu hoàn trả thành công!');
                        const modal = bootstrap.Modal.getInstance(document.getElementById('returnModal'));
                        if (modal) modal.hide();

                        const activeTab = document.querySelector('#orderHistoryTabs .nav-link.active');
                        if (activeTab) {
                            const status = activeTab.getAttribute('data-status');
                            if (status) {
                                loadOrdersByStatus(status);
                            }
                        }
                    } else {
                        alert('Lỗi: ' + (data.message || 'Không gửi được yêu cầu hoàn trả'));
                    }
                })
                .catch(() => {
                    alert('Có lỗi xảy ra khi gửi yêu cầu hoàn trả!');
                });
        }
    }

    // ========== XỬ LÝ ẢNH SẢN PHẨM LỖI ==========
    const addImageBtn = document.getElementById('addImageBtn');
    const returnImagesInput = document.getElementById('returnImages');
    const imagePreviewContainer = document.getElementById('imagePreviewContainer');
    window._returnImagesFiles = [];
    if (addImageBtn && returnImagesInput) {
        addImageBtn.onclick = function () {
            returnImagesInput.click();
        };
        returnImagesInput.onchange = function (e) {
            if (!imagePreviewContainer) return;
            let files = Array.from(e.target.files);
            // Cộng dồn file mới vào file cũ
            window._returnImagesFiles = window._returnImagesFiles.concat(files);
            // Giữ tối đa 5 ảnh
            if (window._returnImagesFiles.length > 5) {
                window._returnImagesFiles = window._returnImagesFiles.slice(0, 5);
            }
            // Preview lại toàn bộ ảnh
            imagePreviewContainer.innerHTML = '';
            window._returnImagesFiles.forEach(file => {
                const reader = new FileReader();
                reader.onload = function (ev) {
                    const img = document.createElement('img');
                    img.src = ev.target.result;
                    img.style.width = '70px';
                    img.style.height = '70px';
                    img.style.objectFit = 'cover';
                    img.className = 'rounded-3 border me-2 mb-2';
                    imagePreviewContainer.appendChild(img);
                };
                reader.readAsDataURL(file);
            });
            // Reset input để lần sau chọn tiếp được
            returnImagesInput.value = '';
        };
    }
});