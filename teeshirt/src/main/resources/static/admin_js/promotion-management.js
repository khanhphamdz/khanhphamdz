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

    // --- CRUD PROMOTION ---
    let currentPromotionPage = 0;
    let currentPromotionKeyword = '';

    function loadPromotions(keyword = '', page = 0, size = 10) {
        currentPromotionPage = page;
        currentPromotionKeyword = keyword;
        let url = `/api/admin/promotion?page=${page}&size=${size}`;
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
                if (!data[idx] || !data[idx].promotionId) {
                    alert('Không tìm thấy dữ liệu chương trình giảm giá!');
                    console.error('Không tìm thấy promotionId cho idx', idx, data[idx]);
                    return;
                }
                const promotionId = data[idx].promotionId;
                fetch(`/api/admin/promotion/${promotionId}`)
                    .then(res => {
                        if (!res.ok) throw new Error('Không lấy được chi tiết chương trình!');
                        return res.json();
                    })
                    .then(fullData => {
                        openPromotionModal(true, fullData);
                    })
                    .catch(err => {
                        alert('Lỗi khi lấy chi tiết chương trình: ' + err.message);
                        console.error('Lỗi khi lấy chi tiết chương trình:', err);
                    });
            };
        });
        document.querySelectorAll('#programs .btn-danger').forEach(btn => {
            btn.onclick = function() {
                const idx = btn.getAttribute('data-idx');
                if (confirm('Bạn có chắc muốn xóa?')) {
                    fetch(`/api/admin/promotion/${data[idx].promotionId}`, { method: 'DELETE' })
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
            form.promotionId.value = data.promotionId || '';
            form.name.value = data.name || '';
            form.type.value = data.type ? data.type.toLowerCase() : '';
            form.value.value = data.discountValue || '';
            form.startDate.value = data.startDate ? data.startDate.substring(0,10) : '';
            form.endDate.value = data.endDate ? data.endDate.substring(0,10) : '';
            form.description.value = data.description || '';
            // Xử lý applyType và variantIds/categoryIds
            if (data.applyType === 'single' && data.variantIds && data.variantIds.length > 0) {
                form.applyType.value = 'single';
                document.getElementById('variantIdsInput').value = data.variantIds.join(',');
                function fillBadges() {
                    let badges = [];
                    allProducts.forEach(product => {
                        (product.variants || []).forEach(variant => {
                            if (data.variantIds.includes(variant.variantId)) {
                                badges.push(`<span class='badge bg-primary me-1 mb-1'>${product.name} - ${variant.colorName || ''} / ${variant.sizeName || ''}</span>`);
                            }
                        });
                    });
                    document.getElementById('selectedProducts').innerHTML = badges.length > 0 ? badges.join(' ') : '<span class="text-muted">Chưa chọn biến thể nào</span>';
                }
                if (allProducts && allProducts.length > 0) {
                    fillBadges();
                } else {
                    fetch('/api/product/admin/products-with-variants')
                        .then(res => res.json())
                        .then(products => {
                            allProducts = products;
                            fillBadges();
                        });
                }
                document.getElementById('applyType').dispatchEvent(new Event('change'));
            } else if (data.applyType === 'category' && data.categoryIds && data.categoryIds.length > 0) {
                form.applyType.value = 'category';
                document.getElementById('categoryIdsInput').value = data.categoryIds.join(',');
                document.getElementById('applyType').dispatchEvent(new Event('change'));
            } else if (data.applyType === 'all') {
                form.applyType.value = 'all';
                document.getElementById('variantIdsInput').value = '';
                // Hiển thị badge cho tất cả sản phẩm/biến thể
                function fillAllBadges() {
                    let badges = [];
                    allProducts.forEach(product => {
                        (product.variants || []).forEach(variant => {
                            badges.push(`<span class='badge bg-primary me-1 mb-1'>${product.name} - ${variant.colorName || ''} / ${variant.sizeName || ''}</span>`);
                        });
                    });
                    document.getElementById('selectedProducts').innerHTML = badges.length > 0 ? badges.join(' ') : '<span class="text-muted">Không có sản phẩm nào</span>';
                }
                if (allProducts && allProducts.length > 0) {
                    fillAllBadges();
                } else {
                    fetch('/api/product/admin/products-with-variants')
                        .then(res => res.json())
                        .then(products => {
                            allProducts = products;
                            fillAllBadges();
                        });
                }
                // Đảm bảo ẩn UI chọn sản phẩm khi là all
                document.getElementById('productSelectGroup').style.display = 'none';
                document.getElementById('categorySelectGroup').style.display = 'none';
                document.getElementById('applyType').dispatchEvent(new Event('change'));
            } else {
                form.applyType.value = 'all';
                document.getElementById('applyType').dispatchEvent(new Event('change'));
            }
        } else {
            // Khi thêm mới, đảm bảo các trường đều rỗng/mặc định
            form.promotionId.value = '';
            form.name.value = '';
            form.type.value = '';
            form.value.value = '';
            form.startDate.value = '';
            form.endDate.value = '';
            form.description.value = '';
            form.applyType.value = 'all';
            document.getElementById('variantIdsInput').value = '';
            document.getElementById('categoryIdsInput').value = '';
            document.getElementById('selectedProducts').innerHTML = '';
            document.getElementById('selectedCategories').innerHTML = '';
            document.getElementById('applyType').dispatchEvent(new Event('change'));
        }
        modal.show();
    }
    document.querySelector('[data-bs-target="#addProgramModal"]').onclick = function() {
        openPromotionModal(false);
    };

    // --- Modal chọn sản phẩm ---
    let allProducts = [];
    function openProductSelectModal(selectedVariantIds = []) {
        const modal = new bootstrap.Modal(document.getElementById('productSelectModal'));
        const tbody = document.getElementById('productSelectTableBody');
        tbody.innerHTML = '<tr><td colspan="4">Đang tải...</td></tr>';
        fetch('/api/product/admin/products-with-variants')
            .then(res => res.json())
            .then(products => {
                allProducts = products;
                renderProductSelectTable(selectedVariantIds, '');
                // Gắn sự kiện filter
                document.getElementById('productSearchInput').oninput = function() {
                    renderProductSelectTable(selectedVariantIds, this.value);
                };
                // Khi xác nhận chọn sản phẩm
                document.getElementById('confirmProductSelect').onclick = function() {
                    // Lấy lại danh sách variant đã tick
                    const checked = Array.from(document.querySelectorAll('.variant-checkbox:checked')).map(cb => parseInt(cb.value));
                    document.getElementById('variantIdsInput').value = checked.join(',');
                    // Hiển thị badge tên biến thể đã chọn
                    let badges = [];
                    allProducts.forEach(product => {
                        (product.variants || []).forEach(variant => {
                            if (checked.includes(variant.variantId)) {
                                badges.push(`<span class='badge bg-primary me-1 mb-1'>${product.name} - ${variant.colorName || ''} / ${variant.sizeName || ''}</span>`);
                            }
                        });
                    });
                    document.getElementById('selectedProducts').innerHTML = badges.length > 0 ? badges.join(' ') : '<span class="text-muted">Chưa chọn biến thể nào</span>';
                    bootstrap.Modal.getInstance(document.getElementById('productSelectModal')).hide();
                };
            });
        modal.show();
    }
    function renderProductSelectTable(selectedVariantIds, filterText) {
        const tbody = document.getElementById('productSelectTableBody');
        tbody.innerHTML = '';
        const filter = (filterText || '').toLowerCase();
        allProducts.forEach(product => {
            // Lọc theo tên sản phẩm hoặc tên biến thể
            const productMatch = product.name.toLowerCase().includes(filter);
            const filteredVariants = (product.variants || []).filter(variant => {
                return (
                    (variant.colorName && variant.colorName.toLowerCase().includes(filter)) ||
                    (variant.sizeName && variant.sizeName.toLowerCase().includes(filter)) ||
                    (product.name.toLowerCase().includes(filter))
                );
            });
            if (!productMatch && filteredVariants.length === 0 && filter) return;
            const allVariantIds = (product.variants || []).map(v => v.variantId);
            const allChecked = allVariantIds.length > 0 && allVariantIds.every(id => selectedVariantIds.includes(id));
            tbody.innerHTML += `
                <tr data-product-id="${product.productId}">
                    <td><input type="checkbox" class="product-checkbox" value="${product.productId}" ${allChecked ? 'checked' : ''}></td>
                    <td>${product.productId}</td>
                    <td><span class="product-name" data-bs-toggle="collapse" data-bs-target="#variants-${product.productId}" style="cursor:pointer;">${product.name}</span></td>
                    <td>
                        <button class="btn btn-link btn-sm" type="button" data-bs-toggle="collapse" data-bs-target="#variants-${product.productId}">Xem biến thể</button>
                    </td>
                </tr>
                <tr class="collapse" id="variants-${product.productId}">
                    <td></td>
                    <td colspan="3">
                        ${(filteredVariants.length > 0 ? filteredVariants : product.variants || []).map(variant => `
                            <label class="me-3">
                                <input type="checkbox" class="variant-checkbox" data-product-id="${product.productId}" value="${variant.variantId}" ${selectedVariantIds.includes(variant.variantId) ? 'checked' : ''}>
                                ${variant.colorName || ''} / ${variant.sizeName || ''}
                            </label>
                        `).join(' ')}
                    </td>
                </tr>
            `;
        });
        // Tick sản phẩm => tick hết variant
        tbody.querySelectorAll('.product-checkbox').forEach(cb => {
            cb.addEventListener('change', function() {
                const productId = cb.value;
                const variantCbs = tbody.querySelectorAll(`.variant-checkbox[data-product-id="${productId}"]`);
                variantCbs.forEach(vcb => vcb.checked = cb.checked);
            });
        });
        // Tick variant => nếu tick hết thì tick sản phẩm cha
        tbody.querySelectorAll('.variant-checkbox').forEach(vcb => {
            vcb.addEventListener('change', function() {
                const productId = vcb.dataset.productId;
                const all = tbody.querySelectorAll(`.variant-checkbox[data-product-id="${productId}"]`);
                const allChecked = Array.from(all).every(cb => cb.checked);
                const productCb = tbody.querySelector(`.product-checkbox[value="${productId}"]`);
                if (productCb) productCb.checked = allChecked;
            });
        });
    }
    document.getElementById('openProductSelectModal').onclick = function() {
        // Lấy danh sách variantIds đã chọn (nếu đang sửa thì lấy từ input, nếu đang sửa chương trình thì lấy từ data)
        let selected = [];
        // Nếu đang sửa chương trình và variantIds đã có trong input, ưu tiên lấy từ input (đã fill khi mở modal)
        const inputVal = document.getElementById('variantIdsInput').value;
        if (inputVal) {
            selected = inputVal.split(',').map(id => parseInt(id)).filter(id => !isNaN(id));
        }
        openProductSelectModal(selected);
    };
    document.getElementById('confirmProductSelect').onclick = function() {
        const checked = Array.from(document.querySelectorAll('.variant-checkbox:checked')).map(cb => parseInt(cb.value));
        document.getElementById('variantIdsInput').value = checked.join(',');
        // Hiển thị badge tên biến thể đã chọn (tên sản phẩm, màu, size)
        let badges = [];
        allProducts.forEach(product => {
            (product.variants || []).forEach(variant => {
                if (checked.includes(variant.variantId)) {
                    badges.push(`<span class='badge bg-primary me-1 mb-1'>${product.name} - ${variant.colorName || ''} / ${variant.sizeName || ''}</span>`);
                }
            });
        });
        document.getElementById('selectedProducts').innerHTML = badges.length > 0 ? badges.join(' ') : '<span class="text-muted">Chưa chọn biến thể nào</span>';
        bootstrap.Modal.getInstance(document.getElementById('productSelectModal')).hide();
    };
    document.getElementById('applyType').onchange = function() {
        if (this.value === 'single') {
            document.getElementById('productSelectGroup').style.display = '';
        } else {
            document.getElementById('productSelectGroup').style.display = 'none';
            document.getElementById('variantIdsInput').value = '';
            document.getElementById('selectedProducts').innerHTML = '';
        }
    };

    // --- CHỌN DANH MỤC ÁP DỤNG KHUYẾN MÃI ---
    let allCategories = [];
    function openCategorySelectModal(selectedCategoryIds = []) {
        const modal = new bootstrap.Modal(document.getElementById('categorySelectModal'));
        const tbody = document.getElementById('categorySelectTableBody');
        tbody.innerHTML = '<tr><td colspan="3">Đang tải...</td></tr>';
        fetch('/api/counter-sale/products/categories')
            .then(res => res.json())
            .then(categories => {
                allCategories = categories;
                renderCategorySelectTable(selectedCategoryIds, '');
                document.getElementById('categorySearchInput').oninput = function() {
                    renderCategorySelectTable(selectedCategoryIds, this.value);
                };
            });
        modal.show();
    }
    function renderCategorySelectTable(selectedCategoryIds, filterText) {
        const tbody = document.getElementById('categorySelectTableBody');
        tbody.innerHTML = '';
        const filter = (filterText || '').toLowerCase();
        allCategories.forEach(category => {
            if (filter && !category.name.toLowerCase().includes(filter)) return;
            const checked = selectedCategoryIds.includes(category.categoryId) ? 'checked' : '';
            tbody.innerHTML += `
                <tr>
                    <td><input type="checkbox" class="category-checkbox" value="${category.categoryId}" ${checked}></td>
                    <td>${category.categoryId}</td>
                    <td>${category.name}</td>
                </tr>
            `;
        });
    }
    document.getElementById('openCategorySelectModal').onclick = function() {
        const selected = (document.getElementById('categoryIdsInput').value || '').split(',').map(id => parseInt(id)).filter(id => !isNaN(id));
        openCategorySelectModal(selected);
    };
    document.getElementById('confirmCategorySelect').onclick = function() {
        const checked = Array.from(document.querySelectorAll('.category-checkbox:checked')).map(cb => parseInt(cb.value));
        document.getElementById('categoryIdsInput').value = checked.join(',');
        let badges = [];
        allCategories.forEach(category => {
            if (checked.includes(category.categoryId)) {
                badges.push(`<span class='badge bg-success me-1 mb-1'>${category.name}</span>`);
            }
        });
        document.getElementById('selectedCategories').innerHTML = badges.length > 0 ? badges.join(' ') : '<span class="text-muted">Chưa chọn danh mục nào</span>';
        bootstrap.Modal.getInstance(document.getElementById('categorySelectModal')).hide();
    };

    // --- Sửa lại submit form ---
    const promotionForm = document.getElementById('promotionForm');
    promotionForm.onsubmit = async function(e) {
        e.preventDefault();
        const form = e.target;
        if (form.startDate.value >= form.endDate.value) {
            showError('Thời gian kết thúc phải sau thời gian bắt đầu');
            return;
        }
        // Validate giá trị giảm giá
        if (form.type.value === 'PERCENTAGE') {
            const value = parseFloat(form.value.value);
            if (value > 99) { showError('Giá trị giảm giá phần trăm không được vượt quá 99%.'); return; }
            if (value < 1) { showError('Giá trị giảm giá phần trăm phải lớn hơn 0.'); return; }
        }
        if (form.type.value === 'FIXED') {
            const value = parseFloat(form.value.value);
            if (value <= 0) { showError('Giá trị giảm giá phải lớn hơn 0.'); return; }
        }
        const applyType = form.applyType.value;
        let variantIds = [];
        let categoryIds = [];
        if (applyType === 'single') {
            variantIds = document.getElementById('variantIdsInput').value ? document.getElementById('variantIdsInput').value.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id)) : [];
            if (!variantIds.length) {
                showError('Vui lòng chọn ít nhất một biến thể sản phẩm để áp dụng khuyến mãi');
                return;
            }
        }
        if (applyType === 'category') {
            categoryIds = document.getElementById('categoryIdsInput').value ? document.getElementById('categoryIdsInput').value.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id)) : [];
            if (!categoryIds.length) {
                showError('Vui lòng chọn ít nhất một danh mục để áp dụng khuyến mãi');
                return;
            }
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
            applyType: applyType,
            variantIds: applyType === 'single' ? variantIds : [],
            categoryIds: applyType === 'category' ? categoryIds : []
        };
        const method = form.dataset.edit === 'true' ? 'PUT' : 'POST';
        const url = method === 'POST' ? '/api/admin/promotion' : `/api/admin/promotion/${data.promotionId}`;
        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
        .then(res => {
            if (!res.ok) return res.text().then(text => { throw new Error(text); });
            return res.json();
        })
        .then(() => {
            showSuccess('Lưu chương trình thành công!');
            window.location.reload();
        })
        .catch(err => showError('Có lỗi khi lưu chương trình! ' + err.message));
    };

    // --- CRUD COUPON ---
    // Thêm biến toàn cục lưu trạng thái filter
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
        let url = `/api/admin/coupon?page=${page}&size=${size}`;
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
                                <button class="btn btn-warning" data-idx="${idx}" title="Chỉnh sửa"><i class="fas fa-edit"></i></button>
                                <button class="btn btn-danger" data-idx="${idx}" title="Xóa"><i class="fas fa-trash"></i></button>
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
        document.querySelectorAll('#vouchers .btn-warning').forEach(btn => {
            btn.onclick = function() {
                const idx = btn.getAttribute('data-idx');
                openCouponModal(true, data[idx]);
            };
        });
        document.querySelectorAll('#vouchers .btn-danger').forEach(btn => {
            btn.onclick = function() {
                const idx = btn.getAttribute('data-idx');
                if (confirm('Bạn có chắc muốn xóa?')) {
                    fetch(`/api/admin/coupon/${data[idx].couponId}`, { method: 'DELETE' }) // Đổi endpoint đúng chuẩn REST
                        .then(() => { showSuccess('Đã xóa!'); window.location.reload(); })
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
        form.dataset.edit = edit ? 'true' : 'false';
        if (edit && data) {
            form.couponId.value = data.couponId;
            form.code.value = data.code;
            form.type.value = data.type ? data.type : (data.discountValue <= 100 ? 'percentage' : 'fixed');
            form.value.value = data.discountValue;
            form.minOrderValue.value = data.minOrderValue || '';
            form.startDate.value = data.startDate ? data.startDate.substring(0,10) : '';
            form.endDate.value = data.endDate ? data.endDate.substring(0,10) : '';
            form.quantity.value = data.maxUsage || '';
            form.description.value = data.description || '';
        }
        modal.show();
    }
    document.querySelector('[data-bs-target="#addVoucherModal"]').onclick = function() {
        openCouponModal(false);
    };
    document.getElementById('couponForm').onsubmit = function(e) {
        e.preventDefault();
        const form = e.target;
        if (form.startDate.value >= form.endDate.value) {
            showError('Thời gian kết thúc phải sau thời gian bắt đầu');
            return;
        }
        // Lấy đúng giá trị type từ select
        const type = form.querySelector('select[name="type"]').value;
        const data = {
            couponId: form.couponId.value || null,
            code: form.code.value,
            discountValue: parseFloat(form.value.value),
            minOrderValue: form.minOrderValue.value ? parseFloat(form.minOrderValue.value) : null,
            startDate: toDateTimeString(form.startDate.value),
            endDate: toDateTimeString(form.endDate.value),
            maxUsage: form.quantity.value ? parseInt(form.quantity.value) : null,
            description: form.description.value,
            isActive: true,
            applyToCustomer: 'all',
            type: type // Lấy đúng giá trị type từ select
        };
        const method = form.dataset.edit === 'true' ? 'PUT' : 'POST';
        const url = method === 'POST' ? '/api/admin/coupon' : `/api/admin/coupon/${data.couponId}`; // Đổi endpoint đúng chuẩn REST
        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
        .then(res => {
            if (!res.ok) return res.text().then(text => { throw new Error(text); });
            return res.json();
        })
        .then(() => {
            showSuccess('Lưu phiếu giảm giá thành công!');
            window.location.reload();
        })
        .catch(err => showError('Có lỗi khi lưu phiếu giảm giá! ' + err.message));
    };

    // --- SEARCH ---
    document.getElementById('programSearchBtn').onclick = () => {
        const keyword = document.getElementById('programSearchInput').value;
        loadPromotions(keyword, 0);
    };
    document.getElementById('programSearchInput').onkeyup = (e) => {
        if (e.key === 'Enter') {
            loadPromotions(e.target.value, 0);
        }
    };
     document.getElementById('voucherSearchBtn').onclick = () => {
        const keyword = document.getElementById('voucherSearchInput').value;
        loadCoupons(keyword);
    };
    document.getElementById('voucherSearchInput').onkeyup = (e) => {
        if (e.key === 'Enter') {
            loadCoupons(e.target.value);
        }
    };

    // --- CHỌN SẢN PHẨM ÁP DỤNG KHUYẾN MÃI ---
    const openProductModalBtn = document.getElementById('openProductModal');
    const selectProductsBtn = document.getElementById('selectProductsBtn');
    if (openProductModalBtn) {
        openProductModalBtn.onclick = function() {
            try {
                fetch('/api/product/admin/products')
                    .then(res => {
                        if (!res.ok) throw new Error('Lỗi API sản phẩm!');
                        return res.json();
                    })
                    .then(products => {
                        const tbody = document.querySelector('#productTable tbody');
                        tbody.innerHTML = '';
                        // Lấy lại giá trị mới nhất của productIdsInput mỗi lần mở modal
                        const selectedIds = (document.getElementById('variantIdsInput').value || '').split(',').map(id => id.trim()).filter(Boolean);
                        if (!products || products.length === 0) {
                            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Không có sản phẩm nào!</td></tr>';
                        } else {
                            products.forEach(product => {
                                tbody.innerHTML += `
                                  <tr>
                                    <td><input type="checkbox" value="${product.productId}" data-name="${product.name}"></td>
                                    <td><img src="${product.imageUrl ? product.imageUrl : '/static/images/default.png'}" width="40"></td>
                                    <td>${product.name}</td>
                                    <td>${product.basePrice}đ</td>
                                    <td>${product.variants && product.variants.length > 0 ? product.variants.map(v => v.colorName + ' / ' + v.sizeName).join(', ') : ''}</td>
                                  </tr>
                                `;
                            });
                            // Sau khi render xong, tick lại các checkbox và set row-selected
                            tbody.querySelectorAll('input[type="checkbox"]').forEach(cb => {
                                if (selectedIds.includes(cb.value)) {
                                    cb.checked = true;
                                    cb.closest('tr').classList.add('row-selected');
                                }
                                cb.addEventListener('change', function() {
                                    const tr = cb.closest('tr');
                                    if (cb.checked) {
                                        tr.classList.add('row-selected');
                                    } else {
                                        tr.classList.remove('row-selected');
                                    }
                                });
                            });
                        }
                        new bootstrap.Modal(document.getElementById('productModal')).show();
                    })
                    .catch(err => {
                        alert('Không thể tải danh sách sản phẩm! ' + err.message);
                        console.error('Lỗi fetch sản phẩm:', err);
                    });
            } catch (e) {
                alert('Lỗi JS khi mở modal sản phẩm!');
                console.error('Lỗi JS modal sản phẩm:', e);
            }
        };
    }
    if (selectProductsBtn) {
        selectProductsBtn.onclick = function() {
            try {
                const checked = document.querySelectorAll('#productTable input[type="checkbox"]:checked');
                const ids = Array.from(checked).map(cb => cb.value);
                const names = Array.from(checked).map(cb => cb.dataset.name);
                document.getElementById('variantIdsInput').value = ids.join(',');
                document.getElementById('selectedProducts').innerHTML = names.map(n => `<span class="badge bg-primary me-1">${n}</span>`).join(' ');
                bootstrap.Modal.getInstance(document.getElementById('productModal')).hide();
            } catch (e) {
                alert('Lỗi JS khi chọn sản phẩm!');
                console.error('Lỗi JS chọn sản phẩm:', e);
            }
        };
    }

    // --- Load thống kê động ---
    function loadPromotionStats() {
        fetch('/api/admin/promotions')
            .then(res => res.json())
            .then(data => {
                const now = new Date();
                const active = data.filter(p => {
                    const start = new Date(p.startDate);
                    const end = new Date(p.endDate);
                    end.setHours(23,59,59,999);
                    return now >= start && now <= end;
                }).length;
                document.getElementById('stat-active-programs').textContent = active;
            });
        fetch('/api/admin/promotions/coupons')
            .then(res => res.json())
            .then(data => {
                const now = new Date();
                const active = data.filter(c => {
                    const start = new Date(c.startDate);
                    const end = new Date(c.endDate);
                    end.setHours(23,59,59,999);
                    return now >= start && now <= end && (!c.maxUsage || c.usageCount < c.maxUsage);
                }).length;
                document.getElementById('stat-active-coupons').textContent = active;
                var totalCouponsEl = document.getElementById('stat-total-coupons');
                if (totalCouponsEl) totalCouponsEl.textContent = data.length;
                // Lượt sử dụng tháng này
                const thisMonth = now.getMonth();
                const thisYear = now.getFullYear();
                let usage = 0;
                data.forEach(c => {
                    if (c.usageCount && c.endDate) {
                        const end = new Date(c.endDate);
                        if (end.getMonth() === thisMonth && end.getFullYear() === thisYear) {
                            usage += c.usageCount;
                        }
                    }
                });
                document.getElementById('stat-usage-this-month').textContent = usage;
            });
    }
    loadPromotionStats();

    // --- Xử lý applyType ---
    let lastSelectedVariantIds = [];
    document.getElementById('applyType').addEventListener('change', function() {
        const applyType = this.value;
        const productSelection = document.getElementById('productSelectGroup');
        const categorySelection = document.getElementById('categorySelectGroup');
        if (applyType === 'single') {
            productSelection.style.display = 'block';
            categorySelection.style.display = 'none';
            // Fill lại lựa chọn cũ nếu có
            if (lastSelectedVariantIds.length > 0) {
                document.getElementById('variantIdsInput').value = lastSelectedVariantIds.join(',');
                // Hiển thị badge
                let badges = [];
                allProducts.forEach(product => {
                    (product.variants || []).forEach(variant => {
                        if (lastSelectedVariantIds.includes(variant.variantId)) {
                            badges.push(`<span class='badge bg-primary me-1 mb-1'>${product.name} - ${variant.colorName || ''} / ${variant.sizeName || ''}</span>`);
                        }
                    });
                });
                document.getElementById('selectedProducts').innerHTML = badges.length > 0 ? badges.join(' ') : '<span class="text-muted">Chưa chọn biến thể nào</span>';
            } else {
                document.getElementById('variantIdsInput').value = '';
                document.getElementById('selectedProducts').innerHTML = '';
            }
        } else if (applyType === 'category') {
            productSelection.style.display = 'none';
            categorySelection.style.display = 'block';
            // Lưu lại lựa chọn cũ
            lastSelectedVariantIds = document.getElementById('variantIdsInput').value ? document.getElementById('variantIdsInput').value.split(',').map(id => parseInt(id)).filter(id => !isNaN(id)) : [];
            document.getElementById('variantIdsInput').value = '';
            document.getElementById('selectedProducts').innerHTML = '';
        } else { // all
            productSelection.style.display = 'none';
            categorySelection.style.display = 'none';
            // Lưu lại lựa chọn cũ
            lastSelectedVariantIds = document.getElementById('variantIdsInput').value ? document.getElementById('variantIdsInput').value.split(',').map(id => parseInt(id)).filter(id => !isNaN(id)) : [];
            document.getElementById('variantIdsInput').value = '';
            // Badge chỉ hiển thị cho UI, không đẩy variantIds lên backend
            let badges = [];
            allProducts.forEach(product => {
                (product.variants || []).forEach(variant => {
                    badges.push(`<span class='badge bg-primary me-1 mb-1'>${product.name} - ${variant.colorName || ''} / ${variant.sizeName || ''}</span>`);
                });
            });
            document.getElementById('selectedProducts').innerHTML = badges.length > 0 ? badges.join(' ') : '<span class="text-muted">Không có sản phẩm nào</span>';
            document.getElementById('categoryIdsInput').value = '';
            document.getElementById('selectedCategories').innerHTML = '';
        }
    });
    // Khi mở modal chọn sản phẩm, luôn tích xanh các biến thể đã chọn
    document.getElementById('openProductSelectModal').onclick = function() {
        let selected = [];
        const inputVal = document.getElementById('variantIdsInput').value;
        if (inputVal) {
            selected = inputVal.split(',').map(id => parseInt(id)).filter(id => !isNaN(id));
        } else if (lastSelectedVariantIds.length > 0) {
            selected = lastSelectedVariantIds;
        }
        openProductSelectModal(selected);
    };
    // Khi xác nhận chọn sản phẩm, lưu lại lựa chọn cuối cùng
    document.getElementById('confirmProductSelect').onclick = function() {
        const checked = Array.from(document.querySelectorAll('.variant-checkbox:checked')).map(cb => parseInt(cb.value));
        document.getElementById('variantIdsInput').value = checked.join(',');
        lastSelectedVariantIds = checked;
        // Hiển thị badge tên biến thể đã chọn (tên sản phẩm, màu, size)
        let badges = [];
        allProducts.forEach(product => {
            (product.variants || []).forEach(variant => {
                if (checked.includes(variant.variantId)) {
                    badges.push(`<span class='badge bg-primary me-1 mb-1'>${product.name} - ${variant.colorName || ''} / ${variant.sizeName || ''}</span>`);
                }
            });
        });
        document.getElementById('selectedProducts').innerHTML = badges.length > 0 ? badges.join(' ') : '<span class="text-muted">Chưa chọn biến thể nào</span>';
        bootstrap.Modal.getInstance(document.getElementById('productSelectModal')).hide();
    };

    // --- Load dữ liệu bảng khi vào trang lần đầu ---
    loadPromotions('', 0);
    loadCoupons();

    // Fallback: Đảm bảo nút Hủy luôn đóng modal
    document.querySelectorAll('.btn-secondary[data-bs-dismiss="modal"]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            // Tìm modal cha gần nhất
            let modalEl = btn.closest('.modal');
            if (modalEl) {
                let modalInstance = bootstrap.Modal.getInstance(modalEl);
                if (modalInstance) modalInstance.hide();
            }
        });
    });
});