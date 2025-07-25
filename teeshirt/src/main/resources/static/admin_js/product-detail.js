document.addEventListener('DOMContentLoaded', function () {
    // Khởi tạo tooltip
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

    // Khởi tạo xử lý ảnh biến thể (modal sửa)
    initImageUploadForModal({
        inputId: 'variantImageInput',
        uploadAreaSelector: '#variantDetailModal .upload-area',
        newImagesListId: 'newImagesList',
        newImagesContainerId: 'newImagesContainer',
        filesKey: 'newVariantFiles'
    });
    // Khởi tạo xử lý ảnh biến thể (modal thêm mới)
    initImageUploadForModal({
        inputId: 'variantImageInput2',
        uploadAreaSelector: '#addVariantModal .upload-area',
        newImagesListId: 'newImagesList2',
        newImagesContainerId: 'newImagesContainer2',
        filesKey: 'newVariantFiles2'
    });

    // Khởi tạo xử lý ảnh sản phẩm
    initProductImageHandling();

    // Hiển thị ảnh sản phẩm hiện tại
    displayCurrentProductImages();

    // Khởi tạo xử lý category tree
    initCategoryTree();

    if (document.getElementById('variantColor'))
        document.getElementById('variantColor').addEventListener('change', onVariantSelectChange);
    if (document.getElementById('variantSize'))
        document.getElementById('variantSize').addEventListener('change', onVariantSelectChange);
});

/**
 * Khởi tạo xử lý ảnh cho modal biến thể (dùng chung cho cả 2 modal)
 * @param {string} inputId - id input file
 * @param {string} uploadAreaSelector - selector vùng upload
 * @param {string} newImagesListId - id list ảnh mới
 * @param {string} newImagesContainerId - id container ảnh mới
 * @param {string} filesKey - key lưu files vào window
 */
function initImageUploadForModal({
    inputId,
    uploadAreaSelector,
    newImagesListId,
    newImagesContainerId,
    filesKey
}) {
    const uploadArea = document.querySelector(uploadAreaSelector);
    const fileInput = document.getElementById(inputId);
    const newImagesList = document.getElementById(newImagesListId);
    const newImagesContainer = document.getElementById(newImagesContainerId);
    if (!uploadArea || !fileInput) return;
    window[filesKey] = [];
    // Xóa event cũ nếu có
    uploadArea.replaceWith(uploadArea.cloneNode(true));
    fileInput.replaceWith(fileInput.cloneNode(true));
    // Lấy lại element sau khi clone
    const uploadAreaNew = document.querySelector(uploadAreaSelector);
    const fileInputNew = document.getElementById(inputId);
    // Drag & Drop events
    uploadAreaNew.addEventListener('dragover', function (e) {
        e.preventDefault();
        uploadAreaNew.classList.add('dragover');
    });
    uploadAreaNew.addEventListener('dragleave', function (e) {
        e.preventDefault();
        uploadAreaNew.classList.remove('dragover');
    });
    uploadAreaNew.addEventListener('drop', function (e) {
        e.preventDefault();
        uploadAreaNew.classList.remove('dragover');
        const files = e.dataTransfer.files;
        handleImageFilesGeneric(files, newImagesListId, newImagesContainerId, filesKey);
    });
    uploadAreaNew.addEventListener('click', function (e) {
        // Chỉ trigger fileInput nếu click KHÔNG phải vào input file
        if (e.target !== fileInputNew) {
            fileInputNew.click();
        }
    });
    fileInputNew.addEventListener('change', function (e) {
        handleImageFilesGeneric(e.target.files, newImagesListId, newImagesContainerId, filesKey);
    });
}

/**
 * Xử lý files ảnh cho modal biến thể (dùng chung)
 */
function handleImageFilesGeneric(files, newImagesListId, newImagesContainerId, filesKey) {
    const newImagesList = document.getElementById(newImagesListId);
    const newImagesContainer = document.getElementById(newImagesContainerId);
    Array.from(files).forEach(file => {
        if (file.type.startsWith('image/')) {
            if (file.size > 5 * 1024 * 1024) {
                alert(`File ${file.name} quá lớn. Vui lòng chọn file nhỏ hơn 5MB.`);
                return;
            }
            const reader = new FileReader();
            reader.onload = function (e) {
                const fileIndex = window[filesKey].length;
                window[filesKey].push(file);
                const imageItem = createImageItemGeneric(e.target.result, file.name, fileIndex, 'new', null, newImagesListId, filesKey);
                newImagesList.appendChild(imageItem);
                newImagesContainer.style.display = 'block';
            };
            reader.readAsDataURL(file);
        }
    });
}

/**
 * Tạo element ảnh cho modal biến thể (dùng chung)
 */
function createImageItemGeneric(src, name, fileIndex = null, type = 'existing', imageId = null, newImagesListId, filesKey) {
    const imageItem = document.createElement('div');
    imageItem.className = 'image-item';
    imageItem.dataset.type = type;
    if (fileIndex !== null) imageItem.dataset.fileIndex = fileIndex;
    if (imageId) imageItem.dataset.imageId = imageId;
    const img = document.createElement('img');
    img.src = src;
    img.alt = name;
    img.title = name;
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'delete-btn';
    deleteBtn.innerHTML = '×';
    deleteBtn.title = 'Xóa ảnh';
    deleteBtn.onclick = function () {
        deleteImageItemGeneric(imageItem, newImagesListId, filesKey);
    };
    const imageInfo = document.createElement('div');
    imageInfo.className = 'image-info';
    imageInfo.textContent = name.length > 10 ? name.substring(0, 10) + '...' : name;
    imageItem.appendChild(img);
    imageItem.appendChild(deleteBtn);
    imageItem.appendChild(imageInfo);
    img.addEventListener('click', function () {
        showImagePreview(src, name);
    });
    return imageItem;
}

/**
 * Xóa ảnh cho modal biến thể (dùng chung)
 */
function deleteImageItemGeneric(imageItem, newImagesListId, filesKey) {
    const type = imageItem.dataset.type;
    const imageId = imageItem.dataset.imageId;
    const fileIndex = imageItem.dataset.fileIndex;
    if (type === 'existing' && imageId) {
        if (confirm('Bạn có chắc muốn xóa ảnh này?')) {
            deleteExistingImage(imageId, imageItem);
        }
    } else {
        if (fileIndex !== null && window[filesKey][fileIndex]) {
            window[filesKey].splice(fileIndex, 1);
            document.querySelectorAll(`#${newImagesListId} .image-item[data-type="new"]`).forEach((item, index) => {
                if (item !== imageItem) {
                    item.dataset.fileIndex = index;
                }
            });
        }
        imageItem.remove();
        const newImagesList = document.getElementById(newImagesListId);
        if (newImagesList.children.length === 0) {
            document.getElementById(newImagesListId.replace('List', 'Container')).style.display = 'none';
        }
    }
}

