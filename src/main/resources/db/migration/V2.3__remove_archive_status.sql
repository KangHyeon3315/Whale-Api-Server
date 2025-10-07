-- Remove status column from archive table
-- Archive 상태 개념 제거

ALTER TABLE archive DROP COLUMN IF EXISTS status;

-- Update comments
COMMENT ON TABLE archive IS 'iOS 갤러리 백업 작업을 관리하는 테이블 (상태 개념 제거)';
COMMENT ON COLUMN archive.total_items IS '백업 대상 총 파일 수 (0이면 자동 완료 비활성화)';
COMMENT ON COLUMN archive.processed_items IS '처리 완료된 파일 수';
COMMENT ON COLUMN archive.failed_items IS '처리 실패한 파일 수';
COMMENT ON COLUMN archive.completed_date IS '백업 완료 날짜 (null이면 미완료, 값이 있으면 완료)';
