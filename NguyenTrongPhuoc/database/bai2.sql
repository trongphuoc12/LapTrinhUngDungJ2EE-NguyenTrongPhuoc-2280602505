-- Import this file in phpMyAdmin to create database and sample data for bai2 app.
CREATE DATABASE IF NOT EXISTS bai2
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bai2;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL UNIQUE,
  description VARCHAR(500),
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sku VARCHAR(60) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(500),
  category_id BIGINT NOT NULL,
  active BIT(1) NOT NULL DEFAULT b'1',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_products_category
    FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS price_entries (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  price_type VARCHAR(30) NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'VND',
  effective_date DATE NOT NULL,
  note VARCHAR(255),
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_price_entries_product
    FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categories (name, description, created_at) VALUES
  ('Điện tử', 'Thiết bị điện tử và phụ kiện', NOW()),
  ('Gia dụng', 'Sản phẩm dùng trong gia đình', NOW()),
  ('Văn phòng', 'Thiết bị và vật tư văn phòng', NOW());

INSERT INTO products (sku, name, description, category_id, active, created_at, updated_at) VALUES
  ('IP15-128-BLK', 'iPhone 15 128GB', 'Phiên bản màu đen, chính hãng VN/A', 1, b'1', NOW(), NOW()),
  ('AIR-FRY-06L', 'Nồi chiên không dầu 6L', 'Dung tích lớn, công nghệ Rapid Air', 2, b'1', NOW(), NOW()),
  ('PRINT-LASER-A4', 'Máy in laser A4', 'In nhanh 30 trang/phút, kết nối LAN', 3, b'1', NOW(), NOW());

INSERT INTO price_entries (product_id, price_type, price, currency, effective_date, note, created_at) VALUES
  (1, 'RETAIL', 21990000, 'VND', CURDATE(), 'Giá niêm yết cửa hàng', NOW()),
  (1, 'ONLINE', 21490000, 'VND', CURDATE(), 'Áp dụng kênh online', NOW()),
  (2, 'RETAIL', 2890000, 'VND', CURDATE(), 'Giá bán lẻ tiêu chuẩn', NOW()),
  (3, 'WHOLESALE', 3450000, 'VND', CURDATE(), 'Giá đại lý từ 10 sản phẩm', NOW());
