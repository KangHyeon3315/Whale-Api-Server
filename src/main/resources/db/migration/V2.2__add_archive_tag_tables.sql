-- Archive 태그 관련 테이블 생성
-- Archive 도메인에 태그 기능 추가

-- 아카이브 태그를 관리하는 테이블
CREATE TABLE IF NOT EXISTS archive_tag
(
    identifier   UUID                     NOT NULL,
    name         VARCHAR(255)             NOT NULL,
    type         VARCHAR(50)              NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (identifier)
);

-- 아카이브 아이템과 태그의 연결을 관리하는 테이블
CREATE TABLE IF NOT EXISTS archive_item_tag
(
    identifier               UUID                     NOT NULL,
    archive_item_identifier  UUID                     NOT NULL,
    tag_identifier           UUID                     NOT NULL,
    created_date             TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (identifier),
    FOREIGN KEY (archive_item_identifier) REFERENCES archive_item (identifier) ON DELETE CASCADE,
    FOREIGN KEY (tag_identifier) REFERENCES archive_tag (identifier) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_archive_tag_name ON archive_tag (name);
CREATE INDEX IF NOT EXISTS idx_archive_tag_type ON archive_tag (type);
CREATE INDEX IF NOT EXISTS idx_archive_tag_name_type ON archive_tag (name, type);
CREATE INDEX IF NOT EXISTS idx_archive_item_tag_item_id ON archive_item_tag (archive_item_identifier);
CREATE INDEX IF NOT EXISTS idx_archive_item_tag_tag_id ON archive_item_tag (tag_identifier);

-- 유니크 제약조건 추가
CREATE UNIQUE INDEX IF NOT EXISTS uk_archive_tag_name_type ON archive_tag (name, type);
CREATE UNIQUE INDEX IF NOT EXISTS uk_archive_item_tag ON archive_item_tag (archive_item_identifier, tag_identifier);

-- 코멘트 추가
COMMENT ON TABLE archive_tag IS '아카이브 태그를 관리하는 테이블';
COMMENT ON TABLE archive_item_tag IS '아카이브 아이템과 태그의 연결을 관리하는 테이블';

COMMENT ON COLUMN archive_tag.name IS '태그 이름';
COMMENT ON COLUMN archive_tag.type IS '태그 타입 (user, system, auto 등)';
COMMENT ON COLUMN archive_item_tag.archive_item_identifier IS '아카이브 아이템 식별자';
COMMENT ON COLUMN archive_item_tag.tag_identifier IS '태그 식별자';
