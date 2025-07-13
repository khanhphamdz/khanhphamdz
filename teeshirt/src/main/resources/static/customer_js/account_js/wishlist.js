document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll(".btn-add-wishlist").forEach(function (btn) {
    btn.addEventListener("click", function () {
      const productId = btn.getAttribute("data-product-id");
      fetch("/account/wishlist/add", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "productId=" + productId,
      })
        .then((res) => res.text())
        .then((data) => {
          if (data === "ok") {
            showAlert("Đã thêm vào danh sách yêu thích!");
            if (typeof updateWishlistBadge === "function")
              updateWishlistBadge();
          } else if (data === "not_logged_in") {
            showAlert("Vui lòng đăng nhập để sử dụng tính năng này.", "danger");
          }
        });
    });
  });

  document.querySelectorAll(".btn-remove-wishlist").forEach(function (btn) {
    btn.addEventListener("click", function () {
      const productId = btn.getAttribute("data-product-id");
      fetch("/account/wishlist/remove", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "productId=" + productId,
      })
        .then((res) => res.text())
        .then((data) => {
          if (data === "ok") {
            showAlert("Đã xóa khỏi danh sách yêu thích!");
            if (typeof updateWishlistBadge === "function")
              updateWishlistBadge();
            // Xóa sản phẩm khỏi giao diện nếu cần
            btn.closest(".col-md-4, .product").remove();
          } else if (data === "not_logged_in") {
            showAlert("Vui lòng đăng nhập để sử dụng tính năng này.", "danger");
          }
        });
    });
  });
});

function showAlert(message, type = "success") {
  let alert = document.createElement("div");
  alert.className = `alert alert-${type} position-fixed top-0 end-0 m-3 fade show`;
  alert.style.zIndex = 9999;
  alert.innerHTML = message;
  document.body.appendChild(alert);
  setTimeout(() => {
    alert.classList.remove("show");
    setTimeout(() => alert.remove(), 300);
  }, 1500);
}

// Hàm cập nhật badge wishlist (tìm icon trái tim trong header)
function updateWishlistBadge() {
  fetch("/account/wishlist/list")
    .then((res) => res.json())
    .then((data) => {
      const count = data.length;
      // Tìm icon trái tim trong header
      const header = document.getElementById("main-header");
      if (header) {
        // Tìm thẻ <a> chứa .fa-heart
        const heartLink = header.querySelector('a[href="/account/wishlist"]');
        if (heartLink) {
          const badge = heartLink.querySelector(".badge");
          if (badge) badge.textContent = count > 0 ? count : "";
        }
      }
    });
}
