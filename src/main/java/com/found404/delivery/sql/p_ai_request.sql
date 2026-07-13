-- 테이블: p_ai_request
CREATE TABLE IF NOT EXISTS p_ai_request (
    ai_request_id UUID NOT NULL,
    menu_id UUID,
    store_id UUID,
    user_id BIGINT NOT NULL,
    prompt TEXT NOT NULL,
    final_prompt TEXT,
    response_content TEXT,
    ai_model VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    CONSTRAINT pk_p_ai_request PRIMARY KEY (ai_request_id)
);

CREATE INDEX IF NOT EXISTS idx_p_ai_request_menu_id
    ON p_ai_request (menu_id);