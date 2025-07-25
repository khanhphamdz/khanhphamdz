USE master;
GO

-- Tạo database
IF DB_ID('DATN_TeeShirtVibe2') IS NOT NULL
    DROP DATABASE DATN_TeeShirtVibe2;
GO
CREATE DATABASE DATN_TeeShirtVibe2;
GO
USE DATN_TeeShirtVibe2;
GO

-- 1. Bảng Categories (Danh mục)
CREATE TABLE Categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL,
    parent_id INT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    deleted_at DATETIME,
    FOREIGN KEY (parent_id) REFERENCES Categories(category_id) ON DELETE NO ACTION
);
-- 4. Bảng Colors (Màu sắc)
CREATE TABLE Colors (
    color_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL,
    hex_code NVARCHAR(7) NOT NULL,
    created_at DATETIME DEFAULT GETDATE()
);

-- 5. Bảng Sizes (Kích cỡ)
CREATE TABLE Sizes (
    size_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(10) NOT NULL,
    created_at DATETIME DEFAULT GETDATE()
);

-- 6. Bảng Materials (Chất liệu)
CREATE TABLE Materials (
    material_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT GETDATE()
);
-- 2. Bảng Products (Sản phẩm)
CREATE TABLE Products (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(MAX),
    short_description NVARCHAR(MAX),
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price >= 0),
	material_id INT NOT NULL,
    status BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (material_id) REFERENCES Materials(material_id) ON DELETE NO ACTION,
);

-- 3. Bảng Product_Categories (Danh mục sản phẩm)
CREATE TABLE Product_Categories (
    product_category_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    category_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE CASCADE
);

-- 7. Bảng Product_Variants (Biến thể sản phẩm)
CREATE TABLE Product_Variants (
    variant_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    name NVARCHAR(100) NOT NULL,
    sku NVARCHAR(50) UNIQUE,
    barcode NVARCHAR(50) UNIQUE,
    color_id INT NOT NULL,
    size_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    discount_price DECIMAL(10,2) CHECK (discount_price >= 0 OR discount_price IS NULL),
    discount_price_start_at DATETIME,
    discount_price_end_at DATETIME,
    quantity_in_stock INT NOT NULL DEFAULT 0 CHECK (quantity_in_stock >= 0),
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (color_id) REFERENCES Colors(color_id) ON DELETE NO ACTION,
    FOREIGN KEY (size_id) REFERENCES Sizes(size_id) ON DELETE NO ACTION,
    CONSTRAINT CHK_Discount_Dates CHECK (discount_price_end_at IS NULL OR discount_price_start_at <= discount_price_end_at)
);

-- 8. Bảng Product_Images (Hình ảnh sản phẩm)
CREATE TABLE Product_Images (
    image_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT,
    variant_id INT NULL,
    image_url NVARCHAR(MAX) NOT NULL,
    image_type NVARCHAR(50) NOT NULL DEFAULT 'thumbnail' CHECK (image_type IN ('thumbnail', 'variant')),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE NO ACTION,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE NO ACTION
);

