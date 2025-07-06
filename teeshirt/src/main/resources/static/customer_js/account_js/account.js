
document.addEventListener('DOMContentLoaded', function () {
    // ========== KHÔI PHỤC TAB SAU KHI RELOAD ==========
    const activeTab = localStorage.getItem('activeTab');
    if (activeTab === 'address-book') {
        const addressBookTab = document.getElementById('v-pills-address-tab');
        if (addressBookTab) {
            const tabInstance = new bootstrap.Tab(addressBookTab);
            tabInstance.show();
        }
        localStorage.removeItem('activeTab');
    }
    
    // ========== ĐỊA CHỈ MẶC ĐỊNH ==========
    function updateDefaultDisplay() {
        const defaultAddressId = localStorage.getItem('defaultAddressId');
        
        // Reset tất cả badge và radio
        document.querySelectorAll('.default-badge').forEach(badge => badge.style.display = 'none');
        document.querySelectorAll('.default-address-radio').forEach(radio => radio.checked = false);
        
        // Hiển thị địa chỉ mặc định
        if (defaultAddressId) {
            const defaultRadio = document.querySelector(`input[data-address-id="${defaultAddressId}"]`);
            if (defaultRadio) {
                defaultRadio.checked = true;
                const badge = defaultRadio.closest('.address-item')?.querySelector('.default-badge');
                if (badge) badge.style.display = 'inline-block';
            }
        }
    }
    
    document.querySelectorAll('.default-address-radio').forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                const addressId = this.getAttribute('data-address-id');
                localStorage.setItem('defaultAddressId', addressId);
                updateDefaultDisplay();
            }
        });
    });
    
    updateDefaultDisplay();
    
    // ========== API TỈNH/PHƯỜNG/XÃ ==========
    function createOption(value, text) {
        const option = document.createElement('option');
        option.value = value;
        option.textContent = text;
        return option;
    }

    function loadProvinces(selectElement) {
        fetch('https://provinces.open-api.vn/api/p/')
            .then(res => res.json())
            .then(data => {
                selectElement.innerHTML = '<option value="">Chọn tỉnh/thành phố</option>';
                data.forEach(province => {
                    selectElement.appendChild(createOption(province.code, province.name));
                });
            })
            .catch(error => {
                console.error('Error loading provinces:', error);
                selectElement.innerHTML = '<option value="">Không thể tải danh sách tỉnh/thành phố</option>';
            });
    }
    
    function loadDistricts(provinceCode, selectElement) {
        selectElement.innerHTML = '<option value="">Chọn quận/huyện</option>';
        if (!provinceCode) return;
        
        fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`)
            .then(res => res.json())
            .then(data => {
                if (data.districts && Array.isArray(data.districts)) {
                    data.districts.forEach(district => {
                        selectElement.appendChild(createOption(district.code, district.name));
                    });
                }
            })
            .catch(error => {
                console.error('Error loading districts:', error);
                selectElement.innerHTML = '<option value="">Không thể tải danh sách quận/huyện</option>';
            });
    }
    
    function loadWards(districtCode, selectElement) {
        selectElement.innerHTML = '<option value="">Chọn phường/xã</option>';
        if (!districtCode) return;
        
        fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`)
            .then(res => res.json())
            .then(data => {
                if (data.wards && Array.isArray(data.wards)) {
                    data.wards.forEach(ward => {
                        selectElement.appendChild(createOption(ward.code, ward.name));
                    });
                }
            })
            .catch(error => {
                console.error('Error loading wards:', error);
                selectElement.innerHTML = '<option value="">Không thể tải danh sách phường/xã</option>';
            });
    }
    
    // ========== MODAL SETUP ==========
    const addAddressModal = document.getElementById('addAddressModal');
    const editAddressModal = document.getElementById('editAddressModal');
    
    if (addAddressModal) {
        addAddressModal.addEventListener('show.bs.modal', function() {
            loadProvinces(document.getElementById('addProvinceSelect'));
        });
    }
    
    if (editAddressModal) {
        editAddressModal.addEventListener('show.bs.modal', function() {
            loadProvinces(document.getElementById('editProvinceSelect'));
        });
    }
    
    // Province/District/Ward change handlers
    ['add', 'edit'].forEach(prefix => {
        const provinceSelect = document.getElementById(`${prefix}ProvinceSelect`);
        const districtSelect = document.getElementById(`${prefix}DistrictSelect`);
        const wardSelect = document.getElementById(`${prefix}WardSelect`);
        
        if (provinceSelect) {
            provinceSelect.addEventListener('change', function() {
                loadDistricts(this.value, districtSelect);
                wardSelect.innerHTML = '<option value="">Chọn phường/xã</option>';
            });
        }
        
        if (districtSelect) {
            districtSelect.addEventListener('change', function() {
                loadWards(this.value, wardSelect);
            });
        }
    });
    
    // ========== FORM SUBMISSIONS ==========
    function handleFormSubmission(url, method, formData, successMessage) {
        return fetch(url, {
            method: method,
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message || successMessage);
                localStorage.setItem('activeTab', 'address-book');
                location.reload();
            } else {
                alert('Lỗi: ' + (data.error || 'Có lỗi xảy ra'));
            }
        });
    }

    const addAddressForm = document.getElementById('addAddressForm');
    if (addAddressForm) {
        addAddressForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            
            handleFormSubmission('/account/address/add', 'POST', formData, 'Thêm địa chỉ thành công!')
                .catch(error => {
                    console.error('Error:', error);
                    alert('Có lỗi xảy ra khi thêm địa chỉ. Vui lòng thử lại.');
                });
        });
    }

    const editAddressForm = document.getElementById('editAddressForm');
    if (editAddressForm) {
        editAddressForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const addressId = document.getElementById('editAddressId').value;
            const formData = new FormData(this);
            
            handleFormSubmission(`/account/address/${addressId}`, 'PUT', formData, 'Cập nhật địa chỉ thành công!')
                .catch(error => {
                    console.error('Error:', error);
                    alert('Có lỗi xảy ra khi cập nhật địa chỉ. Vui lòng thử lại.');
                });
        });
    }
    
    // ========== EVENT DELEGATION ==========
    document.addEventListener('click', function(e) {
        // Xử lý nút sửa địa chỉ
        if (e.target.classList.contains('edit-address-btn')) {
            const button = e.target;
            const addressId = button.getAttribute('data-id');
            const name = button.getAttribute('data-name') || '';
            const phone = button.getAttribute('data-phone') || '';
            const provinceId = button.getAttribute('data-province') || '';
            const districtId = button.getAttribute('data-district') || '';
            const wardId = button.getAttribute('data-ward') || '';
            const specificAddress = button.getAttribute('data-specific') || '';

            document.getElementById('editAddressId').value = addressId;
            document.getElementById('editInputName').value = name;
            document.getElementById('editInputPhone').value = phone;
            document.getElementById('editInputSpecificAddress').value = specificAddress;
            
            // Set giá trị cho các select sau khi load
            setTimeout(() => {
                if (provinceId) {
                    document.getElementById('editProvinceSelect').value = provinceId;
                    loadDistricts(provinceId, document.getElementById('editDistrictSelect'));
                    
                    setTimeout(() => {
                        if (districtId) {
                            document.getElementById('editDistrictSelect').value = districtId;
                            loadWards(districtId, document.getElementById('editWardSelect'));
                            
                            setTimeout(() => {
                                if (wardId) {
                                    document.getElementById('editWardSelect').value = wardId;
                                }
                            }, 500);
                        }
                    }, 500);
                }
            }, 500);
        }
        
        // Xử lý nút xóa địa chỉ
        if (e.target.classList.contains('delete-address-btn')) {
            const button = e.target;
            const addressId = button.getAttribute('data-id');
            
            if (confirm('Bạn có chắc chắn muốn xóa địa chỉ này?')) {
                fetch(`/account/address/${addressId}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert(data.message || 'Xóa địa chỉ thành công!');
                        localStorage.setItem('activeTab', 'address-book');
                        location.reload();
                    } else {
                        alert('Lỗi: ' + (data.error || 'Không thể xóa địa chỉ'));
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Có lỗi xảy ra khi xóa địa chỉ. Vui lòng thử lại.');
                });
            }
        }
    });

    // ========== AVATAR UPLOAD ==========
    const avatarImage = document.getElementById('avatarImage');
    const avatarInput = document.getElementById('avatarInput');

    if (avatarImage && avatarInput) {
        avatarImage.addEventListener('click', function() {
            avatarInput.click();
        });

        avatarInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                const formData = new FormData();
                formData.append('avatar', file);

                fetch('/account/upload-avatar', {
                    method: 'POST',
                    body: formData
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        avatarImage.src = data.avatarUrl;
                        alert('Cập nhật ảnh đại diện thành công!');
                    } else {
                        alert('Lỗi: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Có lỗi xảy ra khi upload ảnh đại diện. Vui lòng thử lại.');
                });
            }
        });
    }
    
    // ========== RETURN MODAL LOGIC ==========
    const checkboxes = document.querySelectorAll('.product-checkbox');
    const totalElement = document.getElementById('returnTotal');
    const returnForm = document.getElementById('returnForm');
    const submitReturnBtn = document.getElementById('submitReturnBtn');
    const returnModal = document.getElementById('returnModal');
    const returnSuccessModal = document.getElementById('returnSuccessModal');

    function updateTotal() {
        const total = Array.from(checkboxes)
            .filter(cb => cb.checked)
            .reduce((sum, cb) => sum + parseInt(cb.dataset.price), 0);
        
        if (totalElement) {
            totalElement.textContent = total.toLocaleString('vi-VN') + 'đ';
        }
    }

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', updateTotal);
    });

    if (submitReturnBtn) {
        submitReturnBtn.addEventListener('click', function () {
            if (!returnForm.checkValidity()) {
                returnForm.reportValidity();
                return;
            }

            const hasSelectedProducts = Array.from(checkboxes).some(checkbox => checkbox.checked);
            if (!hasSelectedProducts) {
                alert('Vui lòng chọn ít nhất một sản phẩm cần hoàn trả');
                return;
            }

            if (confirm('Bạn có chắc chắn muốn gửi yêu cầu hoàn trả?')) {
                // Đóng modal return
                bootstrap.Modal.getInstance(returnModal)?.hide();

                // Hiển thị modal success
                new bootstrap.Modal(returnSuccessModal).show();

                // Cập nhật trạng thái đơn hàng
                const statusBadge = document.querySelector('#completed .card .badge');
                if (statusBadge) {
                    statusBadge.className = 'badge bg-warning text-dark fw-semibold px-4 py-2 rounded-pill';
                    statusBadge.textContent = 'Đang xử lý hoàn trả';
                }

                // Chuyển sang tab processing
                document.querySelector('#processing-tab')?.click();
            }
        });
    }
});