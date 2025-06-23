


// Gọi API lấy danh sách sản phẩm
async function fetchProducts() {
    const response = await fetch('/api/product');
    const data = await response.json();
    console.log(data);
    
    return data.content || [];
}

function toVND(price) {
    if (!price) return '';
    return Number(price).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
}

function renderProducts(products) {
    const productList = document.getElementById('product-list');
    productList.innerHTML = '';
    products.forEach((product, index) => {
        const imageUrl = (product.images && product.images.length > 0) ? product.images[0].imageUrl : '';
        const category = (product.categories && product.categories.length > 0) ? product.categories.map(c => c.name).join(', ') : '';
        const price = product.minPrice ? toVND(product.minPrice) : '';
        const createdAt = product.createdAt ? new Date(product.createdAt).toLocaleDateString() : '';
        const row = document.createElement('tr');
        row.innerHTML = `
                        <th>${index + 1}</th>
                        <th><img src="${imageUrl}" alt="${product.name}" style="max-width: 70px;"></th>
                        <td>${product.name}</td>
                        <td>${price}</td>
                        <td>${category}</td>
                        <td>${product.status ? 'Còn bán' : 'Ngừng bán'}</td>
                        <td>${createdAt}</td>
                        <td>
                            <a href="/admin/product-detail?id=${product.productId}" class="btn btn-sm btn-primary" title="Xem chi tiết"><i class="fas fa-eye"></i></a>
                            <button class="btn btn-sm btn-danger btn-delete" data-id="${product.productId}" title="Xóa"><i class="fas fa-trash"></i></button>
                        </td>
                    `;
        productList.appendChild(row);
    });
    // Gán sự kiện xóa
    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', async function () {
            if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
                const id = this.getAttribute('data-id');
                const res = await fetch(`/api/product/${id}`, { method: 'DELETE' });
                if (res.ok) {
                    alert('Xóa thành công!');
                    loadProducts();
                } else {
                    alert('Xóa thất bại!');
                }
            }
        });
    });
}

async function loadProducts() {
    const products = await fetchProducts();
    renderProducts(products);
}

loadProducts();

// Tìm kiếm sản phẩm
document.getElementById('btn-search').addEventListener('click', async () => {
    const searchQuery = document.getElementById('search-product').value.trim();
    if (!searchQuery) {
        loadProducts();
        return;
    }
    const response = await fetch(`/api/product/search?keyword=${encodeURIComponent(searchQuery)}`);
    const data = await response.json();
    renderProducts(data.listProduct || []);
});