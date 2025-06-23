function getProductIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get('id');
}
tinymce.init({
    selector: '#product-description-editor',
    height: 300,
    plugins: [
        'anchor', 'autolink', 'charmap', 'codesample', 'emoticons', 'image', 'link', 'lists', 'media', 'searchreplace', 'table', 'visualblocks', 'wordcount'
    ],
    toolbar: 'undo redo | blocks fontfamily fontsize | bold italic underline strikethrough | link image media table mergetags | addcomment showcomments | spellcheckdialog a11ycheck typography | align lineheight | checklist numlist bullist indent outdent | emoticons charmap | removeformat',
    tinycomments_mode: 'embedded',
    tinycomments_author: 'Author name',
    mergetags_list: [
        { value: 'First.Name', title: 'First Name' },
        { value: 'Email', title: 'Email' },
    ],
    ai_request: (request, respondWith) => respondWith.string(() => Promise.reject('See docs to implement AI Assistant')),
});
tinymce.init({
    selector: '#product-short-description-editor',
    height: 300,
    plugins: [
        'anchor', 'autolink', 'charmap', 'codesample', 'emoticons', 'image', 'link', 'lists', 'media', 'searchreplace', 'table', 'visualblocks', 'wordcount'
    ],
    toolbar: 'undo redo | blocks fontfamily fontsize | bold italic underline strikethrough | link image media table mergetags | addcomment showcomments | spellcheckdialog a11ycheck typography | align lineheight | checklist numlist bullist indent outdent | emoticons charmap | removeformat',
    tinycomments_mode: 'embedded',
    tinycomments_author: 'Author name',
    mergetags_list: [
        { value: 'First.Name', title: 'First Name' },
        { value: 'Email', title: 'Email' },
    ],
    ai_request: (request, respondWith) => respondWith.string(() => Promise.reject('See docs to implement AI Assistant')),
});

let currentProduct = null;

function setDescriptionContent(content) {
    const editor = tinymce.get('product-description-editor');
    if (editor) {
        editor.setContent(content || '');
    } else {
        setTimeout(() => setDescriptionContent(content), 100);
    }
}
function setShortDescriptionContent(content) {
    const editor = tinymce.get('product-short-description-editor');
    if (editor) {
        editor.setContent(content || '');
    } else {
        setTimeout(() => setShortDescriptionContent(content), 100);
    }
}

// Load chi tiết sản phẩm
async function loadProductDetail() {
    const id = getProductIdFromUrl();
    if (!id) {
        alert('Không tìm thấy sản phẩm!');
        return;
    }
    const res = await fetch(`/api/product/${id}`);
    if (!res.ok) {
        alert('Không tìm thấy sản phẩm!');
        return;
    }
    const product = await res.json();
    currentProduct = product;
    console.log('Current Product:', product);

    document.getElementById('product-name').value = product.name || '';
    setDescriptionContent(product.description);
    setShortDescriptionContent(product.shortDescription);
    // TODO: trạng thái, nổi bật nếu có
    renderImages(product.images || []);
}

// Hiển thị danh sách ảnh
function renderImages(images) {
    const container = document.getElementById('image-preview-list');
    if (!container) return;
    container.innerHTML = '';
    images.forEach(img => {
        const div = document.createElement('div');
        div.className = 'image-preview d-inline-block position-relative';
        div.innerHTML = `<img src="${img.imageUrl}" alt="Ảnh sản phẩm"><button class="btn btn-sm btn-danger position-absolute top-0 end-0 btn-remove-image" data-id="${img.imageId}"><i class="fas fa-times"></i></button>`;
        container.appendChild(div);
    });
    // Gán sự kiện xóa ảnh (nếu muốn)
}

// Upload ảnh mới lên Cloudinary
async function uploadImage(file) {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch('/api/product/upload-image', {
        method: 'POST',
        body: formData
    });
    if (!res.ok) {
        alert('Upload ảnh thất bại!');
        return null;
    }
    return await res.text(); // trả về url ảnh
}

// Sự kiện chọn file ảnh
function setupImageUpload() {
    const input = document.getElementById('product-image-upload');
    if (!input) return;
    input.addEventListener('change', async function () {
        const file = this.files[0];
        if (!file) return;
        const url = await uploadImage(file);
        if (url) {
            // Thêm ảnh vào danh sách tạm thời
            if (!currentProduct.images) currentProduct.images = [];
            currentProduct.images.push({ imageUrl: url });
            renderImages(currentProduct.images);
        }
    });
}

