-- Archive 도메인 테이블 생성
-- iOS 갤러리 백업 기능을 위한 테이블들

-- 백업 작업을 관리하는 테이블
CREATE TABLE IF NOT EXISTS archive
(
    identifier      UUID                     NOT NULL,
    name            VARCHAR(255)             NOT NULL,
    description     TEXT                     NULL,
    status          VARCHAR(50)              NOT NULL,
    total_items     INTEGER                  NOT NULL,
    processed_items INTEGER                  NOT NULL,
    failed_items    INTEGER                  NOT NULL,
    created_date    TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_date   TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_date  TIMESTAMP WITH TIME ZONE NULL,
    PRIMARY KEY (identifier)
);

-- 백업된 개별 파일을 관리하는 테이블
CREATE TABLE IF NOT EXISTS archive_item
(
    identifier              UUID                     NOT NULL,
    archive_identifier      UUID                     NOT NULL,
    original_path           VARCHAR(1000)            NOT NULL,
    stored_path             VARCHAR(1000)            NOT NULL,
    file_name               VARCHAR(255)             NOT NULL,
    file_size               BIGINT                   NOT NULL,
    mime_type               VARCHAR(100)             NOT NULL,
    is_live_photo           BOOLEAN                  NOT NULL,
    live_photo_video_path   VARCHAR(1000)            NULL,
    checksum                VARCHAR(64)              NULL,
    original_created_date   TIMESTAMP WITH TIME ZONE NULL,
    original_modified_date  TIMESTAMP WITH TIME ZONE NULL,
    created_date            TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_date           TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (identifier),
    FOREIGN KEY (archive_identifier) REFERENCES archive (identifier) ON DELETE CASCADE
);

-- 파일의 메타데이터를 저장하는 테이블
CREATE TABLE IF NOT EXISTS archive_metadata
(
    identifier              UUID                     NOT NULL,
    archive_item_identifier UUID                     NOT NULL,
    metadata_type           VARCHAR(50)              NOT NULL,
    key                     VARCHAR(255)             NOT NULL,
    value                   TEXT                     NOT NULL,
    created_date            TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (identifier),
    FOREIGN KEY (archive_item_identifier) REFERENCES archive_item (identifier) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_archive_status ON archive (status);
CREATE INDEX IF NOT EXISTS idx_archive_created_date ON archive (created_date);
CREATE INDEX IF NOT EXISTS idx_archive_item_archive_id ON archive_item (archive_identifier);
CREATE INDEX IF NOT EXISTS idx_archive_item_mime_type ON archive_item (mime_type);
CREATE INDEX IF NOT EXISTS idx_archive_item_is_live_photo ON archive_item (is_live_photo);
CREATE INDEX IF NOT EXISTS idx_archive_item_checksum ON archive_item (checksum);
CREATE INDEX IF NOT EXISTS idx_archive_metadata_item_id ON archive_metadata (archive_item_identifier);
CREATE INDEX IF NOT EXISTS idx_archive_metadata_type ON archive_metadata (metadata_type);
CREATE INDEX IF NOT EXISTS idx_archive_metadata_key ON archive_metadata (key);

-- 코멘트 추가
COMMENT ON TABLE archive IS 'iOS 갤러리 백업 작업을 관리하는 테이블';
COMMENT ON TABLE archive_item IS '백업된 개별 파일(이미지, 비디오, 텍스트, 문서)을 관리하는 테이블';
COMMENT ON TABLE archive_metadata IS '백업된 파일의 메타데이터(EXIF, GPS, 텍스트 내용 등)를 저장하는 테이블';

COMMENT ON COLUMN archive.status IS '백업 상태: PENDING(대기), IN_PROGRESS(진행중), COMPLETED(완료), FAILED(실패), CANCELLED(취소)';
COMMENT ON COLUMN archive.total_items IS '백업 대상 총 파일 수';
COMMENT ON COLUMN archive.processed_items IS '처리 완료된 파일 수';
COMMENT ON COLUMN archive.failed_items IS '처리 실패한 파일 수';

COMMENT ON COLUMN archive_item.original_path IS 'iOS 기기에서의 원본 파일 경로';
COMMENT ON COLUMN archive_item.stored_path IS '서버에 저장된 파일 경로';
COMMENT ON COLUMN archive_item.is_live_photo IS '라이브 포토 여부';
COMMENT ON COLUMN archive_item.live_photo_video_path IS '라이브 포토의 비디오 파일 경로';
COMMENT ON COLUMN archive_item.checksum IS '파일 무결성 검증을 위한 체크섬(SHA-256)';

COMMENT ON COLUMN archive_metadata.metadata_type IS '메타데이터 타입: EXIF(이미지 메타데이터), GPS(위치 정보), CAMERA(카메라 정보), DEVICE(기기 정보), LIVE_PHOTO(라이브 포토), TEXT_CONTENT(텍스트 내용), DOCUMENT_PROPERTIES(문서 속성), FILE_ENCODING(파일 인코딩), CUSTOM(사용자 정의)';
COMMENT ON COLUMN archive_metadata.key IS '메타데이터 키';
COMMENT ON COLUMN archive_metadata.value IS '메타데이터 값';
