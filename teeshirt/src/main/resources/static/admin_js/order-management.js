let currentPage = 0;
let pageSize = 10;
let totalPages = 1;
let currentKeyword = '';
let currentStatus = '';
let currentOrderType = '';
let currentDate = '';
let ordersData = [];

window.onload = function () {
    bindFilterEvents();
    loadOrders();
};

function bindFilterEvents() {
    document.getElementById('searchKeyword').onkeyup = debounce(applyFilters, 400);
    document.getElementById('filterStatus').onchange = applyFilters;
    document.getElementById('filterSource').onchange = applyFilters;
    document.getElementById('filterDate').onchange = applyFilters;
    document.getElementById('filterForm').onsubmit = e => { e.preventDefault(); applyFilters(); };
    document.querySelector('.btn-primary')?.addEventListener('click', applyFilters);
    document.querySelector('.btn-secondary')?.addEventListener('click', resetFilters);
    document.getElementById('statusDropdown')?.addEventListener('click', (e) => {
        if (e.target.tagName === 'A') {
            const status = e.target.getAttribute('data-status');
            document.getElementById('filterStatus').value = status === 'all' ? '' : status;
            applyFilters();
        }
    });
}

function debounce(fn, delay) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => fn.apply(this, args), delay);
    };
}

// Map trạng thái tiếng Việt sang tiếng Anh cho filter
const statusMap = {
    'cho-xac-nhan': 'pending',
    'dang-chuan-bi': 'processing',
    'dang-giao': 'shipped',
    'hoan-tat': 'delivered',
    'da-huy': 'cancelled',
    'tra-hang': 'returned'
};

function applyFilters() {
    currentKeyword = document.getElementById('searchKeyword').value.trim();
    let statusVN = document.getElementById('filterStatus').value;
    currentStatus = statusMap[statusVN] || statusVN; // chuyển sang tiếng Anh nếu có
    currentOrderType = document.getElementById('filterSource').value;
    currentDate = document.getElementById('filterDate').value;
    currentPage = 0;
    loadOrders();
}

function resetFilters() {
    document.getElementById('filterForm').reset();
    currentKeyword = '';
    currentStatus = '';
    currentOrderType = '';
    currentDate = '';
    currentPage = 0;
    loadOrders();
}

async function loadOrders() {
    showTableLoading(true);
    let url = `/api/admin/orders?page=${currentPage}&size=${pageSize}`;
    if (currentKeyword) url += `&keyword=${encodeURIComponent(currentKeyword)}`;
    if (currentStatus) url += `&status=${encodeURIComponent(currentStatus)}`;
    if (currentOrderType) url += `&orderType=${encodeURIComponent(currentOrderType)}`;
    if (currentDate) url += `&createdDate=${currentDate}`;

    console.log(url);
    
    try {
        const res = await fetch(url);
        const data = await res.json();
        if (data.status === 'ok' && data.data && data.data) {
            ordersData = data.data;
            renderOrders(ordersData);
            renderPagination(data.data);
        } else {
            ordersData = [];
            renderOrders([]);
            renderPagination({ totalPages: 1, number: 0 });
        }
    } catch (e) {
        ordersData = [];
        renderOrders([]);
        renderPagination({ totalPages: 1, number: 0 });
        showToast('Lỗi kết nối server', 'danger');
    } finally {
        showTableLoading(false);
    }
}

function renderOrders(orders) {
    const tbody = document.getElementById('orderTablex');
    tbody.innerHTML = '';
    if (!orders.length) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center">Không có đơn hàng nào</td></tr>`;
        return;
    }
    orders.forEach((order, idx) => {
        const statusVN = {
            'pending': 'Chờ xác nhận',
            'processing': 'Đang chuẩn bị',
            'shipped': 'Đang giao',
            'delivered': 'Hoàn tất',
            'cancelled': 'Đã hủy',
            'returned': 'Trả hàng'
        }[order.status] || order.status;
        const orderTypeVN = {
            'online': 'Online',
            'offline': 'Tại quầy'
        }[order.orderType] || order.orderType;
        const customerName = order.shippingAddress?.name || 'Khách lẻ';
        const createdAt = order.createdAt ? new Date(order.createdAt).toLocaleString('vi-VN') : '';
        const total = order.finalAmount?.toLocaleString('vi-VN') + ' VNĐ' || '';
        tbody.innerHTML += `
            <tr>
                <td>${idx + 1 + currentPage * pageSize}</td>
                <td>#ORD${order.orderId}</td>
                <td>${customerName}</td>
                <td>${statusVN}</td>
                <td>${createdAt}</td>
                <td>${orderTypeVN}</td>
                <td>${total}</td>
                <td>
                    <a href="/admin/order/${order.orderId}" class="btn btn-sm btn-primary"><i class="fa-solid fa-eye"></i></a>
                    ${order.status !== 'cancelled' && order.status !== 'delivered' ? 
                        `<button class="btn btn-sm btn-danger ms-2" onclick="openCancelOrder(${order.orderId})"><i class="fa-solid fa-trash"></i></button>` : 
                        ''}
                </td>
            </tr>
        `;
    });
}

function renderPagination(pageData) {
    totalPages = pageData.totalPages || 1;
    currentPage = pageData.number || 0;
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';
    pagination.innerHTML += `<li class="page-item ${currentPage === 0 ? 'disabled' : ''}"><a class="page-link" href="#" onclick="gotoPage(${currentPage - 1})">Trước</a></li>`;
    for (let i = 0; i < totalPages; i++) {
        pagination.innerHTML += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link" href="#" onclick="gotoPage(${i})">${i + 1}</a></li>`;
    }
    pagination.innerHTML += `<li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}"><a class="page-link" href="#" onclick="gotoPage(${currentPage + 1})">Sau</a></li>`;
}

function gotoPage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    loadOrders();
}

async function openCancelOrder(orderId) {
    const order = ordersData.find(o => o.orderId === orderId);
    if (!order) {
        showToast('Không tìm thấy đơn hàng', 'danger');
        return;
    }
    document.getElementById('cancelOrderId').textContent = `#ORD${order.orderId}`;
    document.getElementById('cancelReason').value = '';
    document.getElementById('confirmCancelBtn').onclick = () => confirmCancel(orderId);
    const modal = new bootstrap.Modal(document.getElementById('cancelOrderModal'));
    modal.show();
}

async function confirmCancel(orderId) {
    const reason = document.getElementById('cancelReason').value.trim();
    if (!reason) {
        showToast('Vui lòng nhập lý do hủy', 'warning');
        return;
    }
    showTableLoading(true);
    try {
        const res = await fetch(`/api/admin/orders/${orderId}/cancel`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason })
        });
        const data = await res.json();
        if (data.status === 'ok') {
            showToast('Hủy đơn hàng thành công', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('cancelOrderModal'));
            modal.hide();
            loadOrders();
        } else {
            showToast(data.message || 'Lỗi khi hủy đơn hàng', 'danger');
        }
    } catch (e) {
        showToast('Lỗi kết nối server', 'danger');
    } finally {
        showTableLoading(false);
    }
}

function showTableLoading(show) {
    const tbody = document.getElementById('orderTablex');
    if (show) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center"><div class='spinner-border text-primary' role='status'><span class='visually-hidden'>Loading...</span></div></td></tr>`;
    }
}

function showToast(message, type = 'success') {
    const toastContainer = document.querySelector('.toast-container');
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    toast.innerHTML = `
    <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
    </div>
`;
    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    setTimeout(() => bsToast.hide(), 3000);
}