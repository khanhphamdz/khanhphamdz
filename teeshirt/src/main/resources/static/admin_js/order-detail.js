// Lấy orderId từ input hidden
const orderId = document.getElementById('orderId').value;

// Gọi API lấy chi tiết đơn hàng
async function fetchOrderDetail(orderId) {
    try {
        showLoading();
        const response = await fetch(`/api/admin/orders/${orderId}`);
        const result = await response.json();
        hideLoading();
        if (result.status === "ok") {
            return result.data;
        } else {
            showToast("Không tìm thấy đơn hàng!", 'danger');
            return null;
        }
    } catch (error) {
        hideLoading();
        showToast("Lỗi khi lấy dữ liệu đơn hàng!", 'danger');
        return null;
    }
}
    
// Map dữ liệu API về format view
function mapApiOrderToViewModel(apiOrder) {
    return {
        id: apiOrder.orderId,
        customer: apiOrder.customerName || "Khách lẻ",
        phone: apiOrder.customerPhone || "",
        status: apiOrder.status,
        createdAt: apiOrder.createdAt ? new Date(apiOrder.createdAt).toLocaleString() : "",
        source: apiOrder.orderType,
        address: apiOrder.shippingAddress || "",
        note: apiOrder.note || "",
        items: (apiOrder.items || []).map(item => ({
            id: item.orderItemId,
            name: item.variant?.name || "",
            color: item.variant?.colorName || "",
            quantity: item.quantity,
            price: item.price,
            status: apiOrder.status === "completed" ? "Thành công" : "Chờ xác nhận",
            image: item.variant?.images?.[0]?.imageUrl || "https://via.placeholder.com/50"
        })),
        shipping: apiOrder.shippingFee || 0,
        discount: apiOrder.discountAmount || 0,
        finalAmount: apiOrder.finalAmount || 0,
        paymentMethod: apiOrder.payment?.paymentType || "Chưa thanh toán",
        statusHistory: apiOrder.statusHistory || []
    };
}

function mapApiStatusHistory(apiStatusHistory) {
    return (apiStatusHistory || []).map(h => ({
        timestamp: h.createdAt ? new Date(h.createdAt).toLocaleString() : "",
        user: "", // Nếu API trả về user thì lấy, không thì để trống
        action: h.statusName
    }));
}

function loadOrderDetails(order) {
    document.getElementById('orderTitle').textContent = `Chi tiết đơn hàng ${order.id}`;
    document.getElementById('detailOrderId').textContent = order.id;
    document.getElementById('detailStatus').textContent = order.status;
    document.getElementById('detailSource').textContent = order.source;
    document.getElementById('detailCustomer').textContent = order.customer;
    document.getElementById('detailPhone').textContent = order.phone;
    document.getElementById('detailAddress').textContent = order.address;
    document.getElementById('detailCreatedAt').textContent = order.createdAt;
    document.getElementById('detailNote').textContent = order.note || 'Không có';
    loadOrderItems(order.items);
    loadHistory(order.statusHistory);
    updateSummary(order);
    renderOrderActions(order);
}

function loadOrderItems(items) {
    const itemsBody = document.getElementById('orderItems');
    itemsBody.innerHTML = '';
    items.forEach((item, index) => {
        const row = `
            <tr>
                <td>${index + 1}</td>
                <td><img src="${item.image}" alt="${item.name}"></td>
                <td>${item.name}</td>
                <td>${item.color}</td>
                <td>${item.quantity}</td>
                <td>${(item.price * item.quantity).toLocaleString()} VND</td>
                <td><span class="badge ${item.status === 'Thành công' ? 'bg-success' : 'bg-warning'}">${item.status}</span></td>
                <td></td>
            </tr>
        `;
        itemsBody.innerHTML += row;
    });
}

function loadHistory(statusHistory) {
    const historyBody = document.getElementById('orderTimeline');
    historyBody.innerHTML = '';
    statusHistory.forEach(entry => {
        const row = `
            <tr>
                <td>${entry.timestamp}</td>
                <td>${entry.user}</td>
                <td>${entry.action}</td>
            </tr>
        `;
        historyBody.innerHTML += row;
    });
}

function updateSummary(order) {
    document.getElementById('summaryTotal').textContent = (order.items.reduce((sum, item) => sum + (item.price * item.quantity), 0)).toLocaleString() + ' VND';
    document.getElementById('summaryShipping').textContent = order.shipping.toLocaleString() + ' VND';
    document.getElementById('summaryDiscount').textContent = order.discount.toLocaleString() + ' VND';
    document.getElementById('summaryTemp').textContent = (order.items.reduce((sum, item) => sum + (item.price * item.quantity), 0) + order.shipping).toLocaleString() + ' VND';
    document.getElementById('summaryFinal').textContent = order.finalAmount.toLocaleString() + ' VND';
}

