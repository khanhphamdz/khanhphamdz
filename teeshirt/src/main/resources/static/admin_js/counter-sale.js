// --- BIẾN TOÀN CỤC ---
let carts = [{}]; // Mỗi hóa đơn là 1 cart
let activeOrder = 0;
let orderCount = 1;
const ORDER_LIMIT = 5;
let selectedCustomer = null;
let customerAddresses = [];
// Thêm biến toàn cục lưu employeeId hiện tại
let currentEmployeeId = null;
// Thêm biến toàn cục cho coupon
let appliedCoupon = null;
let discountAmount = 0;

function createOrder() {
  if (orderCount >= ORDER_LIMIT) {
    showToast("Chỉ có thể tạo tối đa 5 hóa đơn 1 lúc", "danger");
    return;
  }
  carts.push({});
  orderCount++;
  activeOrder = carts.length - 1;
  updateOrderTabs();
  renderCart();
  updateOrderLimitMsg();
}
function updateOrderTabs() {
  const orderTabs = document.getElementById("orderTabs");
  orderTabs.innerHTML = "";
  for (let i = 0; i < carts.length; i++) {
    orderTabs.innerHTML += `<li class="nav-item">
      <button class="nav-link${i === activeOrder ? " active" : ""
      }" onclick="switchOrder(${i})">Hóa đơn ${i + 1
      } <span class='text-danger' style='cursor:pointer;' onclick='closeOrder(event,${i})'>&times;</span></button>
  </li>`;
  }
}
function switchOrder(idx) {
  activeOrder = idx;
  updateOrderTabs();
  renderCart();
  // Nếu hóa đơn này là mới (chưa từng nhập gì), reset các trường nhập liệu
  let cart = carts[activeOrder];
  if (!cart._initialized) {
    document.getElementById("customerPayment").value = "";
    document.getElementById("changeAmount").textContent = "0đ";
    document.getElementById("isDelivery").checked = false;
    document.getElementById("retailCustomer").checked = true;
    document.getElementById("customerList").value = "";
    document.getElementById("deliveryAddress").value = "";
    document.getElementById("paymentMethod").selectedIndex = 0;
    toggleCustomerSelection();
    toggleDeliveryForm();
    cart._initialized = true;
  }
}
function closeOrder(e, idx) {
  e.stopPropagation();
  if (carts.length === 1) return;
  carts.splice(idx, 1);
  if (activeOrder >= carts.length) activeOrder = carts.length - 1;
  orderCount--;
  updateOrderTabs();
  renderCart();
  updateOrderLimitMsg();
}
function updateOrderLimitMsg() {
  const msg = document.getElementById("orderLimitMsg");
  if (orderCount >= ORDER_LIMIT) {
    msg.textContent = "Đã đạt giới hạn 5 hóa đơn.";
    document.getElementById("createOrderBtn").disabled = true;
  } else {
    msg.textContent = `Đã tạo ${orderCount}/5 hóa đơn.`;
    document.getElementById("createOrderBtn").disabled = false;
  }
}

// Hàm kiểm tra tồn kho sản phẩm
function checkProductStock(productId, size = null, color = null) {
  if (productVariants[productId]) {
    // Nếu là sản phẩm có biến thể
    if (!size || !color) return 0;
    const variant = productVariants[productId].find(
      (v) => v.size === size && v.color === color
    );
    return variant ? variant.stock : 0;
  } else {
    // Nếu là sản phẩm thường
    return productStock[productId] || 0;
  }
}

// Hàm cập nhật tồn kho sản phẩm
function updateProductStock(
  productId,
  quantity,
  size = null,
  color = null
) {
  if (productVariants[productId]) {
    // Nếu là sản phẩm có biến thể
    if (!size || !color) return false;
    const variant = productVariants[productId].find(
      (v) => v.size === size && v.color === color
    );
    if (variant) {
      variant.stock -= quantity;
      return true;
    }
    return false;
  } else {
    // Nếu là sản phẩm thường
    if (productStock[productId] !== undefined) {
      productStock[productId] -= quantity;
      return true;
    }
    return false;
  }
}

// Cập nhật hàm addToCart
function addToCart(item) {
  let cart = carts[activeOrder];
  if (!cart.items) cart.items = [];
  cart._initialized = true;

  // Xác định key duy nhất cho sản phẩm hoặc biến thể
  let key = item.variantId ? `${item.productId}_${item.variantId}` : `${item.productId}`;

  // Tìm sản phẩm/biến thể đã có trong giỏ
  let existingItem = cart.items.find(i => (i.variantId ? `${i.productId}_${i.variantId}` : `${i.productId}`) === key);

  // Kiểm tra tồn kho
  let maxQty = item.quantityInStock || 99;
  if (existingItem) {
    if (existingItem.quantity + 1 > maxQty) {
      showToast('Số lượng vượt quá tồn kho!', 'danger');
      return;
    }
    existingItem.quantity++;
  } else {
    cart.items.push({
      productId: item.productId,
      variantId: item.variantId,
      name: item.name || '',
      size: item.size || '',
      color: item.color || '',
      price: item.price,
      quantity: 1,
      imageUrl: item.imageUrl,
      quantityInStock: maxQty,
      // Lưu thêm mô tả biến thể nếu có
      variantName: (item.size && item.color) ? (item.size + ' - ' + item.color) : (item.variantName || ''),
    });
  }
  renderCart();
}

// Cập nhật hàm confirmVariant
function confirmVariant(productId, name, price, category, image) {
  const size = document.getElementById("variantSize").value;
  const color = document.getElementById("variantColor").value;
  const currentStock = checkProductStock(productId, size, color);

  if (currentStock <= 0) {
    showToast("Biến thể này đã hết hàng!", "danger");
    return;
  }

  let cart = carts[activeOrder];
  if (!cart.items) cart.items = [];
  cart._initialized = true;

  const key = `${productId}_${size}_${color}`;
  const existingItem = cart.items.find((item) => item.id === key);

  if (existingItem) {
    if (existingItem.quantity < currentStock) {
      existingItem.quantity++;
    } else {
      showToast("Số lượng vượt quá tồn kho biến thể!", "danger");
      return;
    }
  } else {
    cart.items.push({
      id: key,
      name: name + ` (${size}, ${color})`,
      price,
      quantity: 1,
      stock: currentStock,
      category,
      image,
      size,
      color,
    });
  }
  renderCart();
  document.getElementById("variantModal").remove();
  showToast("Đã thêm vào giỏ hàng!");
}