// Hiển thị ảnh lớn
function showImagePreview(src, name) {
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">${name}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body text-center">
                    <img src="${src}" class="image-preview" alt="${name}">
                </div>
            </div>  
        </div>
    `;

    document.body.appendChild(modal);
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();

    modal.addEventListener('hidden.bs.modal', function () {
        document.body.removeChild(modal);
    });
}

// Số lượng ảnh tối đa cho sản phẩm
const MAX_PRODUCT_IMAGES = 5;

// Cập nhật số lượng ảnh còn lại
function updateProductImageCount() {
    const currentImages = document.querySelectorAll('#productImageList .product-image-item[data-type="existing"]').length;
    const newImages = window.newProductFiles ? window.newProductFiles.length : 0;
    const total = currentImages + newImages;
    const remain = MAX_PRODUCT_IMAGES - total;
    const countDiv = document.getElementById('product-image-count');
    const fileInput = document.getElementById('productImageInput');
    const uploadArea = document.querySelector('.product-upload-area');

    if (countDiv) {
        countDiv.textContent = `Đã chọn ${total}/${MAX_PRODUCT_IMAGES} ảnh. Có thể thêm tối đa ${remain < 0 ? 0 : remain} ảnh nữa.`;
        // Thêm thông báo nếu đã đủ ảnh
        if (remain <= 0) {
            countDiv.textContent += ' (Đã đạt tối đa ảnh)';
        }
    }
    // Disable input và làm mờ vùng upload nếu đủ ảnh
    if (fileInput && uploadArea) {
        if (remain <= 0) {
            fileInput.disabled = true;
            uploadArea.classList.add('disabled-upload-area');
            uploadArea.style.opacity = '0.5';
            uploadArea.style.pointerEvents = 'none';
        } else {
            fileInput.disabled = false;
            uploadArea.classList.remove('disabled-upload-area');
            uploadArea.style.opacity = '';
            uploadArea.style.pointerEvents = '';
        }
    }
}

// Khởi tạo xử lý ảnh sản phẩm
function initProductImageHandling() {
    const uploadArea = document.querySelector('.product-upload-area');
    const fileInput = document.getElementById('productImageInput');
    const newImagesList = document.getElementById('newProductImagesList');
    const newImagesContainer = document.getElementById('newProductImagesContainer');

    if (!uploadArea || !fileInput) {
        console.error('Không tìm thấy upload area hoặc file input');
        return;
    }

    // Lưu trữ files toàn cục cho sản phẩm
    window.newProductFiles = [];

    // Drag & Drop events
    uploadArea.addEventListener('dragover', function (e) {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', function (e) {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', function (e) {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        const files = e.dataTransfer.files;
        handleProductImageFiles(files);
    });

    // Click to upload
    uploadArea.addEventListener('click', function (e) {
        // Chỉ trigger fileInput nếu click KHÔNG phải vào input file
        if (e.target !== fileInput) {
            fileInput.click();
        }
    });

    // File input change
    fileInput.addEventListener('change', function (e) {
        handleProductImageFiles(e.target.files);
    });

    updateProductImageCount();
}

// Xử lý files ảnh sản phẩm được chọn
function handleProductImageFiles(files) {
    const newImagesList = document.getElementById('newProductImagesList');
    const newImagesContainer = document.getElementById('newProductImagesContainer');

    if (!newImagesList || !newImagesContainer) {
        console.error('Không tìm thấy container elements');
        return;
    }

    // Đếm tổng số ảnh hiện có (cả cũ và mới)
    const currentImages = document.querySelectorAll('#productImageList .product-image-item[data-type="existing"]').length;
    let newFiles = window.newProductFiles || [];
    let total = currentImages + newFiles.length;
    let remain = MAX_PRODUCT_IMAGES - total;

    // Lọc file hợp lệ và không vượt quá số lượng còn lại
    let filesToAdd = [];
    for (let i = 0; i < files.length; i++) {
        if (newFiles.length + currentImages + filesToAdd.length >= MAX_PRODUCT_IMAGES) break;
        const file = files[i];
        if (file.type.startsWith('image/')) {
            if (file.size > 5 * 1024 * 1024) {
                alert(`File ${file.name} quá lớn. Vui lòng chọn file nhỏ hơn 5MB.`);
                continue;
            }
            filesToAdd.push(file);
        }
    }
    if (filesToAdd.length === 0) {
        alert('Không thể thêm ảnh. Đã đạt tối đa hoặc không có file hợp lệ.');
        return;
    }

    // Thêm file vào mảng và hiển thị
    filesToAdd.forEach(file => {
        const reader = new FileReader();
        reader.onload = function (e) {
            const fileIndex = window.newProductFiles.length;
            window.newProductFiles.push(file);
            const imageItem = createProductImageItem(e.target.result, file.name, fileIndex, 'new');
            newImagesList.appendChild(imageItem);
            newImagesContainer.style.display = 'block';
            updateProductImageCount();
        };
        reader.readAsDataURL(file);
    });
}

// Tạo element ảnh sản phẩm
function createProductImageItem(src, name, fileIndex = null, type = 'existing', imageId = null) {
    const imageItem = document.createElement('div');
    imageItem.className = 'product-image-item';
    imageItem.dataset.type = type;
    if (fileIndex !== null) {
        imageItem.dataset.fileIndex = fileIndex;
    }
    if (imageId) imageItem.dataset.imageId = imageId;

    const img = document.createElement('img');
    img.src = src;
    img.alt = name;
    img.title = name;

    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'delete-btn';
    deleteBtn.innerHTML = '×';
    deleteBtn.title = 'Xóa ảnh';
    deleteBtn.onclick = function () {
        deleteProductImageItem(imageItem);
    };

    const imageInfo = document.createElement('div');
    imageInfo.className = 'image-info';
    imageInfo.textContent = name.length > 12 ? name.substring(0, 12) + '...' : name;

    imageItem.appendChild(img);
    imageItem.appendChild(deleteBtn);
    imageItem.appendChild(imageInfo);

    // Click để xem ảnh lớn
    img.addEventListener('click', function () {
        showImagePreview(src, name);
    });

    return imageItem;
}

// Xóa ảnh sản phẩm
function deleteProductImageItem(imageItem) {
    const type = imageItem.dataset.type;
    const imageId = imageItem.dataset.imageId;
    const fileIndex = imageItem.dataset.fileIndex;

    if (type === 'existing' && imageId) {
        // Xóa ảnh hiện có từ server
        if (confirm('Bạn có chắc muốn xóa ảnh này?')) {
            deleteExistingProductImage(imageId, imageItem);
        }
    } else {
        // Xóa ảnh mới upload
        if (fileIndex !== null && window.newProductFiles[fileIndex]) {
            window.newProductFiles.splice(fileIndex, 1);
            // Cập nhật lại index cho các file còn lại
            document.querySelectorAll('.product-image-item[data-type="new"]').forEach((item, index) => {
                if (item !== imageItem) {
                    item.dataset.fileIndex = index;
                }
            });
        }
        imageItem.remove();

        // Ẩn container nếu không còn ảnh mới
        const newImagesList = document.getElementById('newProductImagesList');
        if (newImagesList.children.length === 0) {
            document.getElementById('newProductImagesContainer').style.display = 'none';
        }
        updateProductImageCount();
    }
}

// Xóa ảnh sản phẩm hiện có từ server
function deleteExistingProductImage(imageId, imageItem) {
    fetch(`/api/product/product/image/${imageId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'ok') {
                imageItem.remove();
                // Ẩn container nếu không còn ảnh
                const productImageList = document.getElementById('productImageList');
                if (productImageList.children.length === 0) {
                    productImageList.innerHTML = '<p class="text-muted">Chưa có ảnh nào</p>';
                }
                updateCurrentImageCount();
            } else {
                alert('Lỗi khi xóa ảnh: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error deleting product image:', error);
            alert('Lỗi khi xóa ảnh sản phẩm');
        });
}

