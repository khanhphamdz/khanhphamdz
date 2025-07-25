// Cache để lưu tên tỉnh/huyện/xã
const provinceCache = {};
const districtCache = {};
const wardCache = {};

// Khởi tạo trang
document.addEventListener('DOMContentLoaded', function() {
    loadCustomers();
    loadProvincesForFilter();
});

// Load danh sách khách hàng
function loadCustomers() {
    fetch('/admin/customer/api/list')
        .then(response => response.json())
        .then(data => {
            if (data.status === 'ok') {
                displayCustomers(data.data);
            } else {
                console.error('Lỗi:', data.message);
                showAlert('Lỗi khi tải danh sách khách hàng', 'error');
            }
        })
        .catch(error => {
            console.error('Lỗi:', error);
            showAlert('Lỗi kết nối', 'error');
        });
}

// Hiển thị danh sách khách hàng
function displayCustomers(customers) {
    const tbody = document.getElementById('customerTableBody');
    tbody.innerHTML = '';

    if (!customers || customers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Không có dữ liệu</td></tr>';
        return;
    }

    customers.forEach((customer, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>KH${customer.customerId.toString().padStart(3, '0')}</td>
            <td>${customer.name || 'N/A'}</td>
            <td>${customer.phone || 'N/A'}</td>
            <td>${customer.email || 'N/A'}</td>
            <td>Đang tải...</td>
            <td>
                <button class="btn btn-info btn-sm me-1" onclick="showCustomerDetails(${customer.customerId})">
                    <i class="fa-solid fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
        
        // Load địa chỉ cho từng khách hàng
        loadCustomerAddressForTable(customer, row);
    });
}

// Load địa chỉ cho bảng
async function loadCustomerAddressForTable(customer, row) {
    try {
        const response = await fetch(`/admin/customer/api/${customer.customerId}`);
        const data = await response.json();
        
        if (data.status === 'ok' && data.data.address) {
            const address = data.data.address;
            const provinceName = await getProvinceNameById(address.provinceId);
            row.cells[5].textContent = provinceName || 'N/A';
        } else {
            row.cells[5].textContent = 'N/A';
        }
    } catch (error) {
        console.error('Lỗi khi lấy địa chỉ:', error);
        row.cells[5].textContent = 'N/A';
    }
}

// Tìm kiếm khách hàng
function searchCustomers() {
    const name = document.getElementById('filterName').value.trim().toLowerCase();
    const phone = document.getElementById('filterPhone').value.trim();
    const province = document.getElementById('filterProvince').value.trim();

    fetch(`/admin/customer/api/list`)
        .then(response => response.json())
        .then(async data => {
            if (data.status === 'ok') {
                let customers = data.data;
                // Lọc theo tên
                if (name) {
                    customers = customers.filter(c => (c.name || '').toLowerCase().includes(name));
                }
                // Lọc theo số điện thoại
                if (phone) {
                    customers = customers.filter(c => (c.phone || '').includes(phone));
                }
                // Lọc theo tỉnh/thành (khu vực)
                if (province) {
                    // Lọc bất đồng bộ vì phải fetch API lấy tên tỉnh
                    const filtered = [];
                    for (const customer of customers) {
                        const res = await fetch(`/admin/customer/api/${customer.customerId}`);
                        const detail = await res.json();
                        if (detail.status === 'ok' && detail.data.address) {
                            const address = detail.data.address;
                            const provinceName = await getProvinceNameById(address.provinceId);
                            // So sánh chuẩn hóa
                            if (
                                provinceName &&
                                provinceName.trim().toLowerCase() === province.trim().toLowerCase()
                            ) {
                                filtered.push(customer);
                            }
                        }
                    }
                    customers = filtered;
                }
                displayCustomers(customers);
            } else {
                console.error('Lỗi:', data.message);
                showAlert('Lỗi khi tìm kiếm', 'error');
            }
        })
        .catch(error => {
            console.error('Lỗi:', error);
            showAlert('Lỗi kết nối', 'error');
        });
}

// Reset bộ lọc
function resetFilter() {
    document.getElementById('filterName').value = '';
    document.getElementById('filterPhone').value = '';
    document.getElementById('filterProvince').value = '';
    loadCustomers();
}

