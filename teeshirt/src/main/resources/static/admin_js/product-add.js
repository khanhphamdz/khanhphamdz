// product-add.js

document.addEventListener('DOMContentLoaded', function () {
    // Cấu hình TinyMCE cho mô tả chi tiết
    tinymce.init({
        selector: '#product-description',
        height: 600,
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
        ai_request: (request, respondWith) => respondWith.string(() => Promise.reject("See docs to implement AI Assistant")),
        setup: function (editor) {
            editor.on('init', function () {
                // Dữ liệu đã được load tự động từ th:value
            });
        }
    });

    // Cấu hình TinyMCE cho mô tả ngắn
    tinymce.init({
        selector: '#product-short-description',
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
        setup: function (editor) {
            editor.on('init', function () {
                // Dữ liệu đã được load tự động từ th:value
            });
        }
    });

    // Preview ảnh thumbnail
    const thumbnailInput = document.getElementById('product-thumbnail');
    const previewDiv = document.getElementById('thumbnail-preview');
    if (thumbnailInput) {
        thumbnailInput.addEventListener('change', function (e) {
            const file = e.target.files[0];
            if (file) {
                // Kiểm tra loại file
                if (!file.type.startsWith('image/')) {
                    alert('Vui lòng chọn file ảnh hợp lệ!');
                    this.value = '';
                    previewDiv.innerHTML = '';
                    return;
                }

                // Kiểm tra kích thước file (5MB)
                if (file.size > 5 * 1024 * 1024) {
                    alert('File ảnh quá lớn! Kích thước tối đa: 5MB');
                    this.value = '';
                    previewDiv.innerHTML = '';
                    return;
                }

                // Hiển thị preview
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewDiv.innerHTML = `
                        <img src="${e.target.result}" alt="Preview" style="max-width: 200px; max-height: 200px; border-radius: 5px;">
                    `;
                };
                reader.readAsDataURL(file);
            } else {
                previewDiv.innerHTML = '';
            }
        });
    }

    // Validate form 
    const form = document.getElementById('add-product-form');
    if (form) {
        form.addEventListener('submit', function (e) {
            const name = document.getElementById('product-name').value.trim();
            const basePrice = document.getElementById('product-base-price').value;
            const materialId = document.getElementById('product-material').value;
            const thumbnail = thumbnailInput.files[0];

            let isValid = true;
            let errorMessage = '';

            // Kiểm tra tên sản phẩm
            if (name.length < 3) {
                errorMessage += '- Tên sản phẩm phải có ít nhất 3 ký tự\n';
                isValid = false;
            }

            // Kiểm tra giá
            if (!basePrice || parseFloat(basePrice) <= 0) {
                errorMessage += '- Giá sản phẩm phải lớn hơn 0\n';
                isValid = false;
            }

            // Kiểm tra chất liệu
            if (!materialId) {
                errorMessage += '- Vui lòng chọn chất liệu\n';
                isValid = false;
            }

            // Kiểm tra ảnh thumbnail
            if (!thumbnail) {
                errorMessage += '- Vui lòng chọn ảnh thumbnail\n';
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
                alert('Vui lòng sửa các lỗi sau:\n' + errorMessage);
                return false;
            }

            // Hiển thị loading
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';
            }

            // Lưu nội dung TinyMCE
            if (typeof tinymce !== 'undefined') {
                if (tinymce.get('product-description')) {
                    tinymce.get('product-description').save();
                }
                if (tinymce.get('product-short-description')) {
                    tinymce.get('product-short-description').save();
                }
            }
        });
    }

    // Xử lý category tree
    const categoryCheckboxes = document.querySelectorAll('.category-checkbox');
    categoryCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const categoryId = this.value;
            const childrenContainer = document.getElementById('children-' + categoryId);
            
            if (childrenContainer) {
                if (this.checked) {
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
        });
    });

    // Function để toggle hiển thị danh mục con
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
}); 