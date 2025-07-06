// Attribute Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // ========== SIZE MANAGEMENT ==========
    
    // Add Size Form
    const addSizeForm = document.getElementById('addSizeForm');
    if (addSizeForm) {
        addSizeForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addSize();
        });
    }

    // Edit Size Form
    const editSizeForm = document.getElementById('editSizeForm');
    if (editSizeForm) {
        editSizeForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateSize();
        });
    }

    // ========== COLOR MANAGEMENT ==========
    
    // Add Color Form
    const addColorForm = document.getElementById('addColorForm');
    if (addColorForm) {
        addColorForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addColor();
        });
    }

    // Edit Color Form
    const editColorForm = document.getElementById('editColorForm');
    if (editColorForm) {
        editColorForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateColor();
        });
    }

    // ========== MATERIAL MANAGEMENT ==========
    
    // Add Material Form
    const addMaterialForm = document.getElementById('addMaterialForm');
    if (addMaterialForm) {
        addMaterialForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addMaterial();
        });
    }

    // Edit Material Form
    const editMaterialForm = document.getElementById('editMaterialForm');
    if (editMaterialForm) {
        editMaterialForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateMaterial();
        });
    }
});

// ========== SIZE FUNCTIONS ==========

function showEditSizeModal(button) {
    const id = button.getAttribute('data-id');
    const name = button.getAttribute('data-name');
    
    document.getElementById('editSizeId').value = id;
    document.getElementById('editSizeName').value = name;
    
    // Clear previous errors
    clearSizeErrors();
    
    const modal = new bootstrap.Modal(document.getElementById('editSizeModal'));
    modal.show();
}

