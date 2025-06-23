
const initApp = () => {
    window.addEventListener('scroll', function () {
        const btnBackToTop = document.getElementById('back-to-top');
        if (window.scrollY > 500) {
            btnBackToTop.style.display = 'block';
        } else {
            btnBackToTop.style.display = 'none';
        }
    });
    document.getElementById('back-to-top').addEventListener('click', function () {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
    window.addEventListener('scroll', function () {
        const header = document.getElementById('main-header');
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
            header.classList.remove('transparent-header');
        } else {
            header.classList.remove('scrolled');
            header.classList.add('transparent-header');
        }
    });

    const button = document.getElementById("btn-search-product");
    button.addEventListener("click", getData);

    const inputSearch = document.getElementById("filter");
    inputSearch.addEventListener("input", debouce(getData, 1500));
}
async function getData() {
    const products = document.querySelector('.product-search-list')
    const filter = document.getElementById('filter')
    const listItems = []
    let keyword = filter.value;
    console.log(keyword);

    const res = await fetch(`http://localhost:8080/api/product/search?keyword=${encodeURIComponent(keyword)}`);
    console.log(res);

    const results = await res.json();
    console.log(results);

    if (results.status === true) {
        // Clear products
        products.innerHTML = '';
        const listProduct = results.listProduct;

        if (listProduct.length > 0) {
            const div = document.createElement('div');
            div.setAttribute('class', 'product-search-item');
            listItems.push(div);
            listProduct.forEach((product) => {
                const div = document.createElement('div');
                div.setAttribute('class', 'product-search-item');
                div.innerHTML = `
                    <a href="/product/detail/${product.productId}">
                        <img src="${product.images && product.images.length > 0 ? product.images[0].imageUrl : 'https://res.cloudinary.com/...'}" alt="">
                    </a>
                    <div class="product-search-item-detail">
                        <a href="/product/detail/${product.productId}" class="text-decoration-none">
                            <h4>${product.name ? product.name.slice(0, 30) : 'Không có tên'}</h4>
                        </a>
                        <span>${product.description ? product.description.slice(0, 30) : 'Không có mô tả'}</span>
                    </div>
                `;
                products.appendChild(div);
            });
        } else {
            products.innerHTML = `
            <h6 class="text-danger">Không tìm thấy sản phẩm liên quan</h6>
            `;
        }
    }
}
document.addEventListener("DOMContentLoaded", initApp)
const debouce = (fn, delay) => {
    delay = delay || 0;
    let timerId;
    console.log("TimerId imadiate load:", timerId)
    return () => {
        console.log(`TimerId previous at: ${timerId}`);
        if (timerId) {
            clearTimeout(timerId)
            timerId = null;
        }
        timerId = setTimeout(() => {
            fn();
        }, delay)
    }

}