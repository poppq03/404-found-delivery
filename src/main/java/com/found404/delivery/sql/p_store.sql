-- 담당: 서인
-- 테이블: p_store
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE p_store (
                       store_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       owner_id BIGINT NOT NULL,
                       category_id UUID NOT NULL,
                       region_id UUID NOT NULL,

                       name VARCHAR(100) NOT NULL,
                       description TEXT,

                       phone_number VARCHAR(20) NOT NULL,
                       address VARCHAR(255) NOT NULL,
                       detail_address VARCHAR(255) NOT NULL,

                       min_order_price INT CHECK (min_order_price >= 0),
                       delivery_fee INT CHECK (delivery_fee >= 0),

                       status VARCHAR(20) NOT NULL
                           CHECK (status IN ('OPEN', 'CLOSED', 'BREAK_TIME', 'PENDING', 'SUSPENDED')),

                       is_active BOOLEAN NOT NULL DEFAULT TRUE,

                       image_url VARCHAR(500) NOT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by BIGINT,

                       updated_at TIMESTAMP,
                       updated_by BIGINT,

                       deleted_at TIMESTAMP,
                       deleted_by BIGINT,

                       CONSTRAINT fk_store_owner
                           FOREIGN KEY (owner_id)
                               REFERENCES p_user(user_id),

                       CONSTRAINT fk_store_category
                           FOREIGN KEY (category_id)
                               REFERENCES p_category(category_id),

                       CONSTRAINT fk_store_region
                           FOREIGN KEY (region_id)
                               REFERENCES p_region(region_id)
);

-- 인덱스
CREATE INDEX idx_store_owner_id
    ON p_store(owner_id);

CREATE INDEX idx_store_category_id
    ON p_store(category_id);

CREATE INDEX idx_store_status
    ON p_store(status);

CREATE INDEX idx_store_is_active
    ON p_store(is_active);

CREATE INDEX idx_store_name
    ON p_store(name);