// Hiển thị ảnh sản phẩm hiện tại
function displayCurrentProductImages() {
    const productId = document.querySelector('input[name="productId"]').value;
    const imagesDiv = document.getElementById('productImageList');

    // Gọi API để lấy ảnh sản phẩm
    fetch(`/api/product/${productId}/images`)
        .then(response => response.json())
        .then(response => {
            if (response.status === 'ok' && response.data && response.data.length > 0) {
                imagesDiv.innerHTML = '';
                // Hiển thị tối đa 5 ảnh
                const img = response.data.slice(0, MAX_PRODUCT_IMAGES);
                img.forEach(img => {
                    const imageItem = createProductImageItem(
                        img.imageUrl,
                        `Ảnh thumbnail`,
                        null,
                        'existing',
                        img.imageId,
                    );
                    imagesDiv.appendChild(imageItem);
                });
            } else {
                imagesDiv.innerHTML = '<p class="text-muted">Chưa có ảnh thumbnail</p>';
            }
            updateProductImageCount();
        })
        .catch(error => {
            console.error('Error fetching product images:', error);
            imagesDiv.innerHTML = '<p class="text-muted">Chưa có ảnh thumbnail</p>';
            updateProductImageCount();
        });
}

// Reset nút cập nhật
function resetUpdateButton(button, originalText) {
    button.innerHTML = originalText;
    button.disabled = false;
}

// Sự kiện click nút sửa biến thể
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll(".btn-edit-variant").forEach(button => {
        button.addEventListener("click", function () {
            const variantId = this.dataset.variantId;
            const productId = this.dataset.productId;

            // Reset form và ảnh
            resetVariantFormGeneric('variant');

            // Gọi API để lấy thông tin biến thể
            fetch(`/api/product/variant/${variantId}`)
                .then(response => response.json())
                .then(json => {
                    if (json.status === 'ok' && json.data && json.data.variant) {
                        const v = json.data.variant;
                        // Fill các trường cơ bản
                        document.getElementById('variantId').value = v.variantId || '';
                        document.getElementById('variantName').value = v.name || '';
                        document.getElementById('variantSku').value = v.sku || '';
                        document.getElementById('variantBarcode').value = v.barcode || '';
                        document.getElementById('variantPrice').value = v.price || '';
                        document.getElementById('variantDiscountPrice').value = v.discountPrice || '';
                        document.getElementById('variantDiscountStart').value = v.discountPriceStartAt ? v.discountPriceStartAt.substring(0, 16) : '';
                        document.getElementById('variantDiscountEnd').value = v.discountPriceEndAt ? v.discountPriceEndAt.substring(0, 16) : '';
                        document.getElementById('variantQuantity').value = v.quantityInStock || '';
                        document.getElementById('variantIsActive').value = v.isActive ? 'true' : 'false';

                        // Fill thuộc tính màu, size, chất liệu
                        if (document.getElementById('variantColor'))
                            document.getElementById('variantColor').value = v.colorName || '';
                        if (document.getElementById('variantSize'))
                            document.getElementById('variantSize').value = v.sizeName || '';

                        // Hiển thị ảnh biến thể hiện tại
                        displayExistingImages(json.data.images);
                    } else {
                        alert(json.message || 'Không lấy được dữ liệu biến thể!');
                    }
                })
                .catch(error => console.error('Error fetching variant:', error));
        });
    });
});

// Reset form biến thể
function resetVariantFormGeneric(prefix) {
    document.getElementById(prefix + 'Name').value = '';
    document.getElementById(prefix + 'Sku').value = '';
    document.getElementById(prefix + 'Barcode').value = '';
    document.getElementById(prefix + 'Price').value = '';
    document.getElementById(prefix + 'DiscountPrice').value = '';
    document.getElementById(prefix + 'DiscountStart').value = '';
    document.getElementById(prefix + 'DiscountEnd').value = '';
    document.getElementById(prefix + 'Quantity').value = '';
    document.getElementById(prefix + 'IsActive').value = 'true';
    if (document.getElementById(prefix + 'ImageInput')) document.getElementById(prefix + 'ImageInput').value = '';
    if (document.getElementById('variantImageList' + (prefix === 'variant' ? '' : '2'))) document.getElementById('variantImageList' + (prefix === 'variant' ? '' : '2')).innerHTML = '';
    if (document.getElementById('newImagesList' + (prefix === 'variant' ? '' : '2'))) document.getElementById('newImagesList' + (prefix === 'variant' ? '' : '2')).innerHTML = '';
    if (document.getElementById('newImagesContainer' + (prefix === 'variant' ? '' : '2'))) document.getElementById('newImagesContainer' + (prefix === 'variant' ? '' : '2')).style.display = 'none';
    window['newVariantFiles' + (prefix === 'variant' ? '' : '2')] = [];
}

