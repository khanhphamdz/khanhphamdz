// Xử lý hiển thị thời gian chờ
function updateWaitingTime() {
    const waitingElements = document.querySelectorAll('.status-pending + small');
    waitingElements.forEach(element => {
        const startDate = new Date(element.getAttribute('data-start'));
        const now = new Date();
        const diffDays = Math.floor((now - startDate) / (1000 * 60 * 60 * 24));
        element.textContent = `Đã chờ ${diffDays} ngày`;
    });
}
setInterval(updateWaitingTime, 60000);
updateWaitingTime();

// --- Load danh sách trả hàng từ API và render bảng ---
async function loadRefundList() {
    const res = await fetch('/api/return-request/list');
    const data = await res.json();
    console.log('API /api/return-request/list trả về:', data);
    let list = [];
    if (Array.isArray(data)) {
        list = data;
    } else if (data && Array.isArray(data.data)) {
        list = data.data;
    } else if (data && Array.isArray(data.content)) {
        list = data.content;
    } else {
        // Không phải mảng, không render gì cả
        return;
    }
    renderRefundList(list);
}

function renderRefundList(list) {
    const tbody = document.querySelector('table tbody');
    tbody.innerHTML = '';
    list.forEach(refund => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${refund.orderId}</td>
            <td>${refund.customerId || ''}</td>
            <td>${refund.requestDate ? new Date(refund.requestDate).toLocaleDateString() : ''}</td>
            <td>${refund.returnReason || ''}</td>
            <td>${refund.refundAmount ? refund.refundAmount.toLocaleString() + ' VNĐ' : ''}</td>
            <td>${refund.returnType === 'EXCHANGE' ? 'Đổi hàng' : 'Hoàn tiền'}</td>
            <td>
                <span class="status-badge status-${refund.returnStatus ? refund.returnStatus.toLowerCase() : ''}">${getStatusText(refund.returnStatus)}</span>
            </td>
            <td class="action-buttons">
                <button class="btn btn-info" title="Xem chi tiết" onclick="openDetailModal(${refund.returnId})">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn btn-success" title="Duyệt" onclick="openApproveModal(${refund.returnId})" ${refund.returnStatus !== 'PENDING' ? 'disabled' : ''}>
                    <i class="fas fa-check"></i>
                </button>
                <button class="btn btn-danger" title="Từ chối" onclick="openRejectModal(${refund.returnId})" ${refund.returnStatus !== 'PENDING' ? 'disabled' : ''}>
                    <i class="fas fa-times"></i>
                </button>
                <button class="btn btn-primary" title="Hoàn tất" onclick="openCompleteModal(${refund.returnId})" ${refund.returnStatus !== 'APPROVED' ? 'disabled' : ''}>
                    <i class="fas fa-check-double"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function getStatusText(status) {
    switch (status) {
        case 'PENDING': return 'Chờ xử lý';
        case 'APPROVED': return 'Đã duyệt';
        case 'REJECTED': return 'Từ chối';
        case 'COMPLETED': return 'Hoàn tất';
        case 'CANCELLED': return 'Đã hủy';
        default: return status;
    }
}

// --- Modal chi tiết ---
window.openDetailModal = async function(returnId) {
    const res = await fetch(`/api/return-request/${returnId}/full`);
    const refund = await res.json();
    // Render dữ liệu vào modal #viewRefundModal
    const modal = document.getElementById('viewRefundModal');
    // Tiêu đề
    modal.querySelector('.modal-title').innerText = `Chi tiết yêu cầu trả hàng #${refund.orderId}`;
    // Thông tin đơn hàng
    modal.querySelector('#order-code').innerHTML = `<strong>Mã đơn hàng:</strong> #${refund.orderId}`;
    modal.querySelector('#order-date').innerHTML = `<strong>Ngày đặt hàng:</strong> ${refund.orderDate ? new Date(refund.orderDate).toLocaleDateString() : ''}`;
    modal.querySelector('#order-payment').innerHTML = `<strong>Phương thức thanh toán:</strong> ${refund.orderType || ''}`;
    // Thông tin khách hàng
    modal.querySelector('#customer-name').innerHTML = `<strong>Họ tên:</strong> ${refund.customerName || ''}`;
    modal.querySelector('#customer-phone').innerHTML = `<strong>Số điện thoại:</strong> ${refund.customerPhone || ''}`;
    modal.querySelector('#customer-email').innerHTML = `<strong>Email:</strong> ${refund.customerEmail || ''}`;
    // Sản phẩm trả hàng
    const tbody = modal.querySelector('#refund-items-tbody');
    tbody.innerHTML = '';
    if (refund.returnItems && refund.returnItems.length > 0) {
        refund.returnItems.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.productName} (${item.variantName})</td>
                <td>${item.returnQuantity}</td>
                <td>${item.priceAtPurchase ? item.priceAtPurchase.toLocaleString() + ' VNĐ' : ''}</td>
                <td>${item.priceAtPurchase && item.returnQuantity ? (item.priceAtPurchase * item.returnQuantity).toLocaleString() + ' VNĐ' : ''}</td>
            `;
            tbody.appendChild(tr);
        });
    }
    // Lý do trả hàng
    modal.querySelector('#refund-reason').innerText = refund.returnReason || '';
    // Hình ảnh đính kèm
    const imgRow = modal.querySelector('#refund-images-row');
    imgRow.innerHTML = '';
    if (refund.returnItems && refund.returnItems.length > 0) {
        refund.returnItems.forEach(item => {
            if (item.images && item.images.length > 0) {
                item.images.forEach(url => {
                    const col = document.createElement('div');
                    col.className = 'col-md-4';
                    col.innerHTML = `<img src="${url}" class="img-fluid rounded" alt="Hình ảnh sản phẩm">`;
                    imgRow.appendChild(col);
                });
            }
        });
    }
    // Loại yêu cầu
    document.getElementById('return-type').innerText = refund.returnType === 'EXCHANGE' ? 'Đổi hàng' : 'Hoàn tiền';
    // Hiển thị modal
    new bootstrap.Modal(modal).show();
}

// --- Modal duyệt ---
window.openApproveModal = function(returnId) {
    document.getElementById('approveRefundModal').dataset.returnId = returnId;
    const typeSelect = document.getElementById('approveTypeSelect');
    const refundGroup = document.getElementById('approveRefundAmountGroup');
    const exchangeGroup = document.getElementById('approveExchangeProductGroup');
    // Mặc định
    typeSelect.value = 'refund';
    refundGroup.style.display = '';
    exchangeGroup.style.display = 'none';
    typeSelect.onchange = function() {
        if (this.value === 'exchange') {
            refundGroup.style.display = 'none';
            exchangeGroup.style.display = '';
        } else if (this.value === 'refund') {
            refundGroup.style.display = '';
            exchangeGroup.style.display = 'none';
        } else {
            refundGroup.style.display = '';
            exchangeGroup.style.display = '';
        }
    };
    new bootstrap.Modal(document.getElementById('approveRefundModal')).show();
}
document.querySelector('#approveRefundModal .btn-success').onclick = async function() {
    const returnId = document.getElementById('approveRefundModal').dataset.returnId;
    const type = document.getElementById('approveTypeSelect').value;
    const amount = document.querySelector('#approveRefundAmountGroup input[type="number"]').value;
    const note = document.querySelector('#approveRefundModal textarea').value;
    const productName = document.querySelector('#approveExchangeProductGroup input[type="text"]').value;
    const productQty = document.querySelector('#approveExchangeProductGroup input[type="number"]').value;
    let adminNote = '';
    if (type === 'refund') {
        adminNote = `Duyệt hoàn tiền | Số tiền: ${amount} VNĐ`;
    } else if (type === 'exchange') {
        adminNote = `Duyệt đổi hàng | Sản phẩm: ${productName} | SL: ${productQty}`;
    } else {
        adminNote = `Duyệt hoàn tiền & đổi hàng | Số tiền: ${amount} VNĐ | Sản phẩm: ${productName} | SL: ${productQty}`;
    }
    if (note && note.trim()) {
        adminNote += ` | Ghi chú: ${note}`;
    }
    const res = await fetch(`/api/return-request/${returnId}/approve?adminNote=${encodeURIComponent(adminNote)}`, { method: 'POST' });
    if (res.ok) {
        alert('Đã duyệt yêu cầu trả hàng!');
        loadRefundList();
        bootstrap.Modal.getInstance(document.getElementById('approveRefundModal')).hide();
    } else {
        alert('Duyệt thất bại!');
    }
};
// --- Modal từ chối ---
window.openRejectModal = function(returnId) {
    document.getElementById('rejectRefundModal').dataset.returnId = returnId;
    new bootstrap.Modal(document.getElementById('rejectRefundModal')).show();
}
document.querySelector('#rejectRefundModal .btn-danger').onclick = async function() {
    const returnId = document.getElementById('rejectRefundModal').dataset.returnId;
    const reason = document.querySelector('#rejectRefundModal select').value;
    const res = await fetch(`/api/return-request/${returnId}/reject?reason=${encodeURIComponent(reason)}`, { method: 'POST' });
    if (res.ok) {
        alert('Đã từ chối yêu cầu trả hàng!');
        loadRefundList();
        bootstrap.Modal.getInstance(document.getElementById('rejectRefundModal')).hide();
    } else {
        alert('Từ chối thất bại!');
    }
};
// --- Modal hoàn tất ---
window.openCompleteModal = async function(returnId) {
    // Lấy loại yêu cầu để ẩn/hiện input số tiền hoàn và sản phẩm đổi
    const res = await fetch(`/api/return-request/${returnId}/full`);
    const refund = await res.json();
    document.getElementById('completeRefundModal').dataset.returnId = returnId;
    const inputAmount = document.querySelector('#completeRefundModal input[type="number"]');
    const exchangeGroup = document.getElementById('exchangeProductGroup');
    const refundGroup = document.getElementById('refundAmountGroup');
    const typeSelect = document.getElementById('completeTypeSelect');
    // So sánh không phân biệt hoa/thường
    const type = (refund.returnType || '').toUpperCase();
    if (type === 'EXCHANGE') {
        typeSelect.value = 'exchange';
        refundGroup.style.display = 'none';
        exchangeGroup.style.display = '';
    } else if (type === 'REFUND') {
        typeSelect.value = 'refund';
        refundGroup.style.display = '';
        exchangeGroup.style.display = 'none';
    } else {
        typeSelect.value = 'both';
        refundGroup.style.display = '';
        exchangeGroup.style.display = '';
    }
    // Khi admin đổi select
    typeSelect.onchange = function() {
        if (this.value === 'exchange') {
            refundGroup.style.display = 'none';
            exchangeGroup.style.display = '';
        } else if (this.value === 'refund') {
            refundGroup.style.display = '';
            exchangeGroup.style.display = 'none';
        } else {
            refundGroup.style.display = '';
            exchangeGroup.style.display = '';
        }
    };
    new bootstrap.Modal(document.getElementById('completeRefundModal')).show();
}
document.querySelector('#completeRefundModal .btn-success').onclick = async function() {
    const returnId = document.getElementById('completeRefundModal').dataset.returnId;
    const type = document.getElementById('completeTypeSelect').value;
    const amount = document.querySelector('#refundAmountGroup input[type="number"]').value;
    const note = document.querySelector('#completeRefundModal textarea').value;
    const productName = document.querySelector('#exchangeProductGroup input[type="text"]').value;
    const productQty = document.querySelector('#exchangeProductGroup input[type="number"]').value;
    let adminNote = '';
    if (type === 'refund') {
        adminNote = `Hoàn tiền | Số tiền: ${amount} VNĐ`;
    } else if (type === 'exchange') {
        adminNote = `Đổi hàng | Sản phẩm: ${productName} | SL: ${productQty}`;
    } else {
        adminNote = `Hoàn tiền & Đổi hàng | Số tiền: ${amount} VNĐ | Sản phẩm: ${productName} | SL: ${productQty}`;
    }
    if (note && note.trim()) {
        adminNote += ` | Ghi chú: ${note}`;
    }
    const res = await fetch(`/api/return-request/${returnId}/complete?adminNote=${encodeURIComponent(adminNote)}`, { method: 'POST' });
    if (res.ok) {
        alert('Đã hoàn tất yêu cầu trả hàng!');
        loadRefundList();
        bootstrap.Modal.getInstance(document.getElementById('completeRefundModal')).hide();
    } else {
        alert('Hoàn tất thất bại!');
    }
};
// --- Fill số liệu động cho dashboard ---
async function fetchRefundStats() {
    try {
        const res = await fetch('/api/return-request/stats');
        const stats = await res.json();
        const row = document.getElementById('refund-stats-row');
        row.innerHTML = `
            <div class="col">
                <div class="stats-card border rounded p-2">
                    <i class="fas fa-clock fs-4"></i>
                    <h3 class="text-danger m-0">${stats.pending || 0}</h3>
                    <p>Đang chờ xử lý</p>
                </div>
            </div>
            <div class="col">
                <div class="stats-card border rounded p-2">
                    <i class="fas fa-check-circle fs-4"></i>
                    <h3 class="text-danger m-0">${stats.approved || 0}</h3>
                    <p>Đã duyệt</p>
                </div>
            </div>
            <div class="col">
                <div class="stats-card border rounded p-2">
                    <i class="fas fa-times-circle fs-4"></i>
                    <h3 class="text-danger m-0">${stats.rejected || 0}</h3>
                    <p>Từ chối</p>
                </div>
            </div>
            <div class="col">
                <div class="stats-card border rounded p-2">
                    <i class="fas fa-check-double fs-4"></i>
                    <h3 class="text-danger m-0">${stats.completed || 0}</h3>
                    <p>Hoàn tất</p>
                </div>
            </div>
        `;
    } catch (e) {
        // fallback nếu lỗi
        document.getElementById('refund-stats-row').innerHTML = '<div class="col">Không thể tải số liệu</div>';
    }
}
// --- Filter, tìm kiếm, phân trang danh sách trả hàng ---
let refundPage = 0;
let refundSize = 10;
let refundStatus = '';
let refundKeyword = '';

async function loadRefundListWithFilter(page = 0) {
    refundPage = page;
    const status = refundStatus;
    const keyword = refundKeyword;
    const url = `/api/return-request/search?status=${encodeURIComponent(status)}&keyword=${encodeURIComponent(keyword)}&page=${page}&size=${refundSize}`;
    const res = await fetch(url);
    const data = await res.json();
    renderRefundList(data.content || []);
    renderRefundPagination(data);
}

function renderRefundPagination(pageData) {
    const pag = document.getElementById('refund-pagination');
    pag.innerHTML = '';
    if (!pageData.totalPages || pageData.totalPages <= 1) return;
    // Prev
    pag.innerHTML += `<li class="page-item${pageData.first ? ' disabled' : ''}"><a class="page-link" href="#" data-page="${pageData.number - 1}"><i class="fas fa-chevron-left"></i></a></li>`;
    // Pages
    for (let i = 0; i < pageData.totalPages; i++) {
        pag.innerHTML += `<li class="page-item${i === pageData.number ? ' active' : ''}"><a class="page-link" href="#" data-page="${i}">${i + 1}</a></li>`;
    }
    // Next
    pag.innerHTML += `<li class="page-item${pageData.last ? ' disabled' : ''}"><a class="page-link" href="#" data-page="${pageData.number + 1}"><i class="fas fa-chevron-right"></i></a></li>`;
    // Gắn sự kiện
    pag.querySelectorAll('a.page-link').forEach(a => {
        a.onclick = (e) => {
            e.preventDefault();
            const p = parseInt(a.getAttribute('data-page'));
            if (!isNaN(p) && p >= 0 && p < pageData.totalPages && p !== pageData.number) {
                loadRefundListWithFilter(p);
            }
        };
    });
}

document.getElementById('refund-status-filter').onchange = function() {
    refundStatus = this.value;
    loadRefundListWithFilter(0);
};
document.getElementById('refund-search-btn').onclick = function() {
    refundKeyword = document.getElementById('refund-search-input').value;
    loadRefundListWithFilter(0);
};
document.getElementById('refund-search-input').onkeydown = function(e) {
    if (e.key === 'Enter') {
        refundKeyword = this.value;
        loadRefundListWithFilter(0);
    }
};
// --- Khởi động ---
document.addEventListener('DOMContentLoaded', () => {
    fetchRefundStats();
    loadRefundListWithFilter(0);
}); 