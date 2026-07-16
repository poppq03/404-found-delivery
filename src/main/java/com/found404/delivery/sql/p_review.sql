-- 테이블: p_review
CREATE TABLE IF NOT EXISTS p_review (
                                        review_id UUID NOT NULL,
                                        order_id UUID NOT NULL,
                                        user_id BIGINT NOT NULL,
                                        store_id UUID NOT NULL,
                                        rating INTEGER NOT NULL,
                                        content TEXT NOT NULL,
                                        is_hidden BOOLEAN NOT NULL DEFAULT FALSE,

                                        created_at TIMESTAMP,
                                        created_by BIGINT,
                                        updated_at TIMESTAMP,
                                        updated_by BIGINT,
                                        deleted_at TIMESTAMP,
                                        deleted_by BIGINT,

                                        CONSTRAINT pk_p_review PRIMARY KEY (review_id),
    CONSTRAINT uk_p_review_order_id UNIQUE (order_id),
    CONSTRAINT chk_p_review_rating
    CHECK (rating BETWEEN 1 AND 5)
    );

CREATE INDEX IF NOT EXISTS idx_p_review_user_id
    ON p_review (user_id);

CREATE INDEX IF NOT EXISTS idx_p_review_store_id
    ON p_review (store_id);