// Hiển thị ảnh hiện có
function displayExistingImages(images) {
    const imagesDiv = document.getElementById('variantImageList');
    imagesDiv.innerHTML = '';

    if (images && images.length > 0) {
        images.forEach(img => {
            const imageItem = createImageItemGeneric(
                img.imageUrl,
                `Ảnh ${img.imageId}`,
                null,
                'existing',
                img.imageId,
                'variantImageList',
                'newVariantFiles'
            );
            imagesDiv.appendChild(imageItem);
        });
    } else {
        imagesDiv.innerHTML = '<p class="text-muted">Chưa có ảnh nào</p>';
    }
}

// Xử lý nút Lưu biến thể
document.addEventListener('DOMContentLoaded', function () {
    const saveVariantBtn = document.getElementById('saveVariantBtn');
    if (saveVariantBtn) {
        saveVariantBtn.addEventListener('click', function () {
            saveVariantData();
        });
    }
});

// Lưu dữ liệu biến thể
function saveVariantData() {
    const variantId = document.getElementById('variantId').value;
    const variantData = {
        variantId: variantId,
        name: document.getElementById('variantName').value,
        sku: document.getElementById('variantSku').value,
        barcode: document.getElementById('variantBarcode').value,
        price: document.getElementById('variantPrice').value,
        discountPrice: document.getElementById('variantDiscountPrice').value || null,
        discountPriceStartAt: document.getElementById('variantDiscountStart').value || null,
        discountPriceEndAt: document.getElementById('variantDiscountEnd').value || null,
        quantityInStock: document.getElementById('variantQuantity').value,
        isActive: document.getElementById('variantIsActive').value === 'true'
    };

    // Hiển thị loading
    const saveBtn = document.getElementById('saveVariantBtn');
    const originalText = saveBtn.innerHTML;
    saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang lưu...';
    saveBtn.disabled = true;

    // Cập nhật thông tin biến thể
    fetch(`/api/product/variant/${variantId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(variantData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'ok') {
                // Upload ảnh mới nếu có
                uploadNewImages(variantId);
            } else {
                alert('Lỗi khi cập nhật biến thể: ' + data.message);
                resetSaveButton(saveBtn, originalText);
            }
        })
        .catch(error => {
            console.error('Error updating variant:', error);
            alert('Lỗi khi cập nhật biến thể');
            resetSaveButton(saveBtn, originalText);
        });
}

// Upload ảnh mới
function uploadNewImages(variantId) {
    const newImagesList = document.getElementById('newImagesList');
    const imageItems = newImagesList.querySelectorAll('.image-item[data-type="new"]');

    if (imageItems.length === 0) {
        // Không có ảnh mới, đóng modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('variantDetailModal'));
        modal.hide();
        location.reload(); // Reload để cập nhật danh sách
        return;
    }

    // Kiểm tra xem có file nào không
    const validFiles = [];
    imageItems.forEach((item) => {
        const fileIndex = item.dataset.fileIndex;
        if (fileIndex !== null && window.newVariantFiles[fileIndex]) {
            validFiles.push(window.newVariantFiles[fileIndex]);
        }
    });

    if (validFiles.length === 0) {
        alert('Không có file ảnh hợp lệ để upload!');
        return;
    }

    const formData = new FormData();
    formData.append('variantId', variantId);

    // Thêm tất cả ảnh mới vào FormData
    validFiles.forEach((file) => {
        formData.append('images', file);
    });

    fetch('/api/product/variant/images', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'ok') {
                alert('Cập nhật biến thể thành công!');
                const modal = bootstrap.Modal.getInstance(document.getElementById('variantDetailModal'));
                modal.hide();
                location.reload(); // Reload để cập nhật danh sách
            } else {
                alert('Lỗi khi upload ảnh: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error uploading images:', error);
            alert('Lỗi khi upload ảnh');
        })
        .finally(() => {
            resetSaveButton(document.getElementById('saveVariantBtn'), '<i class="fas fa-save me-2"></i>Lưu');
        });
}

// Reset nút lưu
function resetSaveButton(button, originalText) {
    button.innerHTML = originalText;
    button.disabled = false;
}

// Xử lý nút cập nhật sản phẩm
document.addEventListener('DOMContentLoaded', function () {
    const updateBtn = document.getElementById('update-product-btn');
    if (updateBtn) {
        updateBtn.addEventListener('click', function () {
            // Cập nhật nội dung từ TinyMCE vào textarea trước khi submit
            if (typeof tinymce !== 'undefined') {
                if (tinymce.get('product-description-editor')) {
                    tinymce.get('product-description-editor').save();
                }
                if (tinymce.get('product-short-description-editor')) {
                    tinymce.get('product-short-description-editor').save();
                }
            }

            // Upload ảnh sản phẩm mới trước khi submit form
            uploadNewProductImages();
        });
    }

    // Xử lý nút áp dụng hàng loạt
    const applyBulkActionBtn = document.getElementById('apply-bulk-action');
    if (applyBulkActionBtn) {
        applyBulkActionBtn.addEventListener('click', function () {
            applyBulkAction();
        });
    }

    // Xử lý checkbox biến thể
    const variantCheckboxes = document.querySelectorAll('.variant-checkbox');
    variantCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', updateSelectedCount);
    });

    // Xử lý select all checkbox
    const selectAllCheckbox = document.getElementById('select-all-variants');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            const checkboxes = document.querySelectorAll('.variant-checkbox');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
            updateSelectedCount();
        });
    }
});

// Upload ảnh sản phẩm mới
function uploadNewProductImages() {
    const newImagesList = document.getElementById('newProductImagesList');
    const imageItems = newImagesList.querySelectorAll('.product-image-item[data-type="new"]');

    if (imageItems.length === 0) {
        // Không có ảnh mới, submit form bình thường
        document.getElementById('product-form').submit();
        return;
    }

    const productId = document.querySelector('input[name="productId"]').value;
    const formData = new FormData();
    formData.append('productId', productId);

    // Thêm ảnh mới vào FormData (tối đa 5 ảnh)
    if (window.newProductFiles && window.newProductFiles.length > 0) {
        window.newProductFiles.slice(0, MAX_PRODUCT_IMAGES).forEach(file => {
            formData.append('images', file);
        });
    }

    // Hiển thị loading
    const updateBtn = document.getElementById('update-product-btn');
    const originalText = updateBtn.innerHTML;
    updateBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang cập nhật...';
    updateBtn.disabled = true;

    fetch(`/api/product/${productId}/images`, {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'ok') {
                // Upload thành công, submit form
                document.getElementById('product-form').submit();
            } else {
                alert('Lỗi khi upload ảnh: ' + data.message);
                resetUpdateButton(updateBtn, originalText);
            }
        })
        .catch(error => {
            console.error('Error uploading product images:', error);
            alert('Lỗi khi upload ảnh sản phẩm');
            resetUpdateButton(updateBtn, originalText);
        });
}

// Tạo biến thể tự động
function generateVariants() {
    const productId = document.querySelector('input[name="productId"]').value;
    const btn = event.target;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang tạo...';
    btn.disabled = true;

    fetch(`/api/product/${productId}/generate-variants`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'ok') {
                if (!data.data || data.data.length === 0) {
                    alert('Tất cả combination đã tồn tại, không có biến thể mới nào được tạo!');
                } else {
                    alert('Tạo biến thể thành công!');
                    location.reload();
                }
            } else {
                alert('Lỗi khi tạo biến thể: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error generating variants:', error);
            alert('Lỗi khi tạo biến thể');
        })
        .finally(() => {
            btn.innerHTML = originalText;
            btn.disabled = false;
        });
}

// Áp dụng hàng loạt
function applyBulkAction() {
    const selectedVariants = document.querySelectorAll('.variant-checkbox:checked');
    const action = document.getElementById('bulk-action-select').value;

    if (selectedVariants.length === 0) {
        alert('Vui lòng chọn ít nhất một biến thể!');
        return;
    }

    if (!action) {
        alert('Vui lòng chọn hành động!');
        return;
    }

    const variantIds = Array.from(selectedVariants).map(checkbox => checkbox.value);

    switch (action) {
        case 'price':
            showBulkPriceModal(variantIds);
            break;
        case 'stock':
            showBulkStockModal(variantIds);
            break;
        case 'delete':
            confirmBulkDelete(variantIds);
            break;
        default:
            alert('Hành động không hợp lệ!');
    }
}

// Hiển thị modal chỉnh sửa giá hàng loạt
function showBulkPriceModal(variantIds) {
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.innerHTML = `
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Chỉnh sửa giá hàng loạt</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Giá mới</label>
                        <input type="number" class="form-control" id="bulk-price" min="0" step="1000" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="button" class="btn btn-primary" onclick='applyBulkPrice(${JSON.stringify(variantIds)})'>Áp dụng</button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();

    modal.addEventListener('hidden.bs.modal', function () {
        document.body.removeChild(modal);
    });
}

// Áp dụng chỉnh sửa giá hàng loạt
function applyBulkPrice(variantIds) {
    const price = document.getElementById('bulk-price').value;
    console.log('Showing bulk stock modal for variant IDs:', variantIds);
    console.log('Showing bulk stock modal for variant IDs:', JSON.stringify(variantIds));
    if (!price) {
        alert('Vui lòng nhập giá!');
        return;
    }

    const data = {
        variantIds: variantIds,
        price: price,
    };

    fetch('/api/product/variants/bulk-price', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(result => {
            if (result.status === 'ok') {
                alert('Cập nhật giá thành công!');
                location.reload();
            } else {
                alert('Lỗi khi cập nhật giá: ' + result.message);
            }
        })
        .catch(error => {
            console.error('Error updating bulk price:', error);
            alert('Lỗi khi cập nhật giá');
        });
}

// Hiển thị modal chỉnh sửa số lượng hàng loạt
function showBulkStockModal(variantIds) {
    const modal = document.createElement('div');
    console.log('Showing bulk stock modal for variant IDs:', variantIds);
    console.log('Showing bulk stock modal for variant IDs:', JSON.stringify(variantIds));

    modal.className = 'modal fade';
    modal.innerHTML = `
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Chỉnh sửa số lượng hàng loạt</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Số lượng mới</label>
                        <input type="number" class="form-control" placeholder="Nhập số lượng mới" id="bulk-stock" min="0" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="button" class="btn btn-primary" onclick='applyBulkStock(${JSON.stringify(variantIds)})'>Áp dụng</button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();

    modal.addEventListener('hidden.bs.modal', function () {
        document.body.removeChild(modal);
    });
}

// Áp dụng chỉnh sửa số lượng hàng loạt
function applyBulkStock(variantIds) {
    const stock = document.getElementById('bulk-stock').value;

    if (!stock) {
        alert('Vui lòng nhập số lượng!');
        return;
    }

    const data = {
        variantIds: variantIds,
        stock: stock,
    };

    fetch('/api/product/variants/bulk-stock', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(result => {
            if (result.status === 'ok') {
                alert('Cập nhật số lượng thành công!');
                location.reload();
            } else {
                alert('Lỗi khi cập nhật số lượng: ' + result.message);
            }
        })
        .catch(error => {
            console.error('Error updating bulk stock:', error);
            alert('Lỗi khi cập nhật số lượng');
        });
}

// Xác nhận xóa hàng loạt
function confirmBulkDelete(variantIds) {
    if (confirm(`Bạn có chắc muốn xóa ${variantIds.length} biến thể đã chọn?`)) {
        fetch('/api/product/variants/bulk-delete', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ variantIds: variantIds })
        })
            .then(response => response.json())
            .then(result => {
                if (result.status === 'ok') {
                    alert('Xóa biến thể thành công!');
                    location.reload();
                } else {
                    alert('Lỗi khi xóa biến thể: ' + result.message);
                }
            })
            .catch(error => {
                console.error('Error deleting variants:', error);
                alert('Lỗi khi xóa biến thể');
            });
    }
}

// Lưu biến thể (function placeholder)
function saveVariants() {
    alert('Chức năng lưu biến thể đã được xử lý tự động khi cập nhật từng biến thể!');
}

// Lưu thuộc tính (function placeholder)
function saveAttributes() {
    alert('Chức năng lưu thuộc tính sẽ được phát triển sau!');
}

// Xóa biến thể đơn lẻ
function deleteVariant(variantId) {
    if (confirm('Bạn có chắc muốn xóa biến thể này?')) {
        fetch(`/api/product/variant/${variantId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        })
            .then(response => response.json())
            .then(result => {
                if (result.status === 'ok') {
                    alert('Xóa biến thể thành công!');
                    location.reload();
                } else {
                    alert('Lỗi khi xóa biến thể: ' + result.message);
                }
            })
            .catch(error => {
                console.error('Error deleting variant:', error);
                alert('Lỗi khi xóa biến thể');
            });
    }
}

