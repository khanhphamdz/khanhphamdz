document.addEventListener('DOMContentLoaded', function() {
    function toDateTimeString(dateStr) {
        if (!dateStr) return null;
        return dateStr + 'T00:00:00';
    }
    function showError(message) { alert('Lỗi: ' + message); }
    function showSuccess(message) { alert(message); }

    function getCouponStatus(coupon) {
        const now = new Date();
        if (coupon.endDate) {
            const endDate = new Date(coupon.endDate);
            endDate.setHours(23, 59, 59, 999);
            if (now > endDate) {
                return { text: 'Hết hạn', className: 'status-ended' };
            }
        }
        if (coupon.maxUsage && coupon.usageCount >= coupon.maxUsage) {
            return { text: 'Hết mã', className: 'status-ended' };
        }
        if (coupon.startDate) {
            const startDate = new Date(coupon.startDate);
            if (now < startDate) {
                return { text: 'Sắp diễn ra', className: 'status-scheduled' };
            }
        }
        return { text: 'Đang hoạt động', className: 'status-active' };
    }

    // --- CRUD COUPON ---
    let couponStatusFilter = '';
    let currentCouponPage = 0;
    let currentCouponKeyword = '';
    let currentCouponSize = 10;

    // Gắn sự kiện cho dropdown filter trạng thái
    const statusSelect = document.querySelector('#vouchers select.form-select');
    if (statusSelect) {
        statusSelect.addEventListener('change', function() {
            couponStatusFilter = this.value;
            loadCoupons(document.getElementById('voucherSearchInput').value);
        });
    }

    function loadCoupons(keyword = '', page = 0, size = 10) {
        currentCouponPage = page;
        currentCouponKeyword = keyword;
        currentCouponSize = size;
        let url = `/api/admin/coupons?page=${page}&size=${size}`;
        if (keyword) {
            url += `&keyword=${encodeURIComponent(keyword)}`;
        }
        fetch(url)
            .then(res => res.json())
            .then(data => {
                const tbody = document.querySelector('#vouchers .table tbody');
                tbody.innerHTML = '';
                (data.content || data).filter(coupon => {
                    if (!couponStatusFilter || couponStatusFilter === '') return true;
                    const now = new Date();
                    const start = new Date(coupon.startDate);
                    const end = new Date(coupon.endDate); end.setHours(23,59,59,999);
                    if (couponStatusFilter === 'active') {
                        return now >= start && now <= end && (!coupon.maxUsage || coupon.usageCount < coupon.maxUsage);
                    }
                    if (couponStatusFilter === 'used') {
                        return coupon.maxUsage && coupon.usageCount >= coupon.maxUsage;
                    }
                    if (couponStatusFilter === 'expired') {
                        return now > end;
                    }
                    return true;
                }).forEach((coupon, idx) => {
                    const status = getCouponStatus(coupon);
                    const usedInfo = coupon.maxUsage ? `<span class='${coupon.usageCount >= coupon.maxUsage ? "text-danger fw-bold" : ""}'>${coupon.usageCount || 0} / ${coupon.maxUsage}</span>` : '-';
                    tbody.innerHTML += `
                        <tr>
                            <td>${coupon.code}</td>
                            <td>${coupon.discountValue && coupon.discountValue <= 100 ? 'Phần trăm' : 'Giảm giá cố định'}</td>
                            <td>${coupon.discountValue && coupon.discountValue <= 100 ? coupon.discountValue + '%' : coupon.discountValue + ' VNĐ'}</td>
                            <td>${usedInfo}</td>
                            <td>${coupon.minOrderValue ? 'Đơn hàng từ ' + coupon.minOrderValue + ' VNĐ' : ''}</td>
                            <td>${coupon.startDate ? coupon.startDate.substring(0,10) : ''}</td>
                            <td>${coupon.endDate ? coupon.endDate.substring(0,10) : ''}</td>
                            <td><span class="status-badge ${status.className}">${status.text}</span></td>
                            <td class="action-buttons">
                                <button class="btn btn-warning btn-updateCoupon" data-idx="${idx}" title="Chỉnh sửa"><i class="fas fa-edit"></i></button>
                                <button class="btn btn-danger btn-deleteCoupon" data-idx="${idx}" title="Xóa"><i class="fas fa-trash"></i></button>
                            </td>
                        </tr>
                    `;
                });
                attachCouponActions(data.content || data);
                renderCouponPagination(data, keyword, size);
            });
    }

    function renderCouponPagination(data, keyword, size) {
        const pag = document.getElementById('voucher-pagination');
        if (!pag) return;
        pag.innerHTML = '';
        if (!data.totalPages || data.totalPages <= 1) return;
        // Previous button
        const prevLi = document.createElement('li');
        prevLi.className = 'page-item' + (data.first ? ' disabled' : '');
        const prevA = document.createElement('a');
        prevA.className = 'page-link';
        prevA.href = '#';
        prevA.innerHTML = '<i class="fas fa-chevron-left"></i>';
        prevA.onclick = function(e) {
            e.preventDefault();
            if (!data.first) loadCoupons(keyword, data.number - 1, size);
        };
        prevLi.appendChild(prevA);
        pag.appendChild(prevLi);
        // Page numbers
        for (let i = 0; i < data.totalPages; i++) {
            const li = document.createElement('li');
            li.className = 'page-item' + (i === data.number ? ' active' : '');
            const a = document.createElement('a');
            a.className = 'page-link';
            a.href = '#';
            a.textContent = (i + 1);
            a.onclick = function(e) {
                e.preventDefault();
                if (i !== data.number) loadCoupons(keyword, i, size);
            };
            li.appendChild(a);
            pag.appendChild(li);
        }
        // Next button
        const nextLi = document.createElement('li');
        nextLi.className = 'page-item' + (data.last ? ' disabled' : '');
        const nextA = document.createElement('a');
        nextA.className = 'page-link';
        nextA.href = '#';
        nextA.innerHTML = '<i class="fas fa-chevron-right"></i>';
        nextA.onclick = function(e) {
            e.preventDefault();
            if (!data.last) loadCoupons(keyword, data.number + 1, size);
        };
        nextLi.appendChild(nextA);
        pag.appendChild(nextLi);
    }

    function attachCouponActions(data) {
        document.querySelectorAll('.btn-updateCoupon').forEach(btn => {
            btn.onclick = function() {
                const idx = btn.getAttribute('data-idx');
                openCouponModal(true, data[idx]);
            };
        });
        document.querySelectorAll('.btn-deleteCoupon').forEach(btn => {
            btn.onclick = function() {
                const idx = btn.getAttribute('data-idx');
                if (confirm('Bạn có chắc muốn xóa mã giảm giá này?')) {
                    fetch(`/api/admin/coupons/${data[idx].id}`, { method: 'DELETE' })
                        .then(() => { showSuccess('Đã xóa!'); loadCoupons(currentCouponKeyword, currentCouponPage); })
                        .catch(() => showError('Lỗi khi xóa!'));
                }
            };
        });
    }

    function openCouponModal(edit = false, data = null) {
        const modalEl = document.getElementById('addVoucherModal');
        const modal = new bootstrap.Modal(modalEl);
        const form = document.getElementById('couponForm');
        form.reset();
        form.couponId.value = '';
        form.type.value = 'PERCENTAGE';
        form.applyToCustomer.value = 'all';
        form.dataset.edit = edit ? 'true' : 'false';
        // Xử lý disable các trường nếu đã có usage
        if (edit && data) {
            form.couponId.value = data.couponId;
            form.code.value = data.code;
            form.type.value = data.type ? data.type.toUpperCase() : (data.discountValue && data.discountValue <= 100 ? 'PERCENTAGE' : 'FIXED');
            form.value.value = data.discountValue;
            form.maxUsage.value = data.maxUsage || '';
            form.minOrderValue.value = data.minOrderValue || '';
            form.startDate.value = data.startDate ? data.startDate.substring(0,10) : '';
            form.endDate.value = data.endDate ? data.endDate.substring(0,10) : '';
            form.description.value = data.description || '';
            form.applyToCustomer.value = data.applyToCustomer || 'all';
            // Nếu đã có usage > 0 thì disable code, type, value
            if (data.usageCount && data.usageCount > 0) {
                form.code.disabled = true;
                form.type.disabled = true;
                form.value.disabled = true;
                form.dataset.limited = 'true';
            } else {
                form.code.disabled = false;
                form.type.disabled = false;
                form.value.disabled = false;
                form.dataset.limited = 'false';
            }
        } else {
            form.code.disabled = false;
            form.type.disabled = false;
            form.value.disabled = false;
            form.dataset.limited = 'false';
        }
        modal.show();
    }

    // Event Listeners
    document.querySelector('[data-bs-target="#addVoucherModal"]').onclick = function() {
        openCouponModal(false);
    };

    document.getElementById('couponForm').onsubmit = async function(e) {
        e.preventDefault();
        const form = e.target;
        
        // Validate dates
        if (form.startDate.value && form.endDate.value && form.startDate.value >= form.endDate.value) {
            showError('Thời gian kết thúc phải sau thời gian bắt đầu');
            return;
        }

        // Validate discount value
        if (form.type.value === 'PERCENTAGE') {
            const value = parseFloat(form.value.value);
            if (value > 100) { showError('Giảm giá phần trăm không được vượt quá 100%'); return; }
            if (value <= 0) { showError('Giảm giá phần trăm phải lớn hơn 0%'); return; }
        }
        if (form.type.value === 'FIXED') {
            const value = parseFloat(form.value.value);
            if (value <= 0) { showError('Giá trị giảm giá phải lớn hơn 0'); return; }
        }

        // Validate min order value
        if (form.minOrderValue.value) {
            const minOrder = parseFloat(form.minOrderValue.value);
            if (minOrder < 0) { showError('Giá trị đơn hàng tối thiểu không được âm'); return; }
        }

        const typeValue = form.type.value || 'PERCENTAGE';
        // Nếu đang chỉnh sửa và bị giới hạn, chỉ gửi các trường cơ bản
        let data;
        if (form.dataset.edit === 'true' && form.dataset.limited === 'true') {
            data = {
                id: form.couponId.value && form.couponId.value !== 'undefined' ? form.couponId.value : null,
                startDate: toDateTimeString(form.startDate.value),
                endDate: toDateTimeString(form.endDate.value),
                maxUsage: form.maxUsage.value ? parseInt(form.maxUsage.value) : null,
                minOrderValue: form.minOrderValue.value ? parseFloat(form.minOrderValue.value) : null,
                description: form.description.value,
                applyToCustomer: form.applyToCustomer.value,
                isActive: true,
                usageCount: data && data.usageCount ? data.usageCount : 0 // fallback nếu có
            };
        } else {
            data = {
                id: form.couponId.value && form.couponId.value !== 'undefined' ? form.couponId.value : null,
                code: form.code.value,
                discountValue: parseFloat(form.value.value),
                startDate: toDateTimeString(form.startDate.value),
                endDate: toDateTimeString(form.endDate.value),
                maxUsage: form.maxUsage.value ? parseInt(form.maxUsage.value) : null,
                minOrderValue: form.minOrderValue.value ? parseFloat(form.minOrderValue.value) : null,
                description: form.description.value,
                type: typeValue,
                applyToCustomer: form.applyToCustomer.value,
                isActive: true,
                usageCount: 0
            };
        }

        const method = form.dataset.edit === 'true' && data.id ? 'PUT' : 'POST';
        const url = method === 'PUT' ? `/api/admin/coupons/${data.id}` : '/api/admin/coupons';
        
        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }
            await response.json();
            showSuccess('Lưu mã giảm giá thành công!');
            // Đóng modal và render lại danh sách
            const modalEl = document.getElementById('addVoucherModal');
            const modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) modal.hide();
            loadCoupons(currentCouponKeyword, currentCouponPage, currentCouponSize);
        } catch (err) {
            showError('Có lỗi khi lưu mã giảm giá! ' + err.message);
        }
    };

    // Search event handlers
    document.getElementById('voucherSearchBtn').onclick = () => {
        loadCoupons(document.getElementById('voucherSearchInput').value);
    };
    document.getElementById('voucherSearchInput').onkeyup = function(e) {
        if (e.key === 'Enter') {
            loadCoupons(this.value);
        }
    };

    // Thống kê coupon
    async function loadCouponStats() {
        try {
            const res = await fetch('/api/admin/coupons/stats');
            if (!res.ok) throw new Error('Lỗi khi lấy thống kê');
            const stats = await res.json();
            if (document.getElementById('stat-active-programs'))
                document.getElementById('stat-active-programs').textContent = stats.activePrograms ?? '0';
            if (document.getElementById('stat-active-coupons'))
                document.getElementById('stat-active-coupons').textContent = stats.activeCoupons ?? '0';
            if (document.getElementById('stat-total-coupons'))
                document.getElementById('stat-total-coupons').textContent = stats.totalCoupons ?? '0';
            if (document.getElementById('stat-usage-this-month'))
                document.getElementById('stat-usage-this-month').textContent = stats.usageThisMonth ?? '0';
        } catch (e) {
            // Có thể log lỗi hoặc giữ nguyên ...
        }
    }

    // Initialize
    loadCoupons();
    loadCouponStats();
});