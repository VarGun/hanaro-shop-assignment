-- MySQL 8.x
SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- 유저
CREATE TABLE IF NOT EXISTS users (
                                     id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(20)  NOT NULL,             -- 'ADMIN' / 'USER'
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 상품
CREATE TABLE IF NOT EXISTS products (
                                        id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name           VARCHAR(200) NOT NULL UNIQUE,
    description    TEXT,
    price          INT          NOT NULL,
    stock_quantity INT          NOT NULL,
    image_url      VARCHAR(500),
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 장바구니(유저당 1개)
CREATE TABLE IF NOT EXISTS carts (
                                     id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     user_id     BIGINT      NOT NULL UNIQUE,
                                     created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 장바구니 아이템
CREATE TABLE IF NOT EXISTS cart_items (
                                          id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          cart_id    BIGINT   NOT NULL,
                                          product_id BIGINT   NOT NULL,
                                          quantity   INT      NOT NULL,
                                          CONSTRAINT uq_cart_item UNIQUE (cart_id, product_id),
    CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES carts(id)    ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 주문
CREATE TABLE IF NOT EXISTS orders (
                                      id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      user_id      BIGINT      NOT NULL,
                                      order_date   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      status       VARCHAR(20) NOT NULL,                 -- ORDERED / SHIPPING / COMPLETED / CANCELED
    total_price  INT         NOT NULL,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_orders_user (user_id),
    KEY idx_orders_status (status),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 주문 아이템
CREATE TABLE IF NOT EXISTS order_items (
                                           id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           order_id     BIGINT   NOT NULL,
                                           product_id   BIGINT   NOT NULL,
                                           product_name VARCHAR(200) NOT NULL,  -- 스냅샷용(선택), 없애도 무방
    unit_price   INT      NOT NULL,
    quantity     INT      NOT NULL,
    subtotal     INT      NOT NULL,
    CONSTRAINT uq_order_item UNIQUE (order_id, product_id),
    KEY idx_order_items_product (product_id),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 일별 매출 통계
CREATE TABLE IF NOT EXISTS daily_sales_stat (
                                                stat_date    DATE      NOT NULL PRIMARY KEY,
                                                order_count  BIGINT    NOT NULL,
                                                total_amount BIGINT    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 일별 상품 통계
CREATE TABLE IF NOT EXISTS daily_product_stat (
                                                  stat_date  DATE    NOT NULL,
                                                  product_id BIGINT  NOT NULL,
                                                  quantity   BIGINT  NOT NULL,
                                                  amount     BIGINT  NOT NULL,
                                                  PRIMARY KEY (stat_date, product_id),
    CONSTRAINT fk_dps_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;