-- p_order_item: 주문 상품 테이블
CREATE TABLE IF NOT EXISTS p_order_item (

    order_item_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    menu_id UUID NOT NULL,

    menu_name VARCHAR(100) NOT NULL,
    menu_price INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    total_price INTEGER NOT NULL,

    created_at TIMESTAMP(6),
    created_by BIGINT,
    updated_at TIMESTAMP(6),
    updated_by BIGINT,
    deleted_at TIMESTAMP(6),
    deleted_by BIGINT
);

-- 주문별 주문 상품 조회 최적화
CREATE INDEX IF NOT EXISTS idx_p_order_item_order_id
    ON p_order_item (order_id);

-- 메뉴별 주문 상품 조회 최적화
CREATE INDEX IF NOT EXISTS idx_p_order_item_menu_id
    ON p_order_item (menu_id);