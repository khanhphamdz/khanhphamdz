document.addEventListener('DOMContentLoaded', function() {
    function toDateTimeString(dateStr) {
        if (!dateStr) return null;
        if (dateStr.includes('T')) return dateStr;
        return dateStr + 'T00:00:00';
    }
    function showError(message) { alert('Lỗi: ' + message); }
    function showSuccess(message) { alert(message); }

    // --- STATUS LOGIC ---
    function getPromotionStatus(promotion) {
        const now = new Date();
        const startDate = new Date(promotion.startDate);
        const endDate = new Date(promotion.endDate);
        endDate.setHours(23, 59, 59, 999); // Set to end of day

        if (now < startDate) {
            return { text: 'Sắp diễn ra', className: 'status-scheduled' };
        } else if (now > endDate) {
            return { text: 'Đã kết thúc', className: 'status-ended' };
        } else {
            return { text: 'Đang hoạt động', className: 'status-active' };
        }
    }

    // --- CRUD PROMOTION ---
    let currentPromotionPage = 0;
    let currentPromotionKeyword = '';

    function loadPromotions(keyword = '', page = 0, size = 10) {
        currentPromotionPage = page;
        currentPromotionKeyword = keyword;
        let url = `/api/admin/promotions?page=${page}&size=${size}`;
        if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
        fetch(url)
            .then(res => res.json())
            .then(data => {
                const tbody = document.querySelector('#programs .table tbody');
                tbody.innerHTML = '';
                (data.content || []).forEach((promotion, idx) => {
                    const status = getPromotionStatus(promotion);
                    tbody.innerHTML += `
                        <tr>
                            <td>#${promotion.promotionId}</td>
                            <td>${promotion.name}</td>
                            <td>${promotion.type === 'PERCENTAGE' ? 'Phần trăm' : 'Giảm giá cố định'}</td>
                            <td>${promotion.type === 'PERCENTAGE' ? promotion.discountValue + '%' : promotion.discountValue + 'đ'}</td>
                            <td>${promotion.startDate ? promotion.startDate.substring(0,10) : ''} - ${promotion.endDate ? promotion.endDate.substring(0,10) : ''}</td>
                            <td><span class="status-badge ${status.className}">${status.text}</span></td>
                            <td class="action-buttons">
                                <button class="btn btn-warning" data-idx="${idx}" title="Chỉnh sửa"><i class="fas fa-edit"></i></button>
                                <button class="btn btn-danger" data-idx="${idx}" title="Xóa"><i class="fas fa-trash"></i></button>
                            </td>
                        </tr>
                    `;
                });
                attachPromotionActions(data.content || []);
                renderPromotionPagination(data, keyword, size);
            });
    }

    // Thêm hàm render phân trang động cho promotion
    function renderPromotionPagination(data, keyword, size) {
        const pag = document.getElementById('promotion-pagination');
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
            if (!data.first) loadPromotions(keyword, data.number - 1, size);
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
                if (i !== data.number) loadPromotions(keyword, i, size);
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
            if (!data.last) loadPromotions(keyword, data.number + 1, size);
        };
        nextLi.appendChild(nextA);
        pag.appendChild(nextLi);
    }

    function attachPromotionActions(data) {
        document.querySelectorAll('#programs .btn-warning').forEach(btn => {
            btn.onclick = function() {
                const idx = btn.getAttribute('data-idx');
                openPromotionModal(true, data[idx]);
            };
        });
        document.querySelectorAll('#programs .btn-danger').forEach(btn => {
            btn.onclick = function() {
                const idx = btn.getAttribute('data-idx');
                if (confirm('Bạn có chắc muốn xóa?')) {
                    fetch(`/api/admin/promotions/${data[idx].promotionId}`, { method: 'DELETE' })
                        .then(() => { showSuccess('Đã xóa!'); loadPromotions(currentPromotionKeyword, currentPromotionPage); })
                        .catch(() => showError('Lỗi khi xóa!'));
                }
            };
        });
    }

    function openPromotionModal(edit = false, data = null) {
        const modalEl = document.getElementById('addProgramModal');
        const modal = new bootstrap.Modal(modalEl);
        const form = document.getElementById('promotionForm');
        form.reset();
        form.dataset.edit = edit ? 'true' : 'false';
        if (edit && data) {
            form.promotionId.value = data.promotionId;
            form.name.value = data.name;
            form.type.value = data.type.toLowerCase();
            form.value.value = data.discountValue;
            form.startDate.value = data.startDate ? data.startDate.substring(0,10) : '';
            form.endDate.value = data.endDate ? data.endDate.substring(0,10) : '';
            form.description.value = data.description || '';
            form.applyType.value = data.applyType ? data.applyType : (data.productIds && data.productIds.length > 0 ? 'single' : (data.categoryIds && data.categoryIds.length > 0 ? 'category' : 'all'));
            document.getElementById('applyType').dispatchEvent(new Event('change'));
            if (data.variantIds && data.variantIds.length > 0) {
                document.getElementById('variantIdsInput').value = data.variantIds.join(',');
                if (typeof allProducts !== 'undefined' && allProducts.length > 0) {
                    const selected = allProducts.filter(p => data.variantIds.includes(p.variantId));
                    document.getElementById('selectedProducts').innerHTML = selected.map(p => `<span>${p.name}</span>`).join(', ');
                }
            }
            if (data.categoryIds && data.categoryIds.length > 0) {
                document.getElementById('categoryIdsInput').value = data.categoryIds.join(',');
                if (typeof allCategories !== 'undefined' && allCategories.length > 0) {
                    const selected = allCategories.filter(c => data.categoryIds.includes(c.categoryId));
                    document.getElementById('selectedCategories').innerHTML = selected.map(c => `<span>${c.name}</span>`).join(', ');
                }
            }
        } else {
            form.applyType.value = 'single';
            document.getElementById('applyType').dispatchEvent(new Event('change'));
        }
        modal.show();
    }

    // Form submit handler
    document.getElementById('promotionForm').onsubmit = async function(e) {
        e.preventDefault();
        const form = e.target;
        if (form.startDate.value >= form.endDate.value) {
            showError('Thời gian kết thúc phải sau thời gian bắt đầu');
            return;
        }
        if (form.type.value === 'PERCENTAGE') {
            const value = parseFloat(form.value.value);
            if (value > 99) { showError('Giá trị giảm giá phần trăm không được vượt quá 99%.'); return; }
            if (value < 1) { showError('Giá trị giảm giá phần trăm phải lớn hơn 0.'); return; }
        }
        if (form.type.value === 'FIXED') {
            const value = parseFloat(form.value.value);
            if (value <= 0) { showError('Giá trị giảm giá phải lớn hơn 0.'); return; }
        }

        const data = {
            promotionId: form.promotionId.value || null,
            name: form.name.value,
            type: form.type.value,
            discountValue: parseFloat(form.value.value),
            startDate: toDateTimeString(form.startDate.value),
            endDate: toDateTimeString(form.endDate.value),
            description: form.description.value,
            isActive: true,
            applyType: form.applyType.value,
            variantIds: [],
            categoryIds: []
        };

        if (data.applyType === 'single') {
            const variantIds = document.getElementById('variantIdsInput').value;
            if (!variantIds) {
                showError('Vui lòng chọn ít nhất một biến thể sản phẩm');
                return;
            }
            data.variantIds = variantIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
        } else if (data.applyType === 'category') {
            const categoryIds = document.getElementById('categoryIdsInput').value;
            if (!categoryIds) {
                showError('Vui lòng chọn ít nhất một danh mục');
                return;
            }
            data.categoryIds = categoryIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
        }

        const method = form.dataset.edit === 'true' ? 'PUT' : 'POST';
        const url = method === 'POST' ? '/api/admin/promotions' : `/api/admin/promotions/${data.promotionId}`;
        
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
            showSuccess('Lưu chương trình thành công!');
            window.location.reload();
        } catch (err) {
            showError('Có lỗi khi lưu chương trình! ' + err.message);
        }
    };

    // Event Handlers
    document.querySelector('[data-bs-target="#addProgramModal"]').onclick = () => openPromotionModal(false);
    document.getElementById('programSearchBtn').onclick = () => {
        loadPromotions(document.getElementById('programSearchInput').value);
    };
    document.getElementById('programSearchInput').onkeyup = function(e) {
        if (e.key === 'Enter') {
            loadPromotions(this.value);
        }
    };

    // Initialize
    loadPromotions();
});