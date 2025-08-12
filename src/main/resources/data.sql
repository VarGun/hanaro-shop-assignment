-- USERS
-- 비밀번호는 bcrypt 해시 필요 (아래 3단계에서 생성해서 <HASH> 부분에 붙여넣기)
INSERT INTO users (email, password, name, phone, role, created_at, updated_at)
VALUES ('hanaro', '$2a$10$mg9M8jDSw0RD9AURf71LweJ9DxK658T5AbWnEf2imKcvAliQ4FEii', 'ADMIN',
        '010-9999-0000', 'ADMIN',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (email, password, name, phone, role, created_at, updated_at)
VALUES ('user1@aaa.com', '$2a$10$aRIfhIMfuTRlKH./bXdhn.G2tADYe1p7t8vwM1p/oSUtigEk3yRMu',
        'USER1', '010-0000-0000', 'USER',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- PRODUCTS
INSERT INTO products (name, description, price, stock_quantity, image_url, created_at,
                      updated_at)
VALUES ('Product233', 'product desc', 12000, 20, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at,
                      updated_at)
VALUES ('Product22', 'product desc', 12000, 20, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);