// Cập nhật hàm updateVariantStock
function updateVariantStock() {
  const size = document.getElementById("variantSize").value;
  const color = document.getElementById("variantColor").value;
  const modal = document.getElementById("variantModal");
  if (!modal) return;

  const productId = Object.keys(variants).find((vid) =>
    variants[vid].some((v) => v.size === size && v.color === color)
  );

  if (productId) {
    const currentStock = checkProductStock(productId, size, color);
    document.getElementById(
      "variantStockInfo"
    ).textContent = `Tồn kho: ${currentStock}`;
  } else {
    document.getElementById("variantStockInfo").textContent =
      "Không có tồn kho";
  }
}

// Sửa completeTransaction để gửi dữ liệu về backend
async function completeTransaction() {
  if (!carts[activeOrder].items || carts[activeOrder].items.length === 0) {
    showToast("Giỏ hàng trống!", "danger");
    return;
  }
  const cart = carts[activeOrder];
  console.log(cart);
  
  // Lấy số tiền khách trả
  const customerPayment = parseFloat(document.getElementById("customerPayment").value) || 0;
  // Tính lại finalAmount
  const total = cart.items.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const finalAmount = total - discountAmount;
  if (customerPayment < finalAmount) {
    showToast("Số tiền khách trả chưa đủ!", "danger");
    return;
  }
  const change = customerPayment - finalAmount;
  const items = cart.items.map(item => ({
    productVariantId: item.variantId,
    quantity: item.quantity,
    price: item.price,
    productName: item.productName,
    size: item.size,
    color: item.color,
    variantName: item.variantName,
    imageUrl: item.imageUrl
  }));
  const payment = {
    paymentType: document.getElementById("paymentMethod").value === "cash" ? "COD" : "VNPAY",
    amount: finalAmount,
    paymentStatus: "completed"
  };
  // Xử lý địa chỉ giao hàng
  let shippingAddress = null;
  let shippingAddressId = null;
  if (document.getElementById("isDelivery").checked) {
    const selectVal = document.getElementById('customerAddressSelect') ? document.getElementById('customerAddressSelect').value : null;
    if (selectVal) {
      // Địa chỉ đã lưu
      shippingAddressId = selectVal;
    } else {
      // Nhập mới
      shippingAddress = {
        name: document.getElementById("shippingName").value,
        phone: document.getElementById("shippingPhone").value,
        specificAddress: document.getElementById("addressDetail").value,
        provinceId: document.getElementById("provinceSelect").value,
        districtId: document.getElementById("districtSelect").value,
        wardId: document.getElementById("wardSelect").value
      };
      if (!shippingAddress.name || !shippingAddress.phone || !shippingAddress.specificAddress || !shippingAddress.provinceId || !shippingAddress.districtId || !shippingAddress.wardId) {
        showToast('Vui lòng nhập đầy đủ thông tin địa chỉ giao hàng!', 'danger');
        return;
      }
    }
  }
  const orderData = {
    customerId: selectedCustomer ? selectedCustomer.customerId : null,
    employeeId: currentEmployeeId,
    shippingAddressId: shippingAddressId,
    shippingAddress: shippingAddress,
    items,
    payment,
    finalAmount: finalAmount,
    discountAmount: discountAmount,
    couponId: appliedCoupon,
    shippingFee: 0,
    note: "",
    orderType: "offline",
    customerPayment: customerPayment,
    change: change
  };
  try {
    const res = await fetch('/api/counter-sale/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderData)
    });
    const result = await res.json();
    if (res.ok && result && result.orderId) {
      showToast('Thanh toán thành công!');
      carts[activeOrder] = {};
      renderCart();
      // Đảm bảo payment type được giữ nguyên từ frontend
      const invoiceData = { 
        ...orderData, 
        ...result, 
        items,
        payment: {
          ...orderData.payment,
          ...(result.payment || {}),
          // Ưu tiên payment type từ frontend
          paymentType: orderData.payment.paymentType
        }
      };
      showInvoice(invoiceData);
    } else {
      showToast(result.message || 'Lỗi khi lưu hóa đơn!', 'danger');
    }
  } catch (err) {
    showToast('Lỗi khi gửi hóa đơn!', 'danger');
  }
}

