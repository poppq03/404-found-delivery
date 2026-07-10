-- 테이블: p_cart_item
CREATE TABLE IF NOT EXISTS p_cart_item (
    cart_item_id UUID NOT NULL,
    cart_id UUID NOT NULL,
    menu_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    CONSTRAINT pk_p_cart_item PRIMARY KEY (cart_item_id),
    CONSTRAINT uk_p_cart_item_cart_menu UNIQUE (cart_id, menu_id)
);

CREATE INDEX IF NOT EXISTS idx_p_cart_item_cart_id
    ON p_cart_item (cart_id);