// Cập nhật số lượng biến thể đã chọn
function updateSelectedCount() {
    const selectedVariants = document.querySelectorAll('.variant-checkbox:checked');
    const count = selectedVariants.length;

    // Cập nhật text của nút áp dụng
    const applyBtn = document.getElementById('apply-bulk-action');
    if (applyBtn) {
        if (count > 0) {
            applyBtn.textContent = `Áp dụng (${count} biến thể)`;
            applyBtn.disabled = false;
        } else {
            applyBtn.textContent = 'Áp dụng';
            applyBtn.disabled = true;
        }
    }
}

// Kiểm tra trùng combination khi chọn màu/size
function onVariantSelectChange() {
    const colorId = document.getElementById('variantColor').value;
    const sizeId = document.getElementById('variantSize').value;
    const errorDiv = document.getElementById('variant-error');
    const saveBtn = document.getElementById('saveVariantBtn');

    if (colorId && sizeId) {
        const exists = existingVariants.some(v => v.colorId == colorId && v.sizeId == sizeId);
        if (exists) {
            errorDiv.textContent = 'Đã tồn tại biến thể với màu sắc và kích cỡ này!';
            saveBtn.disabled = true;
        } else {
            errorDiv.textContent = '';
            saveBtn.disabled = false;
        }
    } else {
        errorDiv.textContent = '';
        saveBtn.disabled = true;
    }
}