// Sửa showInvoice để hiển thị hóa đơn chi tiết, rõ ràng địa chỉ giao hàng
function showInvoice(order) {
  console.log('Order data for invoice:', order);
  console.log('Payment data:', order.payment);
  console.log('Payment type:', order.payment ? order.payment.paymentType : 'No payment data');
  
  const now = new Date(order.createdAt || Date.now());
  const pad = (n) => n.toString().padStart(2, "0");
  const dateStr = `${pad(now.getDate())}/${pad(now.getMonth() + 1)}/${now.getFullYear()} ${pad(now.getHours())}:${pad(now.getMinutes())}`;

  // Xử lý thông tin giao hàng
  let shippingInfo = '';
  if (order.shippingAddressId || order.shippingAddress) {
    if (order.shippingAddress) {
      // Tạo địa chỉ đầy đủ
      let fullAddress = order.shippingAddress.specificAddress || '';
      if (order.shippingAddress.wardName) {
        fullAddress += fullAddress ? ', ' + order.shippingAddress.wardName : order.shippingAddress.wardName;
      }
      if (order.shippingAddress.districtName) {
        fullAddress += fullAddress ? ', ' + order.shippingAddress.districtName : order.shippingAddress.districtName;
      }
      if (order.shippingAddress.provinceName) {
        fullAddress += fullAddress ? ', ' + order.shippingAddress.provinceName : order.shippingAddress.provinceName;
      }
      
      shippingInfo = `
  <div><strong>Người nhận:</strong> ${order.shippingAddress.name || 'Khách lẻ'}</div>
  <div><strong>SĐT:</strong> ${order.shippingAddress.phone || '---'}</div>
  <div><strong>Địa chỉ:</strong> ${fullAddress || '---'}</div>
`;
    } else {
      shippingInfo = '<div><strong>Khách lẻ (Nhận tại quầy)</strong></div>';
    }
  } else {
    shippingInfo = '<div><strong>Khách lẻ</strong></div>';
  }

  // Xử lý danh sách sản phẩm
  const items = order.items || [];
  const itemRows = items.length > 0
    ? items.map(item => `
  <tr>
    <td>
      ${item.productName || item.productName || 'Sản phẩm không xác định'}
      ${item.variantName ? `<span style='font-size:90%;color:#555;'> (${item.variantName})</span>` : ''}
    </td>
    <td class="text-end">${item.quantity || 0}</td>
    <td class="text-end">${item.price ? item.price.toLocaleString('vi-VN') : '0'}đ</td>
    <td class="text-end">${item.price && item.quantity ? (item.price * item.quantity).toLocaleString('vi-VN') : '0'}đ</td>
  </tr>
`).join('')
    : '<tr><td colspan="4" class="text-center">Không có sản phẩm</td></tr>';

  // Tính toán giá trị mặc định nếu thiếu
  const subtotal = order.subtotal || (items.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0));
  const finalAmount = order.finalAmount || (subtotal - (order.discountAmount || 0));
  const customerPayment = order.customerPayment || 0;
  const change = customerPayment - finalAmount;

  let invoiceHTML = `
<div class="p-4">
<div class="invoice-header">
  <h3>TeeShirtVibe</h3>
</div>
<div class="invoice-info">
  <p>HÓA ĐƠN BÁN HÀNG</p>
  <p>Mã hóa đơn: ${order.orderCode || ('HD' + (order.orderId || Date.now().toString().slice(-6)))}</p>
  <p>Ngày: ${dateStr}</p>
  <p>Nhân viên: ${order.employeeName || document.getElementById("createdBy").value || '---'}</p>
</div>
<div class="invoice-info">
  <h6>Thông tin giao hàng:</h6>
  ${shippingInfo}
</div>
<div class="table-responsive invoice-table">
  <table class="table table-bordered">
    <thead>
      <tr>
        <th>Sản phẩm</th>
        <th class="text-end">SL</th>
        <th class="text-end">Đơn giá</th>
        <th class="text-end">Thành tiền</th>
      </tr>
    </thead>
    <tbody>
      ${itemRows}
    </tbody>
  </table>
</div>
<div class="invoice-total">
  <p><span>Tạm tính:</span><span>${subtotal.toLocaleString('vi-VN')}đ</span></p>
  <p><span>Giảm giá:</span><span>-${(order.discountAmount || 0).toLocaleString('vi-VN')}đ</span></p>
  <p class="total-amount"><span>Tổng cộng:</span><span>${finalAmount.toLocaleString('vi-VN')}đ</span></p>
</div>
<div class="invoice-info">
  <h6>Thông tin thanh toán:</h6>
  <p>Phương thức: ${getPaymentMethodDisplay(order)}</p>
  <p>Khách trả: ${customerPayment.toLocaleString('vi-VN')}đ</p>
  <p>Tiền thối: ${change >= 0 ? change.toLocaleString('vi-VN') : '0'}đ</p>
</div>
<div class="invoice-footer">
  <p class="thank-you">Cảm ơn quý khách đã mua hàng!</p>
  <p>Hẹn gặp lại quý khách</p>
</div>
</div>
`;

  document.getElementById("invoiceModalContent").innerHTML = invoiceHTML;
  const modal = new bootstrap.Modal(document.getElementById("invoiceModal"));
  modal.show();
}

// Hàm helper để hiển thị phương thức thanh toán
function getPaymentMethodDisplay(order) {
  
  // Kiểm tra payment type từ payment object
  if (order.payment && order.payment.paymentType) {
    const result = order.payment.paymentType.toUpperCase() === 'VNPAY' ? 'Chuyển khoản' : 'Tiền mặt';
    console.log('Returning:', result);
    return result;
  }
  
  // Fallback: kiểm tra payment type trực tiếp từ order
  if (order.paymentType) {
    return order.paymentType.toUpperCase() === 'VNPAY' ? 'Chuyển khoản' : 'Tiền mặt';
  }
  
  // Fallback: kiểm tra từ payment method được chọn
  const paymentMethod = document.getElementById("paymentMethod");
  if (paymentMethod) {
    return paymentMethod.value === "cash" ? "Tiền mặt" : "Chuyển khoản";
  }
  
  console.log('Using default fallback');
  return "Tiền mặt"; // Default fallback
}

function exportToPDF() {
  const { jsPDF } = window.jspdf;
  const doc = new jsPDF();

  // Thêm tiêu đề
  doc.setFontSize(16);
  doc.text("TeeShirtVibe", 105, 15, { align: "center" });

  // Thông tin hóa đơn
  doc.setFontSize(10);
  let y = 25;
  doc.text("HÓA ĐƠN BÁN HÀNG", 14, y);
  y += 7;
  doc.text(`Mã hóa đơn: HD${Date.now().toString().slice(-6)}`, 14, y);
  y += 7;
  const now = new Date();
  const pad = (n) => n.toString().padStart(2, "0");
  const dateStr = `${pad(now.getDate())}/${pad(
    now.getMonth() + 1
  )}/${now.getFullYear()} ${pad(now.getHours())}:${pad(
    now.getMinutes()
  )}`;
  doc.text(`Ngày: ${dateStr}`, 14, y);
  y += 7;
  doc.text(
    `Nhân viên: ${document.getElementById("createdBy").value}`,
    14,
    y
  );

  // Thông tin khách hàng/giao hàng
  y += 10;
  doc.setFontSize(11);
  doc.text("Thông tin giao hàng:", 14, y);
  y += 7;
  doc.setFontSize(10);
  const shippingInfo = document.querySelectorAll(".invoice-info")[1];
  if (shippingInfo) {
    const shippingText = Array.from(shippingInfo.querySelectorAll("div, p")).map(
      (p) => p.textContent.trim()
    );
    doc.text(shippingText, 14, y);
    y += shippingText.length * 7;
  }

  // Bảng sản phẩm
  const invoiceTable = document.querySelector(".invoice-table table");
  if (invoiceTable) {
    const rows = Array.from(invoiceTable.querySelectorAll("tr")).map(tr =>
      Array.from(tr.querySelectorAll("th,td")).map(td => td.textContent.trim())
    );
    if (rows.length > 1) {
      doc.autoTable({
        startY: y + 5,
        head: [rows[0]],
        body: rows.slice(1),
        theme: "grid",
        styles: {
          fontSize: 9,
          cellPadding: 2,
        },
        headStyles: {
          fillColor: [0, 0, 0],
          textColor: 255,
          fontStyle: "bold",
        },
      });
      y = doc.lastAutoTable.finalY;
    }
  }

  // Thông tin thanh toán
  const invoiceInfos = document.querySelectorAll(".invoice-info");
  let paymentText = [];
  if (invoiceInfos.length > 2) {
    const paymentInfo = invoiceInfos[invoiceInfos.length - 1];
    paymentText = Array.from(paymentInfo.querySelectorAll("p")).map(
      (p) => p.textContent.trim()
    );
    doc.text(paymentText, 14, y + 10);
  }

  // Lời cảm ơn
  const thankYou = document.querySelector(".invoice-footer");
  if (thankYou) {
    doc.setFontSize(11);
    const thankText = Array.from(thankYou.querySelectorAll("p")).map((p) =>
      p.textContent.trim()
    );
    doc.text(
      thankText,
      105,
      y + 30,
      { align: "center" }
    );
  }

  // Xuất file
  doc.save(`hoadon_${Date.now()}.pdf`);
}