function addSize() {
    const name = document.getElementById('addSizeName').value.trim();
    
    // Clear previous errors
    clearSizeErrors();
    
    // Validation
    if (!name) {
        showSizeError('addSizeName', 'Tên kích cỡ không được để trống');
        return;
    }
    
    if (name.length > 10) {
        showSizeError('addSizeName', 'Tên kích cỡ tối đa 10 ký tự');
        return;
    }

    const formData = {
        name: name
    };

    fetch('/admin/product/attribute/size/add', {
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
            document.getElementById('addSizeModal').querySelector('.btn-close').click();
            document.getElementById('addSizeForm').reset();
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
            if (data.errors) {
                data.errors.forEach(error => {
                    showSizeError('addSizeName', error.defaultMessage);
                });
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi thêm kích cỡ');
    });
}

function updateSize() {
    const id = document.getElementById('editSizeId').value;
    const name = document.getElementById('editSizeName').value.trim();
    
    // Clear previous errors
    clearSizeErrors();
    
    // Validation
    if (!name) {
        showSizeError('editSizeName', 'Tên kích cỡ không được để trống');
        return;
    }
    
    if (name.length > 10) {
        showSizeError('editSizeName', 'Tên kích cỡ tối đa 10 ký tự');
        return;
    }

    const formData = {
        name: name
    };

    fetch(`/admin/product/attribute/size/update/${id}`, {
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
            document.getElementById('editSizeModal').querySelector('.btn-close').click();
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
            if (data.errors) {
                data.errors.forEach(error => {
                    showSizeError('editSizeName', error.defaultMessage);
                });
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi cập nhật kích cỡ');
    });
}

function deleteSize(button) {
    if (!confirm('Bạn có chắc chắn muốn xóa kích cỡ này?')) {
        return;
    }
    
    const id = button.getAttribute('data-id');

    fetch(`/admin/product/attribute/size/delete/${id}`, {
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
        showAlert('error', 'Có lỗi xảy ra khi xóa kích cỡ');
    });
}

function clearSizeErrors() {
    document.getElementById('addSizeName').classList.remove('is-invalid');
    document.getElementById('editSizeName').classList.remove('is-invalid');
    document.getElementById('addSizeNameError').textContent = '';
    document.getElementById('editSizeNameError').textContent = '';
}

function showSizeError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const errorDiv = document.getElementById(fieldId + 'Error');
    
    field.classList.add('is-invalid');
    errorDiv.textContent = message;
}

// ========== COLOR FUNCTIONS ==========

function showEditColorModal(button) {
    const id = button.getAttribute('data-id');
    const name = button.getAttribute('data-name');
    const hex = button.getAttribute('data-hex');
    
    document.getElementById('editColorId').value = id;
    document.getElementById('editColorName').value = name;
    document.getElementById('editColorHex').value = hex;
    
    // Clear previous errors
    clearColorErrors();
    
    const modal = new bootstrap.Modal(document.getElementById('editColorModal'));
    modal.show();
}

function addColor() {
    const name = document.getElementById('addColorName').value.trim();
    const hexCode = document.getElementById('addColorHex').value;
    
    // Clear previous errors
    clearColorErrors();
    
    // Validation
    if (!name) {
        showColorError('addColorName', 'Tên màu sắc không được để trống');
        return;
    }
    
    if (name.length > 50) {
        showColorError('addColorName', 'Tên màu sắc tối đa 50 ký tự');
        return;
    }
    
    if (!hexCode) {
        showColorError('addColorHex', 'Mã màu không được để trống');
        return;
    }
    
    if (!/^#[0-9A-Fa-f]{6}$/.test(hexCode)) {
        showColorError('addColorHex', 'Mã màu phải có định dạng #RRGGBB');
        return;
    }

    const formData = {
        name: name,
        hexCode: hexCode
    };

    fetch('/admin/product/attribute/color/add', {
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
            document.getElementById('addColorModal').querySelector('.btn-close').click();
            document.getElementById('addColorForm').reset();
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
            if (data.errors) {
                data.errors.forEach(error => {
                    if (error.field === 'name') {
                        showColorError('addColorName', error.defaultMessage);
                    } else if (error.field === 'hexCode') {
                        showColorError('addColorHex', error.defaultMessage);
                    }
                });
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi thêm màu sắc');
    });
}

function updateColor() {
    const id = document.getElementById('editColorId').value;
    const name = document.getElementById('editColorName').value.trim();
    const hexCode = document.getElementById('editColorHex').value;
    
    // Clear previous errors
    clearColorErrors();
    
    // Validation
    if (!name) {
        showColorError('editColorName', 'Tên màu sắc không được để trống');
        return;
    }
    
    if (name.length > 50) {
        showColorError('editColorName', 'Tên màu sắc tối đa 50 ký tự');
        return;
    }
    
    if (!hexCode) {
        showColorError('editColorHex', 'Mã màu không được để trống');
        return;
    }
    
    if (!/^#[0-9A-Fa-f]{6}$/.test(hexCode)) {
        showColorError('editColorHex', 'Mã màu phải có định dạng #RRGGBB');
        return;
    }

    const formData = {
        name: name,
        hexCode: hexCode
    };

    fetch(`/admin/product/attribute/color/update/${id}`, {
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
            document.getElementById('editColorModal').querySelector('.btn-close').click();
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
            if (data.errors) {
                data.errors.forEach(error => {
                    if (error.field === 'name') {
                        showColorError('editColorName', error.defaultMessage);
                    } else if (error.field === 'hexCode') {
                        showColorError('editColorHex', error.defaultMessage);
                    }
                });
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi cập nhật màu sắc');
    });
}

function deleteColor(button) {
    if (!confirm('Bạn có chắc chắn muốn xóa màu sắc này?')) {
        return;
    }
    
    const id = button.getAttribute('data-id');

    fetch(`/admin/product/attribute/color/delete/${id}`, {
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
        showAlert('error', 'Có lỗi xảy ra khi xóa màu sắc');
    });
}

function clearColorErrors() {
    document.getElementById('addColorName').classList.remove('is-invalid');
    document.getElementById('addColorHex').classList.remove('is-invalid');
    document.getElementById('editColorName').classList.remove('is-invalid');
    document.getElementById('editColorHex').classList.remove('is-invalid');
    document.getElementById('addColorNameError').textContent = '';
    document.getElementById('addColorHexError').textContent = '';
    document.getElementById('editColorNameError').textContent = '';
    document.getElementById('editColorHexError').textContent = '';
}

function showColorError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const errorDiv = document.getElementById(fieldId + 'Error');
    
    field.classList.add('is-invalid');
    errorDiv.textContent = message;
}

// ========== MATERIAL FUNCTIONS ==========

function showEditMaterialModal(button) {
    const id = button.getAttribute('data-id');
    const name = button.getAttribute('data-name');
    
    document.getElementById('editMaterialId').value = id;
    document.getElementById('editMaterialName').value = name;
    
    // Clear previous errors
    clearMaterialErrors();
    
    const modal = new bootstrap.Modal(document.getElementById('editMaterialModal'));
    modal.show();
}

function addMaterial() {
    const name = document.getElementById('addMaterialName').value.trim();
    
    // Clear previous errors
    clearMaterialErrors();
    
    // Validation
    if (!name) {
        showMaterialError('addMaterialName', 'Tên chất liệu không được để trống');
        return;
    }
    
    if (name.length > 50) {
        showMaterialError('addMaterialName', 'Tên chất liệu tối đa 50 ký tự');
        return;
    }

    const formData = {
        name: name
    };

    fetch('/admin/product/attribute/material/add', {
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
            document.getElementById('addMaterialModal').querySelector('.btn-close').click();
            document.getElementById('addMaterialForm').reset();
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
            if (data.errors) {
                data.errors.forEach(error => {
                    showMaterialError('addMaterialName', error.defaultMessage);
                });
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi thêm chất liệu');
    });
}

function updateMaterial() {
    const id = document.getElementById('editMaterialId').value;
    const name = document.getElementById('editMaterialName').value.trim();
    
    // Clear previous errors
    clearMaterialErrors();
    
    // Validation
    if (!name) {
        showMaterialError('editMaterialName', 'Tên chất liệu không được để trống');
        return;
    }
    
    if (name.length > 50) {
        showMaterialError('editMaterialName', 'Tên chất liệu tối đa 50 ký tự');
        return;
    }

    const formData = {
        name: name
    };

    fetch(`/admin/product/attribute/material/update/${id}`, {
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
            document.getElementById('editMaterialModal').querySelector('.btn-close').click();
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showAlert('error', data.message);
            if (data.errors) {
                data.errors.forEach(error => {
                    showMaterialError('editMaterialName', error.defaultMessage);
                });
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('error', 'Có lỗi xảy ra khi cập nhật chất liệu');
    });
}

function deleteMaterial(button) {
    if (!confirm('Bạn có chắc chắn muốn xóa chất liệu này?')) {
        return;
    }
    
    const id = button.getAttribute('data-id');

    fetch(`/admin/product/attribute/material/delete/${id}`, {
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
        showAlert('error', 'Có lỗi xảy ra khi xóa chất liệu');
    });
}

function clearMaterialErrors() {
    document.getElementById('addMaterialName').classList.remove('is-invalid');
    document.getElementById('editMaterialName').classList.remove('is-invalid');
    document.getElementById('addMaterialNameError').textContent = '';
    document.getElementById('editMaterialNameError').textContent = '';
}

function showMaterialError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const errorDiv = document.getElementById(fieldId + 'Error');
    
    field.classList.add('is-invalid');
    errorDiv.textContent = message;
}

// ========== UTILITY FUNCTIONS ==========

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