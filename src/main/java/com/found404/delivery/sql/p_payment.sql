-- 테이블: p_payment
CREATE TABLE IF NOT EXISTS p_payment (
                                         payment_id UUID NOT NULL,
                                         order_id UUID NOT NULL,
                                         user_id BIGINT NOT NULL,
                                         payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    amount INTEGER NOT NULL,
    paid_at TIMESTAMP,

    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,

    CONSTRAINT pk_p_payment PRIMARY KEY (payment_id),
    CONSTRAINT uk_p_payment_order_id UNIQUE (order_id),
    CONSTRAINT chk_p_payment_amount
    CHECK (amount >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_p_payment_user_id
    ON p_payment (user_id);