// Hiển thị toast thông báo
function showToast(message, type = "success") {
  let toast = document.getElementById("customToast");
  toast.innerHTML = `<div class="toast-content" style="padding:16px 24px 18px 20px; border-radius:10px; background:${type === "danger" ? "#dc3545" : "#28a745"
    }; color:#fff; display:flex; align-items:center; gap:12px; position:relative; min-width:220px; max-width:340px;">
  <i class='fa ${type === "danger" ? "fa-times-circle" : "fa-check-circle"
    }' style='font-size:1.4rem;'></i>
  <span style='flex:1;'>${message}</span>
  <span class='toast-close' style='position:absolute;top:10px;right:12px;cursor:pointer;font-size:1.2rem;opacity:0.7;'>&times;</span>
  <div class='toast-progress' style='position:absolute;left:0;bottom:0;height:4px;width:100%;background:rgba(255,255,255,0.25);overflow:hidden;border-radius:0 0 10px 10px;'>
      <div class='toast-bar' style='height:100%;width:100%;background:#fff;opacity:0.7;transform:scaleX(1);transform-origin:left;transition:transform 5s linear;'></div>
  </div>
</div>`;
  toast.style.opacity = "1";
  toast.style.pointerEvents = "auto";
  setTimeout(() => {
    const bar = toast.querySelector(".toast-bar");
    if (bar) bar.style.transform = "scaleX(0)";
  }, 30);
  const closeBtn = toast.querySelector(".toast-close");
  if (closeBtn) {
    closeBtn.onclick = function () {
      toast.style.opacity = "0";
    };
  }
  if (toast._timeout) clearTimeout(toast._timeout);
  toast._timeout = setTimeout(() => {
    toast.style.opacity = "0";
  }, 5000);
}
// Hiển thị alert góc trái
function showAlert(message) {
  let alertDiv = document.getElementById("customLeftAlert");
  if (!alertDiv) {
    alertDiv = document.createElement("div");
    alertDiv.id = "customLeftAlert";
    alertDiv.style.position = "fixed";
    alertDiv.style.top = "24px";
    alertDiv.style.left = "24px";
    alertDiv.style.zIndex = "9999";
    alertDiv.style.minWidth = "220px";
    alertDiv.style.maxWidth = "340px";
    document.body.appendChild(alertDiv);
  }
  alertDiv.innerHTML = `<div style="padding:14px 22px 14px 18px; border-radius:10px; background:#dc3545; color:#fff; font-weight:500; box-shadow:0 2px 8px rgba(0,0,0,0.08); display:flex; align-items:center; gap:10px;">
  <i class='fa fa-exclamation-circle' style='font-size:1.3rem;'></i>
  <span style='flex:1;'>${message}</span>
</div>`;
  alertDiv.style.opacity = "1";
  alertDiv.style.pointerEvents = "auto";
  if (alertDiv._timeout) clearTimeout(alertDiv._timeout);
  alertDiv._timeout = setTimeout(() => {
    alertDiv.style.opacity = "0";
  }, 3000);
}
// Hiển thị thời gian tạo hóa đơn và nhân viên tạo
function setCreatedTime() {
  const now = new Date();
  const pad = (n) => n.toString().padStart(2, "0");
  const str = `${pad(now.getDate())}/${pad(
    now.getMonth() + 1
  )}/${now.getFullYear()} ${pad(now.getHours())}:${pad(
    now.getMinutes()
  )}`;
  document.getElementById("createdTime").textContent = str;
}
window.onload = function () {
  updateOrderTabs();
  renderCart();
  updateOrderLimitMsg();
  setCreatedTime();
};

