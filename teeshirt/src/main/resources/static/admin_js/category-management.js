// Category Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Add Category Form
    const addCategoryForm = document.getElementById('addCategoryForm');
    if (addCategoryForm) {
        addCategoryForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addCategory();
        });
    }

    // Edit Category Form
    const editCategoryForm = document.getElementById('editCategoryForm');
    if (editCategoryForm) {
        editCategoryForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateCategory();
        });
    }
});

function showEditCategoryModal(button) {
    const id = button.getAttribute('data-id');
    const name = button.getAttribute('data-name');
    const parentId = button.getAttribute('data-parent');
    
    document.getElementById('editCategoryId').value = id;
    document.getElementById('editCategoryName').value = name;
    document.getElementById('editCategoryParent').value = parentId || '';
    
    // Clear previous errors
    clearCategoryErrors();
    
    const modal = new bootstrap.Modal(document.getElementById('editCategoryModal'));
    modal.show();
}

function addCategory() {
    const name = document.getElementById('addCategoryName').value;
    const parentId = document.getElementById('addCategoryParent').value;

    console.log('name: ', name, ', parentId: ', parentId);
    
    
    // Clear previous errors
    clearCategoryErrors();
    
    // Validation
    if (name.length === 0) {
        showCategoryError('addCategoryName', 'Tên danh mục không được để trống');
        return;
    }
    
    if (name.length > 100) {
        showCategoryError('addCategoryName', 'Tên danh mục tối đa 100 ký tự');
        return;
    }

    const formData = {
        name: name,
        parentId: parentId || null
    };
    console.log('form data', formData);
    

    fetch('/admin/product/category/api/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('success', data.message);
            document.getElementById('addCategoryModal').querySelector('.btn-close').click();
            document.getElementById('addCategoryForm').reset();
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
            if (data.errors) {
                console.log(data.errors);
                
                data.errors.forEach(error => {
                    if (error.field === 'name') {
                        showCategoryError('addCategoryName', error.defaultMessage);
                    }
                });
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi thêm danh mục');
    });
}

function updateCategory() {
    const id = document.getElementById('editCategoryId').value;
    const name = document.getElementById('editCategoryName').value.trim();
    const parentId = document.getElementById('editCategoryParent').value;
    
    // Clear previous errors
    clearCategoryErrors();
    
    // Validation
    if (!name) {
        showCategoryError('editCategoryName', 'Tên danh mục không được để trống');
        return;
    }
    
    if (name.length > 100) {
        showCategoryError('editCategoryName', 'Tên danh mục tối đa 100 ký tự');
        return;
    }

    const formData = {
        name: name,
        parentId: parentId || null
    };

    fetch(`/admin/product/category/api/update/${id}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('success', data.message);
            document.getElementById('editCategoryModal').querySelector('.btn-close').click();
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
            if (data.errors) {
                data.errors.forEach(error => {
                    if (error.field === 'name') {
                        showCategoryError('editCategoryName', error.defaultMessage);
                    }
                });
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi cập nhật danh mục');
    });
}

function deleteCategory(button) {
    if (!confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
        return;
    }
    
    const id = button.getAttribute('data-id');

    fetch(`/admin/product/category/api/delete/${id}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('success', data.message);
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi xóa danh mục');
    });
}

function clearCategoryErrors() {
    document.getElementById('addCategoryName').classList.remove('is-invalid');
    document.getElementById('editCategoryName').classList.remove('is-invalid');
    document.getElementById('addCategoryNameError').textContent = '';
    document.getElementById('editCategoryNameError').textContent = '';
}

function showCategoryError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const errorDiv = document.getElementById(fieldId + 'Error');
    
    field.classList.add('is-invalid');
    errorDiv.textContent = message;
}

function showAlert(type, message) {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());
    
    // Create new alert
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    // Insert at the top of the main content
    const mainContent = document.querySelector('main');
    mainContent.insertBefore(alertDiv, mainContent.firstChild);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
} 