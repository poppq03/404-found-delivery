-- 테이블: p_menu
CREATE TABLE IF NOT EXISTS p_menu (
    menu_id UUID NOT NULL,
    store_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    price INTEGER NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    is_sold_out BOOLEAN NOT NULL DEFAULT FALSE,
    is_ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT pk_p_menu PRIMARY KEY (menu_id)
);

CREATE INDEX IF NOT EXISTS idx_p_menu_store_id
    ON p_menu (store_id);