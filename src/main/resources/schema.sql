-- USERS 테이블
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(255),
                       phone VARCHAR(50),
                       role VARCHAR(20) NOT NULL,
                       created_at DATETIME NOT NULL,
                       updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PRODUCTS 테이블
CREATE TABLE products (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255),
                          price INT,
                          description TEXT,
                          stock_quantity INT,
                          image_url VARCHAR(500),
                          created_at DATETIME NOT NULL,
                          updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CARTS 테이블
CREATE TABLE carts (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_id BIGINT NOT NULL UNIQUE,
                       created_at DATETIME NOT NULL,
                       updated_at DATETIME NOT NULL,
                       CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CART_ITEMS 테이블
CREATE TABLE cart_items (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            cart_id BIGINT NOT NULL,
                            product_id BIGINT NOT NULL,
                            quantity INT NOT NULL,
                            created_at DATETIME NOT NULL,
                            updated_at DATETIME NOT NULL,
                            CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
                            CONSTRAINT fk_cartitem_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ORDERS 테이블
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        order_date DATETIME,
                        status ENUM('ORDERED', 'READY', 'SHIPPING', 'COMPLETED', 'CANCELED'),
                        total_price INT,
                        status_changed_at DATETIME NOT NULL,
                        updated_at DATETIME NOT NULL,
                        created_at DATETIME NOT NULL,
                        CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ORDER_ITEMS 테이블
CREATE TABLE order_items (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             price INT NOT NULL,
                             created_at DATETIME NOT NULL,
                             updated_at DATETIME NOT NULL,
                             CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- DAILY_PRODUCT_STAT 테이블 (복합키)
CREATE TABLE daily_product_stat (
                                    stat_date DATE NOT NULL,
                                    product_id BIGINT NOT NULL,
                                    quantity BIGINT NOT NULL,
                                    amount BIGINT NOT NULL,
                                    created_at DATETIME NOT NULL,
                                    updated_at DATETIME NOT NULL,
                                    PRIMARY KEY (stat_date, product_id),
                                    CONSTRAINT fk_dailyproduct_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- DAILY_SALES_STAT 테이블
CREATE TABLE daily_sales_stat (
                                  stat_date DATE PRIMARY KEY,
                                  order_count BIGINT NOT NULL,
                                  total_amount BIGINT NOT NULL,
                                  created_at DATETIME NOT NULL,
                                  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;