// Khởi tạo xử lý category tree
function initCategoryTree() {
    // Đếm số lượng categories đã chọn
    updateCategoryCount();

    // Thêm event listener cho tất cả checkboxes
    const categoryCheckboxes = document.querySelectorAll('.category-checkbox');
    categoryCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            updateCategoryCount();
            handleCategoryToggle(this);
        });
    });

    // Hiển thị các danh mục con nếu có category được chọn
    showSelectedCategoryChildren();
}

// Cập nhật số lượng categories đã chọn
function updateCategoryCount() {
    const checkedBoxes = document.querySelectorAll('.category-checkbox:checked');
    const countElement = document.getElementById('category-count');
    if (countElement) {
        countElement.textContent = checkedBoxes.length;
    }
}

// Xử lý toggle category
function handleCategoryToggle(checkbox) {
    const categoryId = checkbox.value;
    const childrenContainer = document.getElementById('children-' + categoryId);

    if (childrenContainer) {
        if (checkbox.checked) {
            childrenContainer.style.display = 'block';
        } else {
            childrenContainer.style.display = 'none';
            // Bỏ chọn tất cả category con
            const childCheckboxes = childrenContainer.querySelectorAll('.category-checkbox');
            childCheckboxes.forEach(child => {
                child.checked = false;
            });
        }
    }
}

// Hiển thị các danh mục con nếu có category được chọn
function showSelectedCategoryChildren() {
    const checkedBoxes = document.querySelectorAll('.category-checkbox:checked');
    checkedBoxes.forEach(checkbox => {
        const categoryId = checkbox.value;
        const childrenContainer = document.getElementById('children-' + categoryId);
        if (childrenContainer) {
            childrenContainer.style.display = 'block';
        }
    });
}

// Function để toggle hiển thị danh mục con (được gọi từ HTML)
function toggleChildren(element, childrenId) {
    const childrenElement = document.getElementById(childrenId);
    const icon = element.querySelector('i');

    if (childrenElement.style.display === 'none') {
        childrenElement.style.display = 'block';
        element.classList.remove('collapsed');
        icon.classList.remove('fa-chevron-right');
        icon.classList.add('fa-chevron-down');
    } else {
        childrenElement.style.display = 'none';
        element.classList.add('collapsed');
        icon.classList.remove('fa-chevron-down');
        icon.classList.add('fa-chevron-right');
    }
}