// Hiển thị modal chọn biến thể
function showVariantModal(productId, name, price, category, image) {
  let modal = document.getElementById("variantModal");
  if (modal) modal.remove();
  modal = document.createElement("div");
  modal.id = "variantModal";
  const variants = productVariants[productId];
  // Lấy danh sách size, màu duy nhất
  const sizes = [...new Set(variants.map((v) => v.size))];
  const colors = [...new Set(variants.map((v) => v.color))];
  modal.innerHTML = `
  <div style="position:fixed;top:0;left:0;width:100vw;height:100vh;background:rgba(0,0,0,0.3);z-index:20000;display:flex;align-items:center;justify-content:center;">
      <div style="background:#fff;padding:28px 24px 20px 24px;border-radius:14px;min-width:320px;box-shadow:0 4px 24px rgba(0,0,0,0.12);position:relative;max-width:95vw;">
          <div style="position:absolute;top:10px;right:16px;cursor:pointer;font-size:1.5rem;opacity:0.7;" onclick="document.getElementById('variantModal').remove()">&times;</div>
          <h5 class="mb-3 text-center">Chọn size, màu cho sản phẩm</h5>
          <div class="mb-2"><strong>${name}</strong></div>
          <div class="mb-2">
              <label class="form-label">Size:</label>
              <select class="form-select" id="variantSize" onchange="updateVariantStock()">
                  ${sizes
      .map((s) => `<option value='${s}'>${s}</option>`)
      .join("")}
              </select>
          </div>
          <div class="mb-2">
              <label class="form-label">Màu:</label>
              <select class="form-select" id="variantColor" onchange="updateVariantStock()">
                  ${colors
      .map((c) => `<option value='${c}'>${c}</option>`)
      .join("")}
              </select>
          </div>
          <div class="mb-2">
              <span id="variantStockInfo" class="text-muted small"></span>
          </div>
          <div class="text-end">
              <button class="btn btn-primary" onclick="confirmVariant('${productId}','${name}',${price},'${category}','${image}')">Thêm vào giỏ</button>
          </div>
      </div>
  </div>
`;
  document.body.appendChild(modal);
  setTimeout(updateVariantStock, 10);
}
function toggleCustomerSelection() {
  const searchBox = document.getElementById("customerSearchBox");
  if (document.getElementById("selectCustomer").checked) {
    searchBox.style.display = "block";
    renderCustomerList(demoCustomers);
  } else {
    searchBox.style.display = "none";
    selectedCustomerId = "";
  }
}
function renderCustomerList(list) {
  const ul = document.getElementById("customerListBox");
  ul.innerHTML = "";
  if (list.length === 0) {
    ul.innerHTML =
      '<li class="list-group-item text-muted">Không tìm thấy khách hàng</li>';
    return;
  }
  list.forEach((cus) => {
    ul.innerHTML += `
      <li class="list-group-item list-group-item-action" style="cursor:pointer;" onclick="selectCustomer('${cus.id}')">
          <div class="d-flex justify-content-between align-items-center">
              <div>
                  <strong>${cus.name}</strong>
                  <div class="small text-muted">${cus.phone}</div>
              </div>
              <i class="fas fa-chevron-right text-muted"></i>
          </div>
      </li>
  `;
  });
}
function filterCustomerList() {
  const keyword = document
    .getElementById("customerSearchInput")
    .value.trim()
    .toLowerCase();
  const filtered = demoCustomers.filter(
    (cus) =>
      cus.name.toLowerCase().includes(keyword) ||
      cus.phone.includes(keyword)
  );
  renderCustomerList(filtered);
}
function selectCustomer(id) {
  selectedCustomerId = id;
  const cus = demoCustomers.find((c) => c.id === id);
  document.getElementById(
    "customerSearchInput"
  ).value = `${cus.name} - ${cus.phoneNumber}`;
  // Ẩn danh sách sau khi chọn
  document.getElementById("customerListBox").innerHTML = `
      <li class="list-group-item text-success">
          <div class="d-flex justify-content-between align-items-center">
              <div>
                  <strong>${cus.name}</strong>
                  <div class="small">${cus.phone}</div>
              </div>
              <i class="fas fa-check-circle"></i>
          </div>
      </li>
  `;
}
function toggleDeliveryForm() {
  const isDelivery = document.getElementById("isDelivery").checked;
  const deliveryForm = document.getElementById("deliveryForm");
  deliveryForm.style.display = isDelivery ? "block" : "none";
  if (!isDelivery) {
    document.getElementById('addressSelectBox').style.display = 'none';
    document.getElementById('provinceDistrictWardBox').style.display = 'none';
    document.getElementById('shippingName').value = '';
    document.getElementById('shippingPhone').value = '';
    document.getElementById('addressDetail').value = '';
    document.getElementById('provinceSelect').value = '';
    document.getElementById('districtSelect').value = '';
    document.getElementById('wardSelect').value = '';
  } else {
    // Nếu đã chọn khách hàng thì load địa chỉ
    if (selectedCustomer && selectedCustomer.customerId) {
      fetch(`/api/counter-sale/customers/${selectedCustomer.customerId}/addresses`)
        .then(res => res.json())
        .then(addresses => {
          customerAddresses = addresses;
          renderCustomerAddresses();
        });
    } else {
      document.getElementById('addressSelectBox').style.display = 'none';
      document.getElementById('provinceDistrictWardBox').style.display = '';
      fetchProvinces();
    }
  }
}

// --- Bổ sung các hàm lấy tỉnh/thành, quận/huyện, phường/xã ---
function fetchProvinces() {
  fetch("https://provinces.open-api.vn/api/p/")
    .then((res) => res.json())
    .then((data) => {
      const select = document.getElementById("provinceSelect");
      select.innerHTML =
        '<option value="">Chọn tỉnh/thành</option>' +
        data.map((p) => `<option value="${p.code}">${p.name}</option>`).join("");
      document.getElementById("districtSelect").innerHTML =
        '<option value="">Chọn quận/huyện</option>';
      document.getElementById("wardSelect").innerHTML =
        '<option value="">Chọn phường/xã</option>';
    });
}
function fetchDistricts() {
  const provinceCode = document.getElementById("provinceSelect").value;
  if (!provinceCode) return;
  fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`)
    .then((res) => res.json())
    .then((data) => {
      const select = document.getElementById("districtSelect");
      select.innerHTML =
        '<option value="">Chọn quận/huyện</option>' +
        data.districts.map((d) => `<option value="${d.code}">${d.name}</option>`).join("");
      document.getElementById("wardSelect").innerHTML =
        '<option value="">Chọn phường/xã</option>';
    });
}
function fetchWards() {
  const districtCode = document.getElementById("districtSelect").value;
  if (!districtCode) return;
  fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`)
    .then((res) => res.json())
    .then((data) => {
      const select = document.getElementById("wardSelect");
      select.innerHTML =
        '<option value="">Chọn phường/xã</option>' +
        data.wards.map((w) => `<option value="${w.code}">${w.name}</option>`).join("");
    });
}