-- 9. Bảng Customer (Giữ nguyên)
CREATE TABLE Customer (
    customer_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL,
    avatar_url NVARCHAR(255),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 10. Bảng Customer_Address (Giữ nguyên)
CREATE TABLE Customer_Address (
    address_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    province_id VARCHAR(10) NOT NULL,
    district_id VARCHAR(10) NOT NULL,
    ward_id VARCHAR(10) NOT NULL,
    specific_address NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);

-- 11. Bảng Carts (Giữ nguyên)
CREATE TABLE Carts (
    cart_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);

-- 12. Bảng Cart_Items (Giữ nguyên)
CREATE TABLE Cart_Items (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cart_id INT NOT NULL,
    variant_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (cart_id) REFERENCES Carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE CASCADE
);

-- 13. Bảng Employees (Giữ nguyên)
CREATE TABLE Employees (
    employee_id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    gender BIT NOT NULL,
    phone_number NVARCHAR(20),
    email NVARCHAR(100) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL,
    birthday DATE,
    address NVARCHAR(255),
    hire_date DATE,
    status VARCHAR(20) CHECK (status IN ('active', 'inactive', 'terminated')),
    citizen_id NVARCHAR(12) UNIQUE,
    role BIT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 14. Bảng Coupons (Giữ nguyên)
CREATE TABLE Coupons (
    coupon_id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) UNIQUE NOT NULL,
    description NVARCHAR(500),
    discount_value DECIMAL(10,2) NOT NULL CHECK (discount_value >= 0),
    min_order_value DECIMAL(10,2) CHECK (min_order_value >= 0 OR min_order_value IS NULL),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    max_usage INT CHECK (max_usage > 0 OR max_usage IS NULL),
    usage_count INT DEFAULT 0 CHECK (usage_count >= 0),
    is_active BIT DEFAULT 1,
    apply_to_customer VARCHAR(20) NOT NULL DEFAULT 'all' CHECK (apply_to_customer IN ('all', 'new', 'specific')),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 15. Bảng Orders (Giữ nguyên)
CREATE TABLE Orders (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NULL,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    shipping_fee DECIMAL(10,2) DEFAULT 0 CHECK (shipping_fee >= 0),
    discount_amount DECIMAL(10,2) DEFAULT 0 CHECK (discount_amount >= 0),
    final_amount DECIMAL(10,2) NOT NULL CHECK (final_amount >= 0),
    coupon_id INT,
    order_type VARCHAR(20) NOT NULL CHECK (order_type IN ('online', 'offline')),
    employee_id INT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'shipped', 'delivered', 'cancelled')),
    note NVARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE SET NULL,
    FOREIGN KEY (coupon_id) REFERENCES Coupons(coupon_id) ON DELETE SET NULL,
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id) ON DELETE SET NULL
);

-- 16. Bảng Shipping_Address (Giữ nguyên)
CREATE TABLE Shipping_Address (
    shipping_address_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    province_id VARCHAR(10) NOT NULL,
    district_id VARCHAR(10) NOT NULL,
    ward_id VARCHAR(10) NOT NULL,
    specific_address NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    note NVARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
);

-- 17. Bảng Order_Items (Giữ nguyên)
CREATE TABLE Order_Items (
    order_item_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    variant_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    quantity INT NOT NULL CHECK (quantity > 0),
    price_at_purchase DECIMAL(10,2) NOT NULL CHECK (price_at_purchase >= 0),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE CASCADE
);

-- 18. Bảng Order_Status (Giữ nguyên)
CREATE TABLE Order_Status (
    order_status_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    status_name NVARCHAR(50) NOT NULL CHECK (status_name IN ('pending', 'processing', 'shipped', 'delivered', 'cancelled')),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
);

-- 19. Bảng Payments (Giữ nguyên)
CREATE TABLE Payments (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('COD', 'VNPAY')),
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    transaction_id NVARCHAR(100),
    payment_status VARCHAR(20) NOT NULL CHECK (payment_status IN ('pending', 'completed', 'failed', 'refunded')),
    payment_date DATETIME,
    payment_details NVARCHAR(1000),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
);

-- 20. Bảng Promotions (Giữ nguyên)
CREATE TABLE Promotions (
    promotion_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    discount_value DECIMAL(10,2) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('percentage', 'fixed')),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 21. Bảng Promotion_Products (Giữ nguyên)
CREATE TABLE Promotion_Products (
    promotion_id INT NOT NULL,
    product_id INT,
    variant_id INT,
    created_at DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (promotion_id, product_id, variant_id),
    FOREIGN KEY (promotion_id) REFERENCES Promotions(promotion_id) ON DELETE NO ACTION,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE NO ACTION,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE NO ACTION
);

-- 22. Bảng Reviews (Giữ nguyên)
CREATE TABLE Reviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