let existingVariants = [];
function getExistingVariants() {
    const productId = document.querySelector('input[name="productId"]').value;
    fetch(`http://localhost:8080/api/product/${productId}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === 'ok') {
                existingVariants = data.variants || [];
            }
        })
        .catch(error => {
            console.error('Error fetching existing variants:', error);
        });
}
getExistingVariants()
let allAttributes = {};

function getAllAttributes() {
    fetch('http://localhost:8080/api/attribute/get-all-attribute')
        .then(response => response.json())
        .then(data => {
            allAttributes = data.data || {};
        })
        .catch(error => {
            console.error('Error fetching all attributes:', error);
        });
}
getAllAttributes();
function getMissingVariants(allColors, allSizes, existingVariants) {
    const missing = [];
    allColors.forEach(color => {
        allSizes.forEach(size => {
            const exists = existingVariants.some(v => v.colorId === color.colorId && v.sizeId === size.sizeId);
            if (!exists) {
                missing.push({ color, size });
            }
            console.log('missing ', missing);

        });
    });
    return missing;
}

// Khi nhấn nút Thêm biến thể
const btnAddVariant = document.getElementById('btn-add-variant');
if (btnAddVariant) {
    btnAddVariant.addEventListener('click', function () {
        const missing = getMissingVariants(allAttributes.listColor, allAttributes.listSize, existingVariants);
        if (missing.length === 0) {
            alert('Đã tạo tất cả biến thể!');
            return;
        }
        console.log(missing);

        initSelectBoxes(missing);
        const modal = new bootstrap.Modal(document.getElementById('addVariantModal'));
        modal.show();
    });
}
function initSelectBoxes(missingVariantsData) {
    missingVariants = missingVariantsData;

    populateColorSelect(missingVariants);
    populateSizeSelect(missingVariants); // Gọi lúc chưa chọn gì

    // Gán sự kiện cho colorSelect
    document.getElementById('colorSelect').addEventListener('change', () => {
        const selectedColorId = document.getElementById('colorSelect').value;
        if (selectedColorId) {
            // Lọc size theo color
            const filtered = missingVariants.filter(v => v.color.colorId == selectedColorId);
            populateSizeSelect(filtered);
        } else {
            // Hiển thị toàn bộ kích cỡ
            populateSizeSelect(missingVariants);
        }
    });

    // Gán lại listener size nếu bạn cần làm gì đó khi chọn size (tuỳ ý)
}

function populateColorSelect(variants) {
    const colorSelect = document.getElementById('colorSelect');
    colorSelect.innerHTML = `<option value="">Chọn màu</option>`;

    const uniqueColors = new Map();
    variants.forEach(v => {
        uniqueColors.set(v.color.colorId, v.color.name);
    });

    uniqueColors.forEach((name, id) => {
        const option = document.createElement('option');
        option.value = id;
        option.textContent = name;
        colorSelect.appendChild(option);
    });
}

function populateSizeSelect(variants) {
    const sizeSelect = document.getElementById('sizeSelect');
    sizeSelect.innerHTML = `<option value="">Chọn kích cỡ</option>`;

    const uniqueSizes = new Map();
    variants.forEach(v => {
        uniqueSizes.set(v.size.sizeId, v.size.name);
    });

    uniqueSizes.forEach((name, id) => {
        const option = document.createElement('option');
        option.value = id;
        option.textContent = name;
        sizeSelect.appendChild(option);
    });
}

// Xử lý submit form thêm biến thể
const addVariantForm = document.getElementById('addVariantForm');
if (addVariantForm) {
    addVariantForm.addEventListener('submit', function (e) {
        e.preventDefault();
        const rows = document.querySelectorAll('#missingVariantsList .variant-row');
        const formDataList = [];
        let valid = true;
        rows.forEach(row => {
            const colorId = row.querySelector('input[name="colorId"]').value;
            const sizeId = row.querySelector('input[name="sizeId"]').value;
            const price = row.querySelector('input[name="price"]').value;
            const quantity = row.querySelector('input[name="quantity"]').value;
            const imageInput = row.querySelector('input[name="image"]');
            const imageFile = imageInput.files[0];
            if (!price || !quantity || !imageFile) {
                valid = false;
            }
            const formData = new FormData();
            formData.append('colorId', colorId);
            formData.append('sizeId', sizeId);
            formData.append('price', price);
            formData.append('quantityInStock', quantity);
            formData.append('images', imageFile);
            formDataList.push(formData);
        });
        if (!valid) {
            alert('Vui lòng nhập đủ thông tin và chọn ảnh cho tất cả biến thể!');
            return;
        }
        submitNewVariants(formDataList);
    });
}

// Gửi từng biến thể mới lên backend
function submitNewVariants(formDataList) {
    const productId = window.productId;
    let successCount = 0;
    let failCount = 0;
    function sendNext(index) {
        if (index >= formDataList.length) {
            alert(`Đã thêm ${successCount} biến thể thành công, ${failCount} thất bại.`);
            location.reload();
            return;
        }
        const formData = formDataList[index];
        fetch(`/api/product/${productId}/add-variant`, {
            method: 'POST',
            body: formData
        })
            .then(res => res.json())
            .then(data => {
                if (data.status === 'ok') {
                    successCount++;
                } else {
                    failCount++;
                }
                sendNext(index + 1);
            })
            .catch(() => {
                failCount++;
                sendNext(index + 1);
            });
    }
    sendNext(0);
}

// Lấy danh sách SKU hiện có để kiểm tra trùng
function getAllVariantSKUs() {
    // Giả sử biến existingVariants đã có trên trang (từ Thymeleaf hoặc fetch API)
    if (window.existingVariants && Array.isArray(window.existingVariants)) {
        return window.existingVariants.map(v => v.sku ? v.sku.toLowerCase() : '').filter(Boolean);
    }
    // Nếu chưa có, trả về mảng rỗng
    return [];
}

// Xử lý nút Thêm biến thể mới
document.querySelector('#addVariantBtn').addEventListener('click', async function () {
    // Lấy dữ liệu từ form
    const productId = document.querySelector('input[name="productId"]').value;
    const name = document.getElementById('variantName2').value.trim();
    const sku = document.getElementById('variantSku2').value.trim();
    const barcode = document.getElementById('variantBarcode2').value.trim();
    const colorId = document.getElementById('colorSelect').value;
    const sizeId = document.getElementById('sizeSelect').value;
    const price = document.getElementById('variantPrice2').value;
    const discountPrice = document.getElementById('variantDiscountPrice2').value;
    const discountStart = document.getElementById('variantDiscountStart2').value;
    const discountEnd = document.getElementById('variantDiscountEnd2').value;
    const quantity = document.getElementById('variantQuantity2').value;
    const isActive = document.getElementById('variantIsActive2').value;
    const files = window.newVariantFiles2 || [];
    const errorDiv = document.querySelector('#addVariantModal #variant-error');
    if (errorDiv) errorDiv.textContent = '';

    // Validate
    let error = '';
    if (!name || !sku || !colorId || !sizeId || !price || !quantity || files.length === 0) {
        error = 'Vui lòng nhập đầy đủ thông tin và chọn ít nhất 1 ảnh!';
    } else if (isNaN(price) || Number(price) <= 0) {
        error = 'Giá phải là số lớn hơn 0!';
    } else if (discountPrice && (isNaN(discountPrice) || Number(discountPrice) < 0)) {
        error = 'Giá khuyến mãi phải là số không âm!';
    } else if (isNaN(quantity) || Number(quantity) < 0) {
        error = 'Tồn kho phải là số không âm!';
    } else if (files.length > 5) {
        error = 'Chỉ được chọn tối đa 5 ảnh!';
    } else {
        // Kiểm tra định dạng ảnh
        for (let file of files) {
            const ext = file.name.split('.').pop().toLowerCase();
            if (!['jpg', 'jpeg', 'png', 'gif'].includes(ext)) {
                error = 'Chỉ chấp nhận ảnh JPG, PNG, GIF!';
                break;
            }
        }
    }
    // Kiểm tra trùng SKU
    const allSKUs = getAllVariantSKUs();
    if (allSKUs.includes(sku.toLowerCase())) {
        error = 'SKU đã tồn tại, vui lòng nhập SKU khác!';
    }
    if (error) {
        console.log(error);

        if (errorDiv) errorDiv.textContent = error;
        else alert(error);
        return;
    }

    // Chuẩn bị dữ liệu gửi đi
    const formData = new FormData();
    formData.append('productId', productId);
    formData.append('name', name);
    formData.append('sku', sku);
    formData.append('barcode', barcode);
    formData.append('colorId', colorId);
    formData.append('sizeId', sizeId);
    formData.append('price', price);
    formData.append('discountPrice', discountPrice);
    formData.append('discountStart', discountStart);
    formData.append('discountEnd', discountEnd);
    formData.append('quantityInStock', quantity);
    formData.append('isActive', isActive);

    // Hiển thị loading
    const updateBtn = document.getElementById('addVariantBtn');
    const originalText = updateBtn.innerHTML;
    updateBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang cập nhật...';
    updateBtn.disabled = true;
    // Gửi lên backend
    try {
        const res = await fetch('/api/product/variant/add', {
            method: 'POST',
            body: formData
        });
        const data = await res.json();

        if (data.status === "ok" && data.data && data.data.variantId) {
            if (files.length > 0) {
                const imgForm = new FormData();
                imgForm.append('variantId', data.data.variantId);
                files.forEach(file => imgForm.append('images', file));

                const res2 = await fetch('/api/product/variant/images', {
                    method: 'POST',
                    body: imgForm
                });
                const imgData = await res2.json();

                if (imgData.status === 'ok') {
                    alert('Thêm biến thể thành công!');
                    location.reload();
                } else {
                    alert('Thêm ảnh thất bại!');
                    resetSaveButton(updateBtn, originalText);
                }
            } else {
                alert('Thêm biến thể thành công!');
                location.reload();
            }
        }
    } catch (error) {
        if (errorDiv) errorDiv.textContent = error.message;
        else alert('Có lỗi xảy ra khi thêm biến thể!');
        resetSaveButton(updateBtn, originalText);
    } finally {
        resetSaveButton(updateBtn, '<i class="fas fa-save me-2"></i>Lưu');
    }
});

// ========== Render danh sách biến thể động qua API ========== //

function fetchAndRenderVariants() {
    const productId = document.querySelector('input[name="productId"]').value;
    const tbody = document.getElementById('variantsList');
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Đang tải...</td></tr>';
    fetch(`/api/product/${productId}`)
        .then(res => res.json())
        .then(data => {
            if (data.status === 'ok' && data.data.variants) {
                if (data.data.variants.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Chưa có biến thể nào</td></tr>';
                    return;
                }
                tbody.innerHTML = '';
                data.data.variants.forEach(variant => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>
                            <div class="form-check">
                                <input class="form-check-input variant-checkbox" type="checkbox" value="${variant.variantId}">
                            </div>
                        </td>
                        <td>${variant.name || ''}</td>
                        <td>${variant.price != null ? Number(variant.price).toLocaleString('vi-VN') : ''}</td>
                        <td>${variant.quantityInStock != null ? variant.quantityInStock : ''}</td>
                        <td>
                            ${variant.isActive ? '<span class="badge bg-success">Đang kích hoạt</span>' : '<span class="badge bg-secondary">Không kích hoạt</span>'}
                        </td>
                        <td>
                            <button type="button" class="btn btn-sm btn-primary btn-edit-variant" data-variant-id="${variant.variantId}" data-product-id="${productId}" data-bs-toggle="modal" data-bs-target="#variantDetailModal">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button type="button" class="btn btn-sm btn-danger btn-delete-variant" data-variant-id="${variant.variantId}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </td>
                    `;
                    tbody.appendChild(tr);
                });
                attachVariantRowEvents();
            } else {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Lỗi tải biến thể</td></tr>';
            }
        })
        .catch(() => {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Lỗi kết nối server</td></tr>';
        });
}