function renderCart() {
  let cart = carts[activeOrder];
  if (!cart.items) cart.items = [];
  const cartItems = document.getElementById("cartItems");
  cartItems.innerHTML = "";
  let total = 0;
  cart.items.forEach((item) => {
    const subtotal = item.price * item.quantity;
    total += subtotal;
    cartItems.innerHTML += `
          <tr>
              <td>
                  <img src="${item.imageUrl || "https://via.placeholder.com/40"
      }" alt="img" style="width:40px;height:40px;object-fit:cover;border-radius:6px;margin-right:8px;vertical-align:middle;">
                  <span>${item.name}</span>
              </td>
              <td>
                  <button class="btn btn-sm btn-outline-secondary" onclick='updateQuantity("${item.variantId ? `${item.productId}_${item.variantId}` : `${item.productId}`}", -1)'>-</button>
                  <span class="mx-2">${item.quantity}</span>
                  <button class="btn btn-sm btn-outline-secondary" onclick='updateQuantity("${item.variantId ? `${item.productId}_${item.variantId}` : `${item.productId}`}", 1)'>+</button>
              </td>
              <td>${item.price.toLocaleString("vi-VN")}đ</td>
              <td>${subtotal.toLocaleString("vi-VN")}đ</td>
              <td><button class="btn btn-sm btn-danger" onclick="removeFromCart('${item.variantId ? `${item.productId}_${item.variantId}` : `${item.productId}`}')"><i class="fas fa-trash"></i></button></td>
          </tr>
      `;
  });
  // Hiển thị tổng tiền và giảm giá
  let discountHtml = `<div class='text-success'>Giảm giá: -${discountAmount.toLocaleString("vi-VN")}đ</div>`;
  document.getElementById("cartTotal").innerHTML =
    (total - discountAmount).toLocaleString("vi-VN") + "đ" + discountHtml;
  calculateChange();
}
function updateQuantity(key, change) {
  let cart = carts[activeOrder];
  if (!cart.items) return;
  const item = cart.items.find(i => (i.variantId ? `${i.productId}_${i.variantId}` : `${i.productId}`) === key);
  if (item) {
    const newQuantity = item.quantity + change;
    if (newQuantity <= 0) {
      removeFromCart(key);
    } else if (newQuantity > (item.quantityInStock || 99)) {
      showToast('Số lượng vượt quá tồn kho!', 'danger');
    } else {
      item.quantity = newQuantity;
      renderCart();
    }
  }
}
function removeFromCart(key) {
  let cart = carts[activeOrder];
  if (!cart.items) return;
  cart.items = cart.items.filter(i => (i.variantId ? `${i.productId}_${i.variantId}` : `${i.productId}`) !== key);
  renderCart();
}
function calculateChange() {
  let cart = carts[activeOrder];
  if (!cart.items) cart.items = [];
  const total = cart.items.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );
  const customerPayment =
    parseFloat(document.getElementById("customerPayment").value) || 0;
  const change = customerPayment - total;
  document.getElementById("changeAmount").textContent =
    change >= 0 ? change.toLocaleString("vi-VN") + "đ" : "Chưa đủ tiền";
}
function openQRScanModal() {
  const modal = new bootstrap.Modal(
    document.getElementById("qrScanModal")
  );
  modal.show();

  // Khởi tạo scanner khi modal hiển thị
  document
    .getElementById("qrScanModal")
    .addEventListener("shown.bs.modal", function () {
      if (!html5QrcodeScanner) {
        html5QrcodeScanner = new Html5Qrcode("qr-reader");
        const config = { fps: 10, qrbox: { width: 250, height: 250 } };

        html5QrcodeScanner.start(
          { facingMode: "environment" },
          config,
          onScanSuccess,
          onScanFailure
        );
      }
    });

  // Dừng scanner khi đóng modal
  document
    .getElementById("qrScanModal")
    .addEventListener("hidden.bs.modal", function () {
      if (html5QrcodeScanner) {
        html5QrcodeScanner.stop().then(() => {
          html5QrcodeScanner = null;
        });
      }
    });
}
function onScanSuccess(decodedText, decodedResult) {
  // Xử lý kết quả quét QR
  try {
    const productData = JSON.parse(decodedText);
    if (productData.id && productData.name && productData.price) {
      // Nếu là sản phẩm có biến thể
      if (productVariants[productData.id]) {
        showVariantModal(
          productData.id,
          productData.name,
          productData.price,
          productData.category,
          productData.image
        );
      } else {
        // Nếu là sản phẩm thường
        addToCart(productData);
      }
      // Đóng modal sau khi quét thành công
      bootstrap.Modal.getInstance(
        document.getElementById("qrScanModal")
      ).hide();
      showToast("Quét mã thành công!");
    } else {
      showToast("Mã QR không hợp lệ!", "danger");
    }
  } catch (error) {
    showToast("Mã QR không hợp lệ!", "danger");
  }
}
function onScanFailure(error) {
  // Xử lý lỗi quét QR
  console.warn(`Lỗi quét QR: ${error}`);
}

// --- LOAD SẢN PHẨM TỪ API ---
async function loadProducts(keyword = '', categoryId = null, page = 0, size = 20) {
  let url = `/api/counter-sale/products?keyword=${encodeURIComponent(keyword)}`;
  if (categoryId) url += `&categoryId=${categoryId}`;
  url += `&page=${page}&size=${size}`;
  const res = await fetch(url);
  const products = await res.json();
  renderProductList(products);
}

// --- LOAD DANH MỤC SẢN PHẨM ---
async function loadCategories() {
  const res = await fetch('/api/counter-sale/products/categories');
  const categories = await res.json();
  const select = document.getElementById('filterCategory');
  select.innerHTML = '<option value="">Tất cả danh mục</option>';
  categories.forEach(cat => {
    select.innerHTML += `<option value="${cat.categoryId}">${cat.name}</option>`;
  });
}

// Khi load trang, lấy employeeId hiện tại
window.addEventListener('DOMContentLoaded', async () => {
  updateOrderTabs();
  renderCart();
  updateOrderLimitMsg();
  setCreatedTime();
  loadProducts("", null);
  try {
    const res = await fetch('/api/employees/me');
    if (res.ok) {
      const me = await res.json();
      currentEmployeeId = me.employeeId;
    }
  } catch (e) { }
});

function filterProducts() {
  const keyword = document.getElementById('searchProduct').value;
  const categoryId = document.getElementById('filterCategory').value || null;
  loadProducts(keyword, categoryId);
}

// Khai báo biến productVariants ở đầu script
let productVariants = {};

