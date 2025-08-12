-- MySQL 8.x
SET NAMES utf8mb4;
SET time_zone = '+00:00';

---
-- USERS
INSERT INTO users (email, password, name, phone, role, created_at, updated_at)
VALUES ('hanaro@hanaro.com', '$2a$10$mg9M8jDSw0RD9AURf71LweJ9DxK658T5AbWnEf2imKcvAliQ4FEii',
        'ADMIN',
        '010-9999-0000', 'ADMIN',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (email, password, name, phone, role, created_at, updated_at)
VALUES ('user1@aaa.com', '$2a$10$aRIfhIMfuTRlKH./bXdhn.G2tADYe1p7t8vwM1p/oSUtigEk3yRMu',
        'USER1', '010-0000-0000', 'USER',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---
-- PRODUCTS
INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at)
VALUES ('MacBook Pro', 'MacBook Pro description', 2000000, 50, '/origin/image1.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at)
VALUES ('iPad Pro', 'iPad Pro description', 1200000, 100, '/origin/image2.jpeg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at)
VALUES ('Apple Watch', 'Apple Watch description', 500000, 200, '/origin/image3.jpeg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---
-- CARTS
-- user_id 1 (hanaro@hanaro.com)의 카트
INSERT INTO carts (user_id, created_at, updated_at)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- user_id 2 (user1@aaa.com)의 카트
INSERT INTO carts (user_id, created_at, updated_at)
VALUES (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---
-- CART_ITEMS
-- user_id 1의 카트(cart_id=1)에 상품 추가
INSERT INTO cart_items (cart_id, product_id, quantity, created_at, updated_at)
VALUES (1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- 상품 1 (MacBook Pro) 1개

INSERT INTO cart_items (cart_id, product_id, quantity, created_at, updated_at)
VALUES (1, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- 상품 3 (Apple Watch) 2개

-- user_id 2의 카트(cart_id=2)에 상품 추가
INSERT INTO cart_items (cart_id, product_id, quantity, created_at, updated_at)
VALUES (2, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- 상품 2 (iPad Pro) 1개

---
-- ORDERS
-- user_id 2의 주문 (ORDERED 상태)
INSERT INTO orders (user_id, order_date, status, total_price, status_changed_at, created_at, updated_at)
VALUES (2, CURRENT_TIMESTAMP, 'ORDERED', 2000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- user_id 2의 다른 주문 (CANCELED 상태)
INSERT INTO orders (user_id, order_date, status, total_price, status_changed_at, created_at, updated_at)
VALUES (2, CURRENT_TIMESTAMP, 'CANCELED', 1000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---
-- ORDER_ITEMS
-- order_id 1의 주문에 대한 주문 아이템
INSERT INTO order_items (order_id, product_id, quantity, price, created_at, updated_at)
VALUES (1, 1, 1, 2000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- order_id 2의 주문에 대한 주문 아이템
INSERT INTO order_items (order_id, product_id, quantity, price, created_at, updated_at)
VALUES (2, 3, 2, 500000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---
-- DAILY_SALES_STAT
-- 전날(예시) 완료된 주문에 대한 통계
INSERT INTO daily_sales_stat (stat_date, order_count, total_amount, created_at, updated_at)
VALUES ('2025-08-11', 1, 2000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---
-- DAILY_PRODUCT_STAT
-- 전날(예시) 상품별 통계
INSERT INTO daily_product_stat (stat_date, product_id, quantity, amount, created_at, updated_at)
VALUES ('2025-08-11', 1, 1, 2000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);