function attachVariantRowEvents() {
    // Sửa biến thể
    document.querySelectorAll('.btn-edit-variant').forEach(button => {
        button.addEventListener('click', function () {
            const variantId = this.dataset.variantId;
            const productId = this.dataset.productId;
            // Reset form và ảnh
            resetVariantFormGeneric('variant');
            // Gọi API để lấy thông tin biến thể
            fetch(`/api/product/variant/${variantId}`)
                .then(response => response.json())
                .then(json => {
                    if (json.status === 'ok' && json.data && json.data.variant) {
                        const v = json.data.variant;
                        // Fill các trường cơ bản
                        document.getElementById('variantId').value = v.variantId || '';
                        document.getElementById('variantName').value = v.name || '';
                        document.getElementById('variantSku').value = v.sku || '';
                        document.getElementById('variantBarcode').value = v.barcode || '';
                        document.getElementById('variantPrice').value = v.price || '';
                        document.getElementById('variantDiscountPrice').value = v.discountPrice || '';
                        document.getElementById('variantDiscountStart').value = v.discountPriceStartAt ? v.discountPriceStartAt.substring(0, 16) : '';
                        document.getElementById('variantDiscountEnd').value = v.discountPriceEndAt ? v.discountPriceEndAt.substring(0, 16) : '';
                        document.getElementById('variantQuantity').value = v.quantityInStock || '';
                        document.getElementById('variantIsActive').value = v.isActive ? 'true' : 'false';
                        if (document.getElementById('variantColor'))
                            document.getElementById('variantColor').value = v.colorName || '';
                        if (document.getElementById('variantSize'))
                            document.getElementById('variantSize').value = v.sizeName || '';
                        displayExistingImages(json.data.images);
                    } else {
                        alert(json.message || 'Không lấy được dữ liệu biến thể!');
                    }
                })
                .catch(error => console.error('Error fetching variant:', error));
        });
    });
    // Xóa biến thể
    document.querySelectorAll('.btn-delete-variant').forEach(button => {
        button.addEventListener('click', function () {
            const variantId = this.dataset.variantId;
            if (confirm('Bạn có chắc muốn xóa biến thể này?')) {
                fetch(`/api/product/variant/${variantId}`, {
                    method: 'DELETE',
                    headers: { 'Content-Type': 'application/json' }
                })
                    .then(response => response.json())
                    .then(result => {
                        if (result.status === 'ok') {
                            alert('Xóa biến thể thành công!');
                            fetchAndRenderVariants();
                        } else {
                            alert('Lỗi khi xóa biến thể: ' + result.message);
                        }
                    })
                    .catch(error => {
                        console.error('Error deleting variant:', error);
                        alert('Lỗi khi xóa biến thể');
                    });
            }
        });
    });
    // Gắn lại các event khác nếu cần (checkbox, bulk action...)
    // ...
}

// Gọi hàm này khi trang load và sau khi thêm/sửa/xóa biến thể

document.addEventListener('DOMContentLoaded', function () {
    fetchAndRenderVariants();
});