function renderProductList(products) {
  const productList = document.getElementById('productList');
  productList.innerHTML = '';
  // Reset biến productVariants
  productVariants = {};
  products.forEach(product => {
    if (product.variants && Array.isArray(product.variants)) {
      productVariants[product.productId] = product.variants;
    }
    let productQuantity = 0;
    if (product.variants && Array.isArray(product.variants)) {
      product.variants.forEach(variant => {
        productQuantity += variant.quantityInStock;
      });
    }
    productList.innerHTML += `
<div class="col-4">
  <div class="card product-card" onclick="onProductClick('${product.productId}')">
    <img src="${(product.images && product.images[0] && product.images[0].imageUrl) || ''}" class="card-img-top" alt="Product" />
    <div class="card-body text-center">
      <h6 class="card-title">${product.name}</h6>
      <p class="">${product.basePrice.toLocaleString()}đ</p>
      <p class=" text-muted">Tổng tồn kho: ${productQuantity || 0}</p>
    </div>
  </div>
</div>
`;
  });
}

// --- CHỌN SẢN PHẨM, LẤY CHI TIẾT VÀ BIẾN THỂ ---
async function onProductClick(productId) {
  const res = await fetch(`/api/counter-sale/products/${productId}`);
  const product = await res.json();
  if (product.variants && product.variants.length > 0) {
    showVariantModal(product);
  } else {
    // Nếu không có biến thể, thêm trực tiếp vào giỏ
    addToCart({
      productId: product.productId,
      name: product.name,
      price: product.basePrice,
      quantityInStock: product.quantityInStock || 99,
      imageUrl: (product.images && product.images[0] && product.images[0].imageUrl) || ''
    });
  }
}

function showVariantModal(product) {
  const modalId = `variantModal-${Date.now()}`;

  const html = `
  <div class='modal fade' id='${modalId}' tabindex='-1'>
    <div class='modal-dialog modal-lg'>
      <div class='modal-content'>
        <div class='modal-header'>
          <h5 class='modal-title'>Chọn biến thể cho <strong>${product.name}</strong></h5>
          <button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Close'></button>
        </div>
        <div class='modal-body'>
          <div class="container-fluid">
            <div class="row">
              ${product.variants.map(variant => `
                <div class="col-md-6 mb-3">
                  <input type="radio" class="variant-radio" name="variantRadio" id="variant${variant.variantId}" value="${variant.variantId}">
                  <label class="variant-option border" for="variant${variant.variantId}">
                    <div class="checkmark">✓</div>
                    <img src="${variant.images[0].imageUrl}" alt="Ảnh biến thể" style="width: 60px; height: 60px; object-fit: cover; border-radius: 6px;">
                    <div>
                      <div><strong>${variant.name || ''}</strong></div>
                      <div class="text-muted">Kích thước: ${variant.sizeName || ''}</div>
                      <div class="text-muted">Màu sắc: ${variant.colorName || ''}</div>
                      <div class="fw-bold text-primary">Giá: ${variant.price.toLocaleString()}đ</div>
                      <div class="text-secondary">Tồn kho: ${variant.quantityInStock}</div>
                    </div>
                  </label>
                </div>
              `).join('')}
            </div>
          </div>
        </div>
        <div class='modal-footer'>
          <button type='button' class='btn btn-secondary' data-bs-dismiss='modal'>Hủy</button>
          <button type='button' class='btn btn-primary' onclick='confirmVariantAddToCart(${JSON.stringify(product)})'>Thêm vào giỏ</button>
        </div>
      </div>
    </div>
  </div>`;

  document.body.insertAdjacentHTML('beforeend', html);
  const modal = new bootstrap.Modal(document.getElementById(modalId));
  modal.show();

  document.getElementById(modalId).addEventListener('hidden.bs.modal', function () {
    document.getElementById(modalId).remove();
  });
}


function confirmVariantAddToCart(product) {
  const checked = document.querySelector('input[name="variantRadio"]:checked');
  if (!checked) {
    alert('Vui lòng chọn biến thể!');
    return;
  }
  const variantId = checked.value;
  const variant = product.variants.find(v => v.variantId == variantId);
  addToCart({
    productId: product.productId,
    variantId: variant.variantId,
    name: variant.name,
    variantName: (variant.sizeName || '') + (variant.colorName ? ' - ' + variant.colorName : ''),
    size: variant.sizeName,
    color: variant.colorName,
    price: variant.price,
    quantityInStock: variant.quantityInStock,
    imageUrl: (product.images && product.images[0] && product.images[0].imageUrl) || '',
  });
  var modal = bootstrap.Modal.getInstance(document.getElementById('variantModal'));
  modal.hide();
}

// --- TỐI ƯU CHỌN KHÁCH HÀNG VÀ ĐỊA CHỈ ---
async function autoCompleteCustomer() {
  const keyword = document.getElementById('customerSearchInput').value.trim();
  const listBox = document.getElementById('customerAutoCompleteList');
  if (!keyword) {
    listBox.innerHTML = '';
    return;
  }
  // Gọi API tìm kiếm khách hàng
  const res = await fetch(`/api/counter-sale/customers/search?query=${encodeURIComponent(keyword)}`);
  const customers = await res.json();
  listBox.innerHTML = customers.length
    ? customers.map(c => `<a href='#' class='list-group-item list-group-item-action' onclick='selectCustomerUI(${JSON.stringify(c)})'>${c.name} - ${c.phoneNumber} - ${c.email || '---'}</a>`).join('')
    : "<div class='list-group-item text-muted'>Không tìm thấy khách hàng</div>";
}

function selectCustomerUI(cus) {
  selectedCustomer = cus;
  document.getElementById('customerSearchInput').value = `${cus.name} - ${cus.phoneNumber}`;
  document.getElementById('customerAutoCompleteList').innerHTML = '';
  // Hiển thị thông tin chi tiết
  document.getElementById('customerInfoBox').style.display = 'block';
  document.getElementById('customerInfoBox').innerHTML = `
    <div class="card p-2">
      <div><strong>${cus.name}</strong> (${cus.phoneNumber})</div>
      <div>Email: ${cus.email || '---'}</div>
      <div>Đã mua: ${cus.totalOrders || 0} đơn, Tổng chi: ${(cus.totalSpent || 0).toLocaleString()}đ</div>
      <button class="btn btn-link p-0" onclick="changeCustomer()">Chọn lại khách hàng</button>
    </div>
  `;
  // Lấy địa chỉ
  fetch(`/api/counter-sale/customers/${cus.customerId}/addresses`)
    .then(res => res.json())
    .then(addresses => {
      customerAddresses = addresses;
      renderCustomerAddresses();
    });
}