// Hiển thị các nút thao tác trạng thái
function renderOrderActions(order) {
    let actionsDiv = document.getElementById('orderActions');
    if (!actionsDiv) {
        actionsDiv = document.createElement('div');
        actionsDiv.id = 'orderActions';
        actionsDiv.className = 'mt-3';
        document.querySelector('main').appendChild(actionsDiv);
    }
    actionsDiv.innerHTML = '';
    const status = order.status;
    if (status === 'pending') {
        actionsDiv.innerHTML = `
            <button class="btn btn-success me-2" onclick="updateOrderStatus('processing')">Xác nhận đơn</button>
            <button class="btn btn-danger" onclick="cancelOrder()">Hủy đơn</button>
        `;
    } else if (status === 'processing') {
        actionsDiv.innerHTML = `
            <button class="btn btn-primary me-2" onclick="updateOrderStatus('shipping')">Đã giao cho vận chuyển</button>
            <button class="btn btn-danger" onclick="cancelOrder()">Hủy đơn</button>
        `;
    } else if (status === 'shipping') {
        actionsDiv.innerHTML = `
            <button class="btn btn-success me-2" onclick="updateOrderStatus('delivered')">Đã giao thành công</button>
            <button class="btn btn-danger" onclick="cancelOrder()">Hủy đơn</button>
        `;
    } else if (status === 'delivered') {
        actionsDiv.innerHTML = `
            <button class="btn btn-warning" onclick="returnOrder()">Trả hàng</button>
        `;
    }
}

// Toast UI
function showToast(message, type = 'success') {
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        toastContainer.style.zIndex = 9999;
        document.body.appendChild(toastContainer);
    }
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${type} border-0 show mb-2`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    toastContainer.appendChild(toast);
    setTimeout(() => { toast.remove(); }, 3000);
}

// Loading UI
function showLoading() {
    let loading = document.getElementById('globalLoading');
    if (!loading) {
        loading = document.createElement('div');
        loading.id = 'globalLoading';
        loading.innerHTML = `<div class="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center" style="background:rgba(0,0,0,0.2);z-index:99999;"><div class='spinner-border text-primary' style='width:3rem;height:3rem;' role='status'></div></div>`;
        document.body.appendChild(loading);
    }
    loading.style.display = 'block';
}
function hideLoading() {
    const loading = document.getElementById('globalLoading');
    if (loading) loading.style.display = 'none';
}

// API thao tác trạng thái
async function updateOrderStatus(newStatus) {
    if (!confirm('Bạn chắc chắn muốn chuyển trạng thái đơn hàng?')) return;
    showLoading();
    const res = await fetch(`/api/admin/orders/${orderId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    });
    const data = await res.json();
    hideLoading();
    if (data.status === 'ok') {
        showToast('Cập nhật trạng thái thành công!', 'success');
        setTimeout(() => location.reload(), 1000);
    } else {
        showToast(data.message || 'Cập nhật thất bại!', 'danger');
    }
}

async function cancelOrder() {
    const reason = prompt('Nhập lý do hủy đơn:');
    if (!reason) return;
    showLoading();
    const res = await fetch(`/api/admin/orders/${orderId}/cancel`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reason })
    });
    const data = await res.json();
    hideLoading();
    if (data.status === 'ok') {
        showToast('Đã hủy đơn hàng!', 'success');
        setTimeout(() => location.reload(), 1000);
    } else {
        showToast(data.message || 'Hủy đơn thất bại!', 'danger');
    }
}

async function returnOrder() {
    const reason = prompt('Nhập lý do trả hàng:');
    if (!reason) return;
    showLoading();
    const res = await fetch(`/api/admin/orders/${orderId}/return`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reason })
    });
    const data = await res.json();
    hideLoading();
    if (data.status === 'ok') {
        showToast('Đã trả hàng!', 'success');
        setTimeout(() => location.reload(), 1000);
    } else {
        showToast(data.message || 'Trả hàng thất bại!', 'danger');
    }
}

window.onload = async () => {
    if (!orderId) {
        showToast("Không tìm thấy orderId!", 'danger');
        return;
    }
    const apiOrder = await fetchOrderDetail(orderId);
    if (!apiOrder) return;
    const order = mapApiOrderToViewModel(apiOrder);
    order.statusHistory = mapApiStatusHistory(apiOrder.statusHistory);
    loadOrderDetails(order);
};
