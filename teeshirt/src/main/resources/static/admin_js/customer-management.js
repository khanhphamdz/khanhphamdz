function showCustomerDetails(customerId) {
    // Hàm mẫu, cần tích hợp API để lấy dữ liệu
    const customerData = {
        code: customerId,
        name: 'Nguyễn Văn Anh',
        phone: '0123456789',
        email: 'vinhnguyen0120@gmail.com',
        gender: 'Nam',
        address: '123 Đường Láng, Láng Thượng, Đống Đa, Hà Nội',
        totalOrders: 15,
        totalSpending: '9,850,000đ'
    };

    // Cập nhật thông tin chi tiết khách hàng
    document.getElementById('viewCustomerCode').textContent = customerData.code;
    document.getElementById('viewCustomerName').textContent = customerData.name;
    document.getElementById('viewCustomerPhone').textContent = customerData.phone;
    document.getElementById('viewCustomerEmail').textContent = customerData.email;
    document.getElementById('viewCustomerGender').textContent = customerData.gender;
    document.getElementById('viewCustomerAddress').textContent = customerData.address;

    // Cập nhật thông tin mua sắm
    document.getElementById('totalOrders').textContent = customerData.totalOrders;
    document.getElementById('totalSpending').textContent = customerData.totalSpending;
}

function addNewCustomer() {
    // Logic thêm khách hàng mới
    alert('Đã thêm khách hàng mới!');
    $('#addCustomerModal').modal('hide');
}