-- 23. Bảng ReturnRequest (Giữ nguyên)
CREATE TABLE ReturnRequest (
    return_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    customer_id INT NOT NULL,
    return_reason NVARCHAR(1000) NOT NULL,
    return_status VARCHAR(20) NOT NULL CHECK (return_status IN ('pending', 'approved', 'rejected', 'completed', 'cancelled')),
    return_type VARCHAR(20) NOT NULL CHECK (return_type IN ('refund', 'exchange')),
    refund_amount DECIMAL(10,2) CHECK (refund_amount >= 0 OR refund_amount IS NULL),
    refund_status VARCHAR(20) CHECK (refund_status IN ('pending', 'completed', 'failed')),
    exchange_product_id INT,
    exchange_variant_id INT,
    return_quantity INT NOT NULL CHECK (return_quantity > 0),
    return_images NVARCHAR(MAX),
    return_note NVARCHAR(1000),
    admin_note NVARCHAR(1000),
    request_date DATETIME DEFAULT GETDATE(),
    processed_date DATETIME,
    processed_by INT,
    completed_date DATETIME,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES Employees(employee_id) ON DELETE SET NULL,
    FOREIGN KEY (exchange_variant_id) REFERENCES Product_Variants(variant_id) ON DELETE SET NULL
);