function changeCustomer() {
  selectedCustomer = null;
  document.getElementById('customerInfoBox').style.display = 'none';
  document.getElementById('customerSearchInput').value = '';
  document.getElementById('customerAutoCompleteList').innerHTML = '';
  document.getElementById('addressSelectBox').style.display = 'none';
  document.getElementById('shippingName').value = '';
  document.getElementById('shippingPhone').value = '';
  document.getElementById('addressDetail').value = '';
  document.getElementById('provinceSelect').value = '';
  document.getElementById('districtSelect').value = '';
  document.getElementById('wardSelect').value = '';
}

function renderCustomerAddresses() {
  const addressSelectBox = document.getElementById('addressSelectBox');
  const addressSelect = document.getElementById('customerAddressSelect');
  if (customerAddresses.length > 0) {
    addressSelectBox.style.display = 'block';
    addressSelect.innerHTML = customerAddresses.map(addr =>
      `<option value="${addr.shippingAddressId}">${addr.specificAddress}</option>`
    ).join('') + '<option value="">Nhập địa chỉ mới</option>';
    addressSelect.selectedIndex = 0;
    document.getElementById('shippingName').value = customerAddresses[0].name || '';
    document.getElementById('shippingPhone').value = customerAddresses[0].phone || '';
    document.getElementById('addressDetail').value = customerAddresses[0].specificAddress || '';
    document.getElementById('provinceSelect').value = customerAddresses[0].provinceId || '';
    fetchDistricts();
    setTimeout(() => {
      document.getElementById('districtSelect').value = customerAddresses[0].districtId || '';
      fetchWards();
      setTimeout(() => {
        document.getElementById('wardSelect').value = customerAddresses[0].wardId || '';
      }, 200);
    }, 200);
  } else {
    addressSelectBox.style.display = 'none';
    document.getElementById('shippingName').value = '';
    document.getElementById('shippingPhone').value = '';
    document.getElementById('addressDetail').value = '';
    document.getElementById('provinceSelect').value = '';
    document.getElementById('districtSelect').value = '';
    document.getElementById('wardSelect').value = '';
  }
}

function onSelectCustomerAddress() {
  const val = document.getElementById('customerAddressSelect').value;
  if (val) {
    // Chọn địa chỉ đã lưu
    const addr = customerAddresses.find(a => a.shippingAddressId == val);
    if (addr) {
      document.getElementById('shippingName').value = addr.name || '';
      document.getElementById('shippingPhone').value = addr.phone || '';
      document.getElementById('addressDetail').value = addr.specificAddress || '';
      document.getElementById('provinceSelect').value = addr.provinceId || '';
      fetchDistricts();
      setTimeout(() => {
        document.getElementById('districtSelect').value = addr.districtId || '';
        fetchWards();
        setTimeout(() => {
          document.getElementById('wardSelect').value = addr.wardId || '';
        }, 200);
      }, 200);
    }
    document.getElementById('provinceDistrictWardBox').style.display = '';
  } else {
    // Nhập địa chỉ mới
    document.getElementById('shippingName').value = '';
    document.getElementById('shippingPhone').value = '';
    document.getElementById('addressDetail').value = '';
    document.getElementById('provinceSelect').value = '';
    document.getElementById('districtSelect').value = '';
    document.getElementById('wardSelect').value = '';
    document.getElementById('provinceDistrictWardBox').style.display = '';
    fetchProvinces();
  }
}

function toggleCustomerSelection() {
  const isSelect = document.getElementById("selectCustomer").checked;
  document.getElementById("customerSearchBox").style.display = isSelect ? "block" : "none";
  document.getElementById("customerInfoBox").style.display = "none";
  document.getElementById('addressSelectBox').style.display = "none";
  document.getElementById('shippingName').value = '';
  document.getElementById('shippingPhone').value = '';
  document.getElementById('addressDetail').value = '';
  document.getElementById('provinceSelect').value = '';
  document.getElementById('districtSelect').value = '';
  document.getElementById('wardSelect').value = '';
  selectedCustomer = null;
}

// --- THÊM MỚI KHÁCH HÀNG ---
function showAddCustomerModal() {
  const modal = new bootstrap.Modal(document.getElementById('addCustomerModal'));
  modal.show();
}

async function submitAddCustomer() {
  const form = document.getElementById('addCustomerForm');
  const formData = new FormData(form);
  const customerData = Object.fromEntries(formData.entries());

  try {
    const res = await fetch('/api/counter-sale/customers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customerData)
    });
    const result = await res.json();
    if (result.status === 'ok') {
      showToast('Thêm khách hàng thành công!');
      bootstrap.Modal.getInstance(document.getElementById('addCustomerModal')).hide();
      form.reset();
      // Tự động chọn khách hàng vừa tạo
      selectCustomerUI(result.data);
    } else {
      showToast(result.message || 'Lỗi khi thêm khách hàng!', 'danger');
    }
  } catch (err) {
    showToast('Lỗi khi thêm khách hàng!', 'danger');
  }
}

function confirmPayment() {
  if (confirm('Bạn có chắc chắn muốn thanh toán hóa đơn này không?')) {
    completeTransaction();
  }
}

// Thêm hàm xử lý mã giảm giá
async function applyCoupon() {
  const code = document.getElementById('couponCode').value.trim();
  if (!code) return;
  // Gọi API kiểm tra mã giảm giá
  const res = await fetch(`/api/coupons/validate?code=${encodeURIComponent(code)}`);
  const data = await res.json();
  if (data && data.valid) {
    appliedCoupon = data.couponId;
    discountAmount = data.discountAmount || 0;
    document.getElementById('couponMessage').textContent = `Áp dụng thành công: Giảm ${discountAmount.toLocaleString()}đ`;
    renderCart(); // Cập nhật lại tổng tiền
  } else {
    appliedCoupon = null;
    discountAmount = 0;
    document.getElementById('couponMessage').textContent = 'Mã giảm giá không hợp lệ!';
  }
}