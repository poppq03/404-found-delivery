-- 테이블: p_cart
CREATE TABLE IF NOT EXISTS p_cart (
    cart_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    store_id UUID,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    CONSTRAINT pk_p_cart PRIMARY KEY (cart_id),
    CONSTRAINT uk_p_cart_user_id UNIQUE (user_id)
);