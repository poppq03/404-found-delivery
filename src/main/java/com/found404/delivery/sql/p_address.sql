-- p_address: 고객 배송지 테이블
CREATE TABLE IF NOT EXISTS p_address (

    address_id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,

    address_name VARCHAR(50),
    address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255),
    receiver_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP(6),
    created_by BIGINT,
    updated_at TIMESTAMP(6),
    updated_by BIGINT,
    deleted_at TIMESTAMP(6),
    deleted_by BIGINT
);

-- 사용자별 배송지 조회 최적화
CREATE INDEX IF NOT EXISTS idx_p_address_user_id
    ON p_address (user_id);