// Sự kiện cập nhật sản phẩm
async function updateProduct() {
    const id = getProductIdFromUrl();
    if (!id) return;
    const name = document.getElementById('product-name').value;
    let description = '';
    let shortDescription = '';
    if (window.tinymce) {
        description = tinymce.get('product-description-editor').getContent();
        shortDescription = tinymce.get('product-short-description-editor').getContent();
    } else {
        description = document.getElementById('product-description-editor').value;
        shortDescription = document.getElementById('product-short-description-editor').value;
    }
    // TODO: trạng thái, nổi bật nếu có
    const images = (currentProduct.images || []).map(img => ({ imageUrl: img.imageUrl }));
    const body = {
        name,
        description,
        shortDescription,
        images
        // TODO: các trường khác
    };
    const res = await fetch(`/api/product/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (res.ok) {
        alert('Cập nhật thành công!');
        loadProductDetail();
    } else {
        alert('Cập nhật thất bại!');
    }
}

// Load danh mục (Category)
async function loadCategories(selectedCategoryIds = []) {
    const res = await fetch('/api/product/categories');
    const categories = await res.json();
    const container = document.querySelector('.category-tree');
    if (!container) return;
    function renderTree(categories, level = 0) {
        return categories.map(cat => {
            const checked = selectedCategoryIds.includes(cat.categoryId) ? 'checked' : '';
            let html = `
                <div class="form-check" style="margin-left:${level * 20}px">
                    <input class="form-check-input category-checkbox" type="checkbox" value="${cat.categoryId}" id="category-${cat.categoryId}" ${checked}>
                    <label class="form-check-label" for="category-${cat.categoryId}">${cat.name}</label>
                </div>
            `;
            if (cat.children && cat.children.length > 0) {
                html += renderTree(cat.children, level + 1);
            }
            return html;
        }).join('');
    }
    container.innerHTML = renderTree(categories);
}

// Load thuộc tính (Attribute) và giá trị thuộc tính (AttributeTerm) - render theo layout accordion-item
async function loadAttributes() {
    const container = document.querySelector('.list-attribute');
    if (!container) return;
    const productAttrs = (currentProduct && currentProduct.attributes) ? currentProduct.attributes : [];
    container.innerHTML = productAttrs.map(attr => {
        const attrTerms = attr.attributeValue || [];
        // Ghép các giá trị thành chuỗi, mỗi giá trị 1 dòng
        const termsText = attrTerms.map(term => term.term).join('\n');
        return `
            <div class="accordion-item">
                <h2 class="accordion-header">
                    <div class="accordion-button d-flex align-items-center justify-content-between collapsed ps-0"
                        data-bs-toggle="collapse"
                        data-bs-target="#panelsStayOpen-collapse-${attr.productAttributeId}"
                        aria-expanded="false"
                        aria-controls="panelsStayOpen-collapse-${attr.productAttributeId}"
                        style="cursor: pointer;">
                        <div class="d-flex align-items-center justify-content-between w-100 me-3">
                            <span class="text-dark fw-bold">${attr.attributeName}</span>
                            <button type="button" class="border-0 bg-transparent text-danger btn-remove-attribute" data-attribute-id="${attr.productAttributeId}">Xóa</button>
                        </div>
                    </div>
                </h2>
                <div id="panelsStayOpen-collapse-${attr.productAttributeId}"
                    class="accordion-collapse collapse"
                    aria-labelledby="panelsStayOpen-heading-${attr.productAttributeId}">
                    <div class="accordion-body p-0 mt-4 mb-1">
                        <div class="d-flex">
                            <span class="w-25">Tên: <strong>${attr.attributeName}</strong></span>
                            <div class="w-75">
                                <div class="form-group">
                                    <label class="form-label">Giá trị:</label>
                                    <textarea class="form-control" readonly rows="${Math.max(attrTerms.length, 1)}">${termsText}</textarea>
                                </div>
                                <div class="button-group mt-2">
                                    <button type="button" class="btn btn-outline-primary btn-select-all-terms" data-attribute-id="${attr.productAttributeId}">Chọn tất cả</button>
                                    <button type="button" class="btn btn-outline-primary btn-deselect-all-terms" data-attribute-id="${attr.productAttributeId}">Không chọn</button>
                                    <button type="button" class="btn btn-outline-primary float-end btn-add-term" data-bs-toggle="modal" data-bs-target="#add-attribute-term-modal" data-attribute-id="${attr.productAttributeId}">Thêm giá trị</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// Load danh sách biến thể
async function loadVariants() {
    const productId = getProductIdFromUrl();
    if (!productId) return;

    try {
        const res = await fetch(`/api/product/${productId}/variants`);
        const variants = await res.json();
        console.log('Variants loaded:', variants);

        const container = document.getElementById('variantsList');
        if (!container) return;

        container.innerHTML = variants.map(variant => `
            <tr>
                <td>
                    <div class="form-check">
                        <input class="form-check-input variant-checkbox" type="checkbox" value="${variant.variantId}">
                    </div>
                </td>
                <td>
                    ${variant.attributes?.map(attr => `${attr.attributeName}: ${attr.attributeValue}`).join('<br>') || 'N/A'}
                </td>
                <td>${formatPrice(variant.price)}</td>
                <td>${variant.quantityInStock}</td>
                <td>
                    <img src="${variant.imageUrl || '/images/no-image.png'}" alt="Variant image" style="width: 50px; height: 50px; object-fit: cover;">
                </td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="openVariantDetailModal(${variant.variantId})">
                        <i class="fas fa-edit"></i> Sửa
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteVariant(${variant.variantId})">
                        <i class="fas fa-trash"></i> Xóa
                    </button>
                </td>
            </tr>
        `).join('');

    } catch (error) {
        console.error('Error loading variants:', error);
        alert('Có lỗi khi tải danh sách biến thể!');
    }
}

// ================== QUẢN LÝ THUỘC TÍNH BIẾN THỂ ==================

// Lưu cache tất cả thuộc tính và giá trị thuộc tính
let allAttributes = [];
let allAttributeTerms = {}; // {attributeId: [terms]}

// Lấy tất cả thuộc tính
async function fetchAllAttributes() {
    if (allAttributes.length > 0) return allAttributes;
    const res = await fetch('/api/product/attributes');
    allAttributes = await res.json();
    return allAttributes;
}
// Lấy tất cả giá trị của 1 thuộc tính
async function fetchAttributeTerms(attributeId) {
    if (allAttributeTerms[attributeId]) return allAttributeTerms[attributeId];
    const res = await fetch(`/api/product/attribute-terms/by-attribute/${attributeId}`);
    const terms = await res.json();
    console.log(`Attribute Terms for ${attributeId}:`, terms);
    allAttributeTerms[attributeId] = terms;
    return terms;
}

// Hiển thị thuộc tính biến thể trong modal
async function renderVariantAttributes(variantId) {
    const container = document.getElementById('variant-attributes-list');
    container.innerHTML = '<div class="text-secondary">Đang tải...</div>';
    // Lấy thuộc tính hiện tại của biến thể
    const res = await fetch(`/api/variant/${variantId}/attributes`);
    const variantAttrs = await res.json();
    // Lấy tất cả thuộc tính
    const attributes = await fetchAllAttributes();
    // Render từng dòng
    container.innerHTML = '';
    for (const va of variantAttrs) {
        const attrId = va.attributeId;
        const termId = va.termId;
        const terms = await fetchAttributeTerms(attrId);
        // Dòng thuộc tính
        const row = document.createElement('div');
        row.className = 'd-flex align-items-center mb-2 variant-attr-row';
        row.dataset.variantAttributeId = va.variantAttributeId;
        // Hiển thị tên thuộc tính (dạng chữ)
        const labelAttr = document.createElement('span');
        labelAttr.className = 'me-2 fw-bold';
        labelAttr.style.minWidth = '120px';
        labelAttr.textContent = va.attributeName;
        // Select giá trị
        const selectTerm = document.createElement('select');
        selectTerm.className = 'form-select me-2';
        selectTerm.style.maxWidth = '180px';
        terms.forEach(term => {
            const opt = document.createElement('option');
            opt.value = term.termId;
            opt.textContent = term.term;
            if (term.termId === termId) opt.selected = true;
            selectTerm.appendChild(opt);
        });
        // Sự kiện đổi giá trị
        selectTerm.addEventListener('change', async function () {
            const newTermId = this.value;
            await fetch(`/api/variant/${variantId}/attributes/${va.variantAttributeId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ termId: newTermId })
            });
        });
        // Nút xóa
        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-danger btn-sm';
        btnDelete.innerHTML = '<i class="fas fa-trash"></i>';
        btnDelete.addEventListener('click', async function () {
            if (!confirm('Xóa thuộc tính này khỏi biến thể?')) return;
            await fetch(`/api/variant/${variantId}/attributes/${va.variantAttributeId}`, { method: 'DELETE' });
            renderVariantAttributes(variantId);
        });
        row.appendChild(labelAttr);
        row.appendChild(selectTerm);
        row.appendChild(btnDelete);
        container.appendChild(row);
    }
}

// Thêm dòng thuộc tính mới
async function addVariantAttributeRow(variantId) {
    const container = document.getElementById('variant-attributes-list');
    // Lấy thuộc tính chưa có trong biến thể
    const res = await fetch(`/api/variant/${variantId}/attributes`);
    const variantAttrs = await res.json();
    console.log('Current Variant Attributes:', variantAttrs);
    const usedAttrIds = variantAttrs.map(va => va.attribute.attributeId);
    const attributes = await fetchAllAttributes();
    const availableAttrs = attributes.filter(attr => !usedAttrIds.includes(attr.attributeId));
    if (availableAttrs.length === 0) {
        alert('Đã thêm hết các thuộc tính!');
        return;
    }
    // Dòng mới
    const row = document.createElement('div');
    row.className = 'd-flex align-items-center mb-2 variant-attr-row';
    // Select thuộc tính
    const selectAttr = document.createElement('select');
    selectAttr.className = 'form-select me-2';
    selectAttr.style.maxWidth = '180px';
    availableAttrs.forEach(attr => {
        const opt = document.createElement('option');
        opt.value = attr.attributeId;
        opt.textContent = attr.attributeName;
        selectAttr.appendChild(opt);
    });
    // Select giá trị (ban đầu rỗng)
    const selectTerm = document.createElement('select');
    selectTerm.className = 'form-select me-2';
    selectTerm.style.maxWidth = '180px';
    // Khi chọn thuộc tính thì load giá trị
    selectAttr.addEventListener('change', async function () {
        const attrId = this.value;
        const terms = await fetchAttributeTerms(attrId);
        selectTerm.innerHTML = '';
        terms.forEach(term => {
            const opt = document.createElement('option');
            opt.value = term.termId;
            opt.textContent = term.term;
            selectTerm.appendChild(opt);
        });
    });
    // Gọi lần đầu
    selectAttr.dispatchEvent(new Event('change'));
    // Nút lưu
    const btnSave = document.createElement('button');
    btnSave.className = 'btn btn-success btn-sm';
    btnSave.innerHTML = '<i class="fas fa-check"></i>';
    btnSave.addEventListener('click', async function () {
        const attrId = selectAttr.value;
        const termId = selectTerm.value;
        if (!attrId || !termId) return alert('Chọn đủ thuộc tính và giá trị!');
        await fetch(`/api/variant/${variantId}/attributes`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ attributeId: attrId, termId: termId })
        });
        renderVariantAttributes(variantId);
    });
    // Nút hủy
    const btnCancel = document.createElement('button');
    btnCancel.className = 'btn btn-secondary btn-sm ms-2';
    btnCancel.innerHTML = '<i class="fas fa-times"></i>';
    btnCancel.addEventListener('click', function () {
        row.remove();
    });
    row.appendChild(selectAttr);
    row.appendChild(selectTerm);
    row.appendChild(btnSave);
    row.appendChild(btnCancel);
    container.appendChild(row);
}

// Gán sự kiện mở modal sửa biến thể
function setupVariantModalEvents() {
    const container = document.getElementById('variantsList');
    if (!container) return;
    container.addEventListener('click', async function (e) {
        if (e.target.closest('.btn-edit-product')) {
            const tr = e.target.closest('tr');
            const tds = tr.querySelectorAll('td');
            const variantId = tds[0].textContent.trim();
            document.getElementById('edit-variant-id').value = variantId;
            await renderVariantAttributes(variantId);
            // Gán lại sự kiện cho nút thêm thuộc tính
            const btnAdd = document.getElementById('btn-add-variant-attribute');
            btnAdd.onclick = function () {
                addVariantAttributeRow(variantId);
            };
        }
    });
}

window.addEventListener('DOMContentLoaded', async () => {
    // Khởi tạo tooltip
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
    await loadProductDetail();
    setupImageUpload();
    // Lấy id sản phẩm từ URL
    const productId = getProductIdFromUrl();
    if (!productId) return;
    // Lấy thông tin sản phẩm đã load
    const product = currentProduct;
    // Load danh mục
    loadCategories((product.categories || []).map(c => c.categoryId));
    // Load thuộc tính
    loadAttributes();
    // Load biến thể
    loadVariants();

    const btnUpdateProduct = document.getElementById('update-product-btn');
    btnUpdateProduct.addEventListener('click', updateProduct);

    setupVariantModalEvents();
});

// Sự kiện mở modal thêm giá trị thuộc tính (vanilla JS)
document.addEventListener('click', function (e) {
    if (e.target.classList.contains('btn-add-term')) {
        const attrId = e.target.getAttribute('data-attribute-id');
        document.getElementById('modal-attribute-id').value = attrId;
        document.getElementById('modal-attribute-term').value = '';
    }
});

// Sự kiện thêm giá trị thuộc tính (vanilla JS)
document.getElementById('btn-modal-add-attribute-term').addEventListener('click', async function () {
    const attrId = document.getElementById('modal-attribute-id').value;
    const term = document.getElementById('modal-attribute-term').value.trim();
    if (!term) return alert('Nhập giá trị!');
    const res = await fetch('/api/product/attribute-terms', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ attributeId: attrId, term })
    });
    if (res.ok) {
        alert('Thêm thành công!');
        // Reload lại thuộc tính (hoặc chỉ giá trị thuộc tính đó)
        loadAttributes();
    } else {
        alert('Thêm thất bại!');
    }
});

// Hàm mở modal chỉnh sửa biến thể
function openVariantDetailModal(variantId) {
    // Reset form
    document.getElementById('variantDetailForm').reset();
    document.getElementById('variantImageList').innerHTML = '';
    document.getElementById('variantAttributes').innerHTML = '';

    // Load thông tin biến thể
    fetch(`/api/product-variant/${variantId}`)
        .then(res => res.json())
        .then(variant => {
            document.getElementById('variantId').value = variant.variantId;
            document.getElementById('variantSku').value = variant.sku || '';
            document.getElementById('variantBarcode').value = variant.barcode || '';
            document.getElementById('variantPrice').value = variant.price || 0;
            document.getElementById('variantDiscountPrice').value = variant.discountPrice || 0;
            document.getElementById('variantDiscountStart').value = variant.discountPriceStartAt ? variant.discountPriceStartAt.substring(0, 16) : '';
            document.getElementById('variantDiscountEnd').value = variant.discountPriceEndAt ? variant.discountPriceEndAt.substring(0, 16) : '';
            document.getElementById('variantQuantity').value = variant.quantityInStock || 0;
            document.getElementById('variantIsActive').value = variant.isActive ? "true" : "false";

            // Load ảnh biến thể
            loadVariantImages(variantId);
            // Load thuộc tính biến thể
            loadVariantAttributes(variantId);

            // Hiển thị modal bằng Bootstrap 5
            const modal = new bootstrap.Modal(document.getElementById('variantDetailModal'));
            modal.show();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi khi tải thông tin biến thể!');
        });
}

// Hàm load ảnh biến thể
function loadVariantImages(variantId) {
    const container = document.getElementById('variantImageList');
    container.innerHTML = ''; // Clear existing images

    // TODO: Gọi API lấy danh sách ảnh của biến thể
    // Ví dụ hiển thị ảnh:
    /*
    images.forEach(img => {
        const imgWrapper = document.createElement('div');
        imgWrapper.className = 'position-relative m-2';
        imgWrapper.innerHTML = `
            <img src="${img.imageUrl}" alt="Variant image" style="width: 100px; height: 100px; object-fit: cover;">
            <button type="button" class="btn btn-sm btn-danger position-absolute" style="top: 0; right: 0;"
                    onclick="deleteVariantImage(${img.imageId})">
                <i class="fas fa-times"></i>
            </button>
        `;
        container.appendChild(imgWrapper);
    });
    */
}

// Hàm load thuộc tính biến thể
function loadVariantAttributes(variantId) {
    fetch(`/api/variant/${variantId}/attributes`)
        .then(res => res.json())
        .then(attributes => {
            const container = document.getElementById('variantAttributes');
            container.innerHTML = ''; // Clear existing attributes

            attributes.forEach(attr => {
                const attrDiv = document.createElement('div');
                attrDiv.className = 'mb-2';
                attrDiv.innerHTML = `
                    <div class="d-flex justify-content-between align-items-center">
                        <strong>${attr.attributeName}:</strong>
                        <span>${attr.termName}</span>
                        <button type="button" class="btn btn-sm btn-outline-primary"
                                onclick="editVariantAttribute(${attr.variantAttributeId})">
                            <i class="fas fa-edit"></i>
                        </button>
                    </div>
                `;
                container.appendChild(attrDiv);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi khi tải thuộc tính biến thể!');
        });
}

// Hàm lưu biến thể
document.getElementById('saveVariantBtn')?.addEventListener('click', function () {
    const id = document.getElementById('variantId').value;
    const data = {
        sku: document.getElementById('variantSku').value,
        barcode: document.getElementById('variantBarcode').value,
        price: parseFloat(document.getElementById('variantPrice').value),
        discountPrice: parseFloat(document.getElementById('variantDiscountPrice').value) || null,
        discountPriceStartAt: document.getElementById('variantDiscountStart').value || null,
        discountPriceEndAt: document.getElementById('variantDiscountEnd').value || null,
        quantityInStock: parseInt(document.getElementById('variantQuantity').value),
        isActive: document.getElementById('variantIsActive').value === "true"
    };

    // Upload ảnh mới nếu có
    const fileInput = document.getElementById('variantImage');
    if (fileInput.files.length > 0) {
        const formData = new FormData();
        for (let i = 0; i < fileInput.files.length; i++) {
            formData.append('files', fileInput.files[i]);
        }

        fetch('/api/product/upload-image', {
            method: 'POST',
            body: formData
        })
            .then(res => res.json())
            .then(urls => {
                // TODO: Lưu URLs vào biến thể
                saveVariantData(id, data);
            })
            .catch(error => {
                console.error('Error uploading images:', error);
                alert('Có lỗi khi tải ảnh lên!');
            });
    } else {
        saveVariantData(id, data);
    }
});

function saveVariantData(id, data) {
    fetch(`/api/product-variant/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (res.ok) {
                alert('Cập nhật thành công!');
                const modal = bootstrap.Modal.getInstance(document.getElementById('variantDetailModal'));
                modal.hide();
                // Reload lại danh sách biến thể
                loadVariants();
            } else {
                alert('Có lỗi khi cập nhật!');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi khi cập nhật biến thể!');
        });
}

// Hàm xóa biến thể
document.getElementById('deleteVariantBtn')?.addEventListener('click', function () {
    const id = document.getElementById('variantId').value;
    if (confirm('Bạn chắc chắn muốn xóa biến thể này?')) {
        deleteVariant(id);
    }
});

function deleteVariant(id) {
    fetch(`/api/product-variant/${id}`, {
        method: 'DELETE'
    })
        .then(res => {
            if (res.ok) {
                alert('Đã xóa biến thể!');
                const modal = bootstrap.Modal.getInstance(document.getElementById('variantDetailModal'));
                if (modal) modal.hide();
                // Reload lại danh sách biến thể
                loadVariants();
            } else {
                alert('Không thể xóa biến thể!');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi khi xóa biến thể!');
        });
}

// Hàm chỉnh sửa thuộc tính biến thể
function editVariantAttribute(variantAttributeId) {
    // TODO: Implement modal chỉnh sửa thuộc tính
    alert('Chức năng đang được phát triển!');
}

// Hàm xóa ảnh biến thể
function deleteVariantImage(imageId) {
    if (confirm('Bạn chắc chắn muốn xóa ảnh này?')) {
        // TODO: Implement API xóa ảnh
        alert('Chức năng đang được phát triển!');
    }
}

// Format giá tiền
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

// Khởi tạo khi trang load
document.addEventListener('DOMContentLoaded', function () {
    loadVariants();
});