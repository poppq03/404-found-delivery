-- 담당: 서인
-- 테이블: p_category
CREATE TABLE category (
                          category_id UUID PRIMARY KEY NOT NULL,

                          name VARCHAR(100) NOT NULL,
                          description VARCHAR(255) NOT NULL,

                          is_active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          created_by BIGINT,

                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_by BIGINT,

                          deleted_at TIMESTAMP,
                          deleted_by BIGINT
);
CREATE EXTENSION IF NOT EXISTS "pgcrypto";