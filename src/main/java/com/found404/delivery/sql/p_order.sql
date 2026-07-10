-- p_order: 주문 테이블
CREATE TABLE IF NOT EXISTS p_order (

    order_id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id UUID NOT NULL,
    address_id UUID NOT NULL,

    status VARCHAR(255) NOT NULL DEFAULT 'REQUESTED',
    total_menu_price INTEGER NOT NULL,
    delivery_fee INTEGER NOT NULL DEFAULT 0,
    discount_price INTEGER NOT NULL DEFAULT 0,
    total_price INTEGER NOT NULL,

    delivery_address VARCHAR(255) NOT NULL,
    delivery_detail_address VARCHAR(255),
    delivery_request VARCHAR(255),

    canceled_at TIMESTAMP(6),
    status_reason VARCHAR(255),

    created_at TIMESTAMP(6),
    created_by BIGINT,
    updated_at TIMESTAMP(6),
    updated_by BIGINT,
    deleted_at TIMESTAMP(6),
    deleted_by BIGINT
);

-- 사용자별 주문 조회 최적화
CREATE INDEX IF NOT EXISTS idx_p_order_user_id
    ON p_order (user_id);

-- 가게별 주문 조회 최적화
CREATE INDEX IF NOT EXISTS idx_p_order_store_id
    ON p_order (store_id);

-- 주문 상태별 조회 최적화
CREATE INDEX IF NOT EXISTS idx_p_order_status
    ON p_order (status);