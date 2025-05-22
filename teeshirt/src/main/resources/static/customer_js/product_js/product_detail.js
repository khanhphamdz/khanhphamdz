// Dữ liệu mẫu sản phẩm
const productData = {
    id: 1,
    name: "Áo thun basic",
    price: 199000,
    discountPrice: 159000,
    description: "Áo thun basic với chất liệu cotton 100%, thoáng mát và thoải mái khi mặc. Thiết kế đơn giản, dễ phối đồ.",
    images: [
        "https://res.cloudinary.com/dbqllv3nz/image/upload/v1747894538/tsv_white_zkweod.png",
        "https://res.cloudinary.com/dbqllv3nz/image/upload/v1747894537/tsv_black.png",
        "https://res.cloudinary.com/dbqllv3nz/image/upload/v1747894537/tsv_brown_umzvjc.png"
    ],
    colors: [
        { id: 1, name: "Đen", code: "#000000" },
        { id: 2, name: "Trắng", code: "#FFFFFF" },
        { id: 3, name: "Xám", code: "#808080" }
    ],
    sizes: ["S", "M", "L", "XL"],
    reviews: [
        {
            id: 1,
            author: "Nguyễn Văn A",
            rating: 5,
            date: "2024-03-15",
            content: "Sản phẩm chất lượng tốt, đúng như mô tả. Giao hàng nhanh chóng."
        },
        {
            id: 2,
            author: "Trần Thị B",
            rating: 4,
            date: "2024-03-10",
            content: "Áo đẹp, vải mềm mại. Giá cả hợp lý."
        }
    ]
};
// Hiển thị đánh giá
function displayReviews() {
    const reviewsList = document.querySelector('.reviews-list');
    reviewsList.innerHTML = productData.reviews.map(review => `
        <div class="review-item">
            <div class="review-header">
                <span class="review-author">${review.author}</span>
                <span class="review-date">${review.date}</span>
            </div>
            <div class="review-rating">
                ${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}
            </div>
            <p>${review.content}</p>
        </div>
    `).join('');
}

// Xử lý đánh giá sao
const starsInput = document.querySelector('.stars-input');
if (starsInput) {
    starsInput.addEventListener('click', (e) => {
        if (e.target.tagName === 'I') {
            const rating = e.target.dataset.rating;
            document.querySelectorAll('.stars-input i').forEach((star, index) => {
                star.className = index < rating ? 'fas fa-star' : 'far fa-star';
                star.style.color = index < rating ? '#FFD700' : '#ccc';
            });
        }
    });
}

// Xử lý form đánh giá
const reviewForm = document.getElementById('reviewForm');
if (reviewForm) {
    reviewForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const rating = document.querySelectorAll('.stars-input i.fas').length;
        const content = reviewForm.querySelector('textarea').value;
        
        if (rating === 0) {
            alert('Vui lòng chọn số sao đánh giá!');
            return;
        }
        
        if (!content.trim()) {
            alert('Vui lòng nhập nội dung đánh giá!');
            return;
        }

        // Thêm đánh giá mới
        const newReview = {
            id: productData.reviews.length + 1,
            author: "Khách hàng",
            rating: rating,
            date: new Date().toISOString().split('T')[0],
            content: content
        };
        
        productData.reviews.unshift(newReview);
        displayReviews();
        reviewForm.reset();
        document.querySelectorAll('.stars-input i').forEach(star => {
            star.className = 'far fa-star';
        });
    });
}
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".size-option").forEach((size) => {
        size.addEventListener("click", function () {
            document
                .querySelectorAll(".size-option")
                .forEach((s) => s.classList.remove("active"));
            this.classList.add("active");
        });
    });
        // Color selection
    document.querySelectorAll(".color-circle").forEach((circle) => {
        circle.addEventListener("click", function () {
            document
                .querySelectorAll(".color-circle")
                .forEach((c) => c.classList.remove("active"));
            this.classList.add("active");
        });
    });

    // Size selection
    document.querySelectorAll(".size-option").forEach((size) => {
        size.addEventListener("click", function () {
            document
                .querySelectorAll(".size-option")
                .forEach((s) => s.classList.remove("active"));
            this.classList.add("active");
        });
    });

    // Quantity buttons
    const quantityInput = document.querySelector(".quantity-input");
    document.querySelectorAll(".quantity-btn").forEach((btn) => {
        btn.addEventListener("click", function () {
            let value = parseInt(quantityInput.value);
            if (this.textContent.trim() === "+") {
                value++;
            } else if (value > 1) {
                value--;
            }
            quantityInput.value = value;
            // Visual feedback
            btn.classList.add("active");
            setTimeout(() => btn.classList.remove("active"), 120);
        });
    });

    // Thumbnail click handler
    document.querySelectorAll(".thumbnail").forEach((thumb) => {
        thumb.addEventListener("click", function () {
            document.querySelectorAll(".thumbnail").forEach((t) => t.classList.remove("active"));
            this.classList.add("active");
            // Update main image src to match clicked thumbnail
            document.querySelector(".main-image").src = this.src;
        });
    });
});

// Quantity buttons handler
const quantityInput = document.querySelector(".quantity-input");
document.querySelectorAll(".quantity-btn").forEach((btn) => {
    btn.addEventListener("click", function () {
        let value = parseInt(quantityInput.value);
        if (this.textContent === "+") {
            value++;
        } else if (value > 1) {
            value--;
        }
        quantityInput.value = value;
        // Hiệu ứng active cho nút vừa click
        document.querySelectorAll(".quantity-btn").forEach((b) => b.classList.remove("active"));
        this.classList.add("active");
        // Loại bỏ hiệu ứng sau 150ms để tạo cảm giác nhấn
        setTimeout(() => {
            this.classList.remove("active");
        }, 150);
    });
    displayReviews();
});