// Hiển thị chi tiết khách hàng
async function showCustomerDetails(customerId) {
    try {
        const response = await fetch(`/admin/customer/api/${customerId}`);
        const data = await response.json();
        
        if (data.status === 'ok') {
            const customer = data.data.customer;
            const address = data.data.address; // chỉ 1 địa chỉ mặc định
            
            // Cập nhật thông tin cá nhân
            document.getElementById('viewCustomerCode').value = `KH${customer.customerId.toString().padStart(3, '0')}`;
            document.getElementById('viewCustomerName').value = customer.name || 'N/A';
            document.getElementById('viewCustomerPhone').value = customer.phone || 'N/A';
            document.getElementById('viewCustomerEmail').value = customer.email || 'N/A';
            
            // Cập nhật thông tin địa chỉ
            if (address) {
                document.getElementById('viewAddressName').value = address.name || 'N/A';
                document.getElementById('viewAddressPhone').value = address.phone || 'N/A';
                // Load tên tỉnh/huyện/xã
                const provinceName = await getProvinceNameById(address.provinceId);
                const districtName = await getDistrictNameById(address.districtId);
                let wardName = 'Không xác định';
                if (address.districtId && address.wardId && /^\d+$/.test(address.wardId)) {
                    // Lấy danh sách phường/xã của quận/huyện
                    try {
                        const res = await fetch(`https://provinces.open-api.vn/api/d/${address.districtId}?depth=2`);
                        const data = await res.json();
                        if (data.wards && Array.isArray(data.wards)) {
                            const found = data.wards.find(w => String(w.code) === String(address.wardId));
                            if (found) wardName = found.name;
                        }
                    } catch (e) {}
                }
                document.getElementById('viewAddressProvince').value = provinceName || 'N/A';
                document.getElementById('viewAddressDistrict').value = districtName || 'N/A';
                document.getElementById('viewAddressWard').value = wardName || 'Không xác định';
                document.getElementById('viewAddressDetail').value = address.specificAddress || 'N/A';
            } else {
                // Nếu không có địa chỉ
                document.getElementById('viewAddressName').value = 'N/A';
                document.getElementById('viewAddressPhone').value = 'N/A';
                document.getElementById('viewAddressProvince').value = 'N/A';
                document.getElementById('viewAddressDistrict').value = 'N/A';
                document.getElementById('viewAddressWard').value = 'N/A';
                document.getElementById('viewAddressDetail').value = 'N/A';
            }
            // Hiển thị modal
            const modal = new bootstrap.Modal(document.getElementById('viewCustomerModal'));
            modal.show();
        } else {
            showAlert('Lỗi khi tải thông tin khách hàng', 'error');
        }
    } catch (error) {
        console.error('Lỗi:', error);
        showAlert('Lỗi kết nối', 'error');
    }
}

// Load danh sách tỉnh cho bộ lọc
function loadProvincesForFilter() {
    fetch('https://provinces.open-api.vn/api/p/')
        .then(response => response.json())
        .then(data => {
            const select = document.getElementById('filterProvince');
            data.forEach(province => {
                const option = document.createElement('option');
                option.value = province.name;
                option.textContent = province.name;
                select.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Lỗi khi tải danh sách tỉnh:', error);
        });
}

// Lấy tên tỉnh theo ID
async function getProvinceNameById(id) {
    if (provinceCache[id]) return provinceCache[id];
    
    try {
        const response = await fetch(`/api/province-proxy/province/${id}`);
        const data = await response.json();
        const name = data.name || 'Không xác định';
        provinceCache[id] = name;
        return name;
    } catch (error) {
        console.error('Lỗi khi lấy tên tỉnh:', error);
        return 'Không xác định';
    }
}

// Lấy tên huyện theo ID
async function getDistrictNameById(id) {
    if (districtCache[id]) return districtCache[id];
    
    try {
        const response = await fetch(`/api/province-proxy/district/${id}`);
        const data = await response.json();
        const name = data.name || 'Không xác định';
        districtCache[id] = name;
        return name;
    } catch (error) {
        console.error('Lỗi khi lấy tên huyện:', error);
        return 'Không xác định';
    }
}

// Lấy tên xã theo ID
async function getWardNameById(id) {
    if (wardCache[id]) return wardCache[id];
    
    try {
        const response = await fetch(`/api/province-proxy/ward/${id}`);
        const data = await response.json();
        const name = data.name || 'Không xác định';
        wardCache[id] = name;
        return name;
    } catch (error) {
        console.error('Lỗi khi lấy tên xã:', error);
        return 'Không xác định';
    }
}

// Hiển thị thông báo
function showAlert(message, type = 'info') {
    // Tạo alert element
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show position-fixed`;
    alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    // Tự động ẩn sau 3 giây
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 3000);
}