-- 24. Bảng ReturnRequest_Items (Giữ nguyên)
CREATE TABLE ReturnRequest_Items (
    return_item_id INT IDENTITY(1,1) PRIMARY KEY,
    return_id INT NOT NULL,
    variant_id INT NOT NULL,
    return_quantity INT NOT NULL CHECK (return_quantity > 0),
    return_reason NVARCHAR(500),
    product_condition VARCHAR(20) NOT NULL CHECK (product_condition IN ('new', 'used', 'damaged')),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (return_id) REFERENCES ReturnRequest(return_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE CASCADE
);

-- 25. Bảng Wishlist (Giữ nguyên)
CREATE TABLE Wishlist (
    wishlist_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);

-- 26. Bảng Wishlist_Item (Giữ nguyên)
CREATE TABLE Wishlist_Item (
    wishlist_item_id INT IDENTITY(1,1) PRIMARY KEY,
    wishlist_id INT NOT NULL,
    product_id INT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (wishlist_id) REFERENCES Wishlist(wishlist_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

GO
/*
delete from Product_Images
delete from Product_Variants
delete from Product_Categories
delete from 
Sizes
delete from 
Materials
delete from 
Colors
delete from 
Categories
delete from 
Products
*/

-- 1. Thêm dữ liệu vào bảng Categories (tách danh mục cha và con)
-- Thêm danh mục cha trước (parent_id = NULL)
INSERT INTO Categories (name, parent_id) VALUES
(N'Áo Thun Nam', NULL),
(N'Áo Thun Nữ', NULL),
(N'Áo Thun Unisex', NULL);

-- Thêm danh mục con sau (tham chiếu đến category_id đã tồn tại)
INSERT INTO Categories (name, parent_id) VALUES
(N'Áo Thun In Hình', 1), -- parent_id = 1 (Áo Thun Nam)
(N'Áo Thun Trơn', 1),    -- parent_id = 1 (Áo Thun Nam)
(N'Áo Thun Cặp', 3);      -- parent_id = 3 (Áo Thun Unisex)

-- 2. Thêm dữ liệu vào bảng Colors
INSERT INTO Colors (name, hex_code) VALUES
(N'Đen', '#000000'),
(N'Trắng', '#FFFFFF'),
(N'Xanh Navy', '#1B263B'),
(N'Đỏ', '#FF0000'),
(N'Xám', '#808080');

-- 3. Thêm dữ liệu vào bảng Sizes
INSERT INTO Sizes (name) VALUES
(N'S'),
(N'M'),
(N'L'),
(N'XL');

-- 4. Thêm dữ liệu vào bảng Materials
INSERT INTO Materials (name) VALUES
(N'Cotton 100%'),
(N'Polyester'),
(N'Cotton pha');

-- 5. Thêm dữ liệu vào bảng Products (sử dụng category_id từ 1 đến 6)
INSERT INTO Products (name, description, short_description, base_price, material_id, status) VALUES
(N'Áo Thun In Hình Superhero', N'Áo thun nam in hình siêu anh hùng chất lượng cao, thoáng mát.', N'Áo thun in hình siêu anh hùng.', 250000, 1, 1),
(N'Áo Thun Trơn Nam Classic', N'Áo thun trơn nam phong cách tối giản, dễ phối đồ.', N'Áo thun trơn nam, phong cách tối giản.', 180000, 2, 1),
(N'Áo Thun Cặp Tình Nhân', N'Áo thun cặp dành cho các cặp đôi, thiết kế đáng yêu.', N'Áo thun cặp đôi, thiết kế dễ thương.', 300000, 3, 1),
(N'Áo Thun Nữ Form Rộng', N'Áo thun nữ form rộng, thoải mái, phù hợp mọi dáng người.', N'Áo thun nữ form rộng, chất cotton pha.', 220000, 1, 1),
(N'Áo Thun Unisex Vintage', N'Áo thun unisex phong cách vintage, phù hợp cả nam và nữ.', N'Áo thun unisex vintage, chất liệu cao cấp.', 270000, 2, 1),
(N'Áo Thun In Hình Anime', N'Áo thun in hình nhân vật anime nổi tiếng, dành cho fan hâm mộ.', N'Áo thun anime, chất cotton 100%.', 260000, 3, 1),
(N'Áo Thun Trơn Nữ Basic', N'Áo thun trơn nữ, thiết kế đơn giản, dễ phối đồ.', N'Áo thun trơn nữ, chất liệu cotton pha.', 190000, 1, 1),
(N'Áo Thun Nam Thể Thao', N'Áo thun nam thể thao, thấm hút mồ hôi tốt.', N'Áo thun thể thao nam, chất polyester.', 200000, 2, 1),
(N'Áo Thun Unisex Oversize', N'Áo thun unisex oversize, phong cách trẻ trung, năng động.', N'Áo thun oversize, chất cotton.', 280000, 3, 1),
(N'Áo Thun In Hình Game', N'Áo thun in hình nhân vật game, dành cho game thủ.', N'Áo thun game, chất cotton 100%.', 265000, 1, 1),
(N'Áo Thun Cặp Gia Đình', N'Áo thun cặp dành cho gia đình, thiết kế đồng bộ.', N'Áo thun gia đình, chất liệu cotton pha.', 320000, 2, 1),
(N'Áo Thun Nữ Croptop', N'Áo thun croptop nữ, phong cách trẻ trung, năng động.', N'Áo croptop nữ, chất cotton.', 210000, 3, 1),
(N'Áo Thun Nam In Slogan', N'Áo thun nam in slogan cá tính, phong cách đường phố.', N'Áo thun nam in slogan, chất cotton.', 255000, 1, 1),
(N'Áo Thun Unisex Retro', N'Áo thun unisex phong cách retro, chất liệu mềm mại.', N'Áo thun retro, chất cotton pha.', 275000, 2, 1),
(N'Áo Thun Trẻ Em', N'Áo thun dành cho trẻ em, thiết kế dễ thương, an toàn cho da.', N'Áo thun trẻ em, chất cotton 100%.', 150000, 3, 1);

-- 6. Thêm dữ liệu vào bảng Product_Categories (sử dụng product_id từ 1 đến 15, category_id từ 1 đến 6)
INSERT INTO Product_Categories (product_id, category_id) VALUES
(1, 4), (2, 5), (3, 6), (4, 2), (5, 3),
(6, 4), (7, 2), (8, 1), (9, 3), (10, 4),
(11, 6), (12, 2), (13, 4), (14, 3), (15, 3);

-- 7. Thêm dữ liệu vào bảng Product_Variants (sử dụng product_id từ 1 đến 15, color_id từ 1 đến 5, size_id từ 1 đến 4, material_id từ 1 đến 3)
INSERT INTO Product_Variants (product_id, name, sku, barcode, color_id, size_id, price, discount_price, quantity_in_stock, is_active) VALUES
-- Sản phẩm 1: Áo Thun In Hình Superhero
(1, N'Áo Superhero Đen Size M', 'SUPERHERO-BLACK-M', 'BAR001', 1, 2, 250000, NULL, 50, 1),
(1, N'Áo Superhero Trắng Size L', 'SUPERHERO-WHITE-L', 'BAR002', 2, 3, 250000, 230000, 30, 1),
(1, N'Áo Superhero Đỏ Size S', 'SUPERHERO-RED-S', 'BAR003', 4, 1, 250000, NULL, 40, 1),
-- Sản phẩm 2: Áo Thun Trơn Nam Classic
(2, N'Áo Trơn Đen Size M', 'PLAIN-BLACK-M', 'BAR004', 1, 2, 180000, NULL, 60, 1),
(2, N'Áo Trơn Trắng Size L', 'PLAIN-WHITE-L', 'BAR005', 2, 3, 180000, NULL, 50, 1),
-- Sản phẩm 3: Áo Thun Cặp Tình Nhân
(3, N'Áo Cặp Đen Size S', 'COUPLE-BLACK-S', 'BAR006', 1, 1, 300000, 280000, 20, 1),
(3, N'Áo Cặp Trắng Size M', 'COUPLE-WHITE-M', 'BAR007', 2, 2, 300000, NULL, 25, 1),
-- Sản phẩm 4: Áo Thun Nữ Form Rộng
(4, N'Áo Form Rộng Xám Size M', 'WIDE-GREY-M', 'BAR008', 5, 2, 220000, NULL, 35, 1),
(4, N'Áo Form Rộng Trắng Size L', 'WIDE-WHITE-L', 'BAR009', 2, 3, 220000, 200000, 30, 1),
-- Sản phẩm 5: Áo Thun Unisex Vintage
(5, N'Áo Vintage Navy Size L', 'VINTAGE-NAVY-L', 'BAR010', 3, 3, 270000, NULL, 40, 1),
(5, N'Áo Vintage Đen Size M', 'VINTAGE-BLACK-M', 'BAR011', 1, 2, 1270000, NULL, 45, 1),
-- Sản phẩm 6: Áo Thun In Hình Anime
(6, N'Áo Anime Trắng Size S', 'ANIME-WHITE-S', 'BAR012', 2, 1, 260000, NULL, 50, 1),
(6, N'Áo Anime Đen Size M', 'ANIME-BLACK-M', 'BAR013', 1, 2, 260000, 240000, 30, 1),
-- Sản phẩm 7: Áo Thun Trơn Nữ Basic
(7, N'Áo Trơn Nữ Đỏ Size S', 'PLAINF-RED-S', 'BAR014', 4, 1, 190000, NULL, 40, 1),
(7, N'Áo Trơn Nữ Trắng Size M', 'PLAINF-WHITE-M', 'BAR015', 2, 2, 190000, NULL, 35, 1),
-- Sản phẩm 8: Áo Thun Nam Thể Thao
(8, N'Áo Thể Thao Đen Size L', 'SPORT-BLACK-L', 'BAR016', 1, 3, 200000, NULL, 50, 1),
(8, N'Áo Thể Thao Xanh Size M', 'SPORT-NAVY-M', 'BAR017', 3, 2, 200000, 180000, 40, 1),
-- Sản phẩm 9: Áo Thun Unisex Oversize
(9, N'Áo Oversize Trắng Size XL', 'OVERSIZE-WHITE-XL', 'BAR018', 2, 4, 1280000, NULL, 30, 1),
(9, N'Áo Oversize Đen Size L', 'OVERSIZE-BLACK-L', 'BAR019', 1, 3, 1280000, NULL, 35, 1),
-- Sản phẩm 10: Áo Thun In Hình Game
(10, N'Áo Game Đen Size M', 'GAME-BLACK-M', 'BAR020', 1, 2, 1265000, 245000, 25, 1),
(10, N'Áo Game Trắng Size L', 'GAME-WHITE-L', 'BAR021', 2, 3, 1265000, NULL, 30, 1),
-- Sản phẩm 11: Áo Thun Cặp Gia Đình
(11, N'Áo Gia Đình Đen Size S', 'FAMILY-BLACK-S', 'BAR022', 1, 1, 320000, NULL, 20, 1),
(11, N'Áo Gia Đình Trắng Size M', 'FAMILY-WHITE-M', 'BAR023', 2, 2, 3320000, NULL, 25, 1),
-- Sản phẩm 12: Áo Thun Nữ Croptop
(12, N'Áo Croptop Đỏ Size S', 'CROPTOP-RED-S', 'BAR024', 4, 1, 1210000, NULL, 30, 1),
(12, N'Áo Croptop Trắng Size M', 'CROPTOP-WHITE-M', 'BAR025', 2, 2, 210000, 190000, 25, 1),
-- Sản phẩm 13: Áo Thun Nam In Slogan
(13, N'Áo Slogan Đen Size M', 'SLOGAN-BLACK-M', 'BAR026', 1, 2, 1255000, NULL, 35, 1),
(13, N'Áo Slogan Xám Size L', 'SLOGAN-GREY-L', 'BAR027', 5, 3, 255000, NULL, 30, 1),
-- Sản phẩm 14: Áo Thun Unisex Retro
(14, N'Áo Retro Navy Size M', 'RETRO-NAVY-M', 'BAR028', 3, 2, 3275000, NULL, 40, 1),
(14, N'Áo Retro Trắng Size L', 'RETRO-WHITE-L', 'BAR029', 2, 3, 275000, 255000, 35, 1),
-- Sản phẩm 15: Áo Thun Trẻ Em
(15, N'Áo Trẻ Em Đen Size S', 'KIDS-BLACK-S', 'BAR030', 1, 1, 1150000, NULL, 50, 1),
(15, N'Áo Trẻ Em Trắng Size M', 'KIDS-WHITE-M', 'BAR031', 2, 2, 150000, NULL, 45, 1);

-- 8. Thêm dữ liệu vào bảng Product_Images (sử dụng product_id từ 1 đến 15, variant_id từ 1 đến 31)
INSERT INTO Product_Images (product_id, variant_id, image_url, image_type) VALUES
-- Sản phẩm 1
(1, 1, 'https://example.com/images/superhero_black_m.jpg', 'variant'),
(1, 2, 'https://example.com/images/superhero_white_l.jpg', 'variant'),
(1, 3, 'https://example.com/images/superhero_red_s.jpg', 'variant'),
(1, NULL, 'https://example.com/images/superhero_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 2
(2, 4, 'https://example.com/images/plain_black_m.jpg', 'variant'),
(2, 5, 'https://example.com/images/plain_white_l.jpg', 'variant'),
(2, NULL, 'https://example.com/images/plain_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 3
(3, 6, 'https://example.com/images/couple_black_s.jpg', 'variant'),
(3, 7, 'https://example.com/images/couple_white_m.jpg', 'variant'),
(3, NULL, 'https://example.com/images/couple_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 4
(4, 8, 'https://example.com/images/wide_grey_m.jpg', 'variant'),
(4, 9, 'https://example.com/images/wide_white_l.jpg', 'variant'),
(4, NULL, 'https://example.com/images/wide_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 5
(5, 10, 'https://example.com/images/vintage_navy_l.jpg', 'variant'),
(5, 11, 'https://example.com/images/vintage_black_m.jpg', 'variant'),
(5, NULL, 'https://example.com/images/vintage_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 6
(6, 12, 'https://example.com/images/anime_white_s.jpg', 'variant'),
(6, 13, 'https://example.com/images/anime_black_m.jpg', 'variant'),
(6, NULL, 'https://example.com/images/anime_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 7
(7, 14, 'https://example.com/images/plainf_red_s.jpg', 'variant'),
(7, 15, 'https://example.com/images/plainf_white_m.jpg', 'variant'),
(7, NULL, 'https://example.com/images/plainf_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 8
(8, 16, 'https://example.com/images/sport_black_l.jpg', 'variant'),
(8, 17, 'https://example.com/images/sport_navy_m.jpg', 'variant'),
(8, NULL, 'https://example.com/images/sport_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 9
(9, 18, 'https://example.com/images/oversize_white_xl.jpg', 'variant'),
(9, 19, 'https://example.com/images/oversize_black_l.jpg', 'variant'),
(9, NULL, 'https://example.com/images/oversize_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 10
(10, 20, 'https://example.com/images/game_black_m.jpg', 'variant'),
(10, 21, 'https://example.com/images/game_white_l.jpg', 'variant'),
(10, NULL, 'https://example.com/images/game_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 11
(11, 22, 'https://example.com/images/family_black_s.jpg', 'variant'),
(11, 23, 'https://example.com/images/family_white_m.jpg', 'variant'),
(11, NULL, 'https://example.com/images/family_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 12
(12, 24, 'https://example.com/images/croptop_red_s.jpg', 'variant'),
(12, 25, 'https://example.com/images/croptop_white_m.jpg', 'variant'),
(12, NULL, 'https://example.com/images/croptop_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 13
(13, 26, 'https://example.com/images/slogan_black_m.jpg', 'variant'),
(13, 27, 'https://example.com/images/slogan_grey_l.jpg', 'variant'),
(13, NULL, 'https://example.com/images/slogan_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 14
(14, 28, 'https://example.com/images/retro_navy_m.jpg', 'variant'),
(14, 29, 'https://example.com/images/retro_white_l.jpg', 'variant'),
(14, NULL, 'https://example.com/images/retro_thumbnail.jpg', 'thumbnail'),
-- Sản phẩm 15
(15, 30, 'https://example.com/images/kids_black_s.jpg', 'variant'),
(15, 31, 'https://example.com/images/kids_white_m.jpg', 'variant'),
(15, NULL, 'https://example.com/images/kids_thumbnail.jpg', 'thumbnail');

GO

-- 1. Thêm cột phone vào bảng Customer
IF COL_LENGTH('Customer', 'phone') IS NULL
BEGIN
    ALTER TABLE Customer
    ADD phone NVARCHAR(20);
END

-- 2. Thêm cột is_default vào bảng Customer_Address
IF COL_LENGTH('Customer_Address', 'is_default') IS NULL
BEGIN
    ALTER TABLE Customer_Address
    ADD is_default BIT DEFAULT 0;
END

-- 3. Trigger: Đảm bảo mỗi customer chỉ có 1 địa chỉ is_default = 1
IF OBJECT_ID('trg_Ensure_One_Default_Address', 'TR') IS NOT NULL
BEGIN
    DROP TRIGGER trg_Ensure_One_Default_Address;
END
GO

CREATE TRIGGER trg_Ensure_One_Default_Address
ON Customer_Address
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Lấy các customer_id vừa bị insert/update mà có địa chỉ mặc định
    WITH NewDefaults AS (
        SELECT customer_id, address_id
        FROM inserted
        WHERE is_default = 1
    )
    UPDATE Customer_Address
    SET is_default = 0
    WHERE customer_id IN (SELECT customer_id FROM NewDefaults)
      AND address_id NOT IN (SELECT address_id FROM NewDefaults)
      AND is_default = 1;
END
GO

-- 4. Cập nhật địa chỉ đầu tiên làm mặc định (nếu chưa có)
UPDATE ca
SET is_default = 1
FROM Customer_Address ca
WHERE ca.address_id = (
    SELECT MIN(ca2.address_id)
    FROM Customer_Address ca2
    WHERE ca2.customer_id = ca.customer_id
)
AND NOT EXISTS (
    SELECT 1 FROM Customer_Address ca3
    WHERE ca3.customer_id = ca.customer_id
      AND ca3.is_default = 1
);

-- 1. Thêm trường type vào bảng Coupons
IF COL_LENGTH('Coupons', 'type') IS NULL
BEGIN
    ALTER TABLE Coupons
    ADD type VARCHAR(20) NOT NULL DEFAULT 'fixed' CHECK (type IN ('percentage', 'fixed'));
END