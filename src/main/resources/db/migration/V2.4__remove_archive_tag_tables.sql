-- Archive 태그 관련 테이블 제거
-- Archive Tag 기능 완전 제거

-- 외래키 제약조건이 있는 테이블부터 삭제
DROP TABLE IF EXISTS archive_item_tag;

-- 메인 태그 테이블 삭제
DROP TABLE IF EXISTS archive_tag;

-- 인덱스가 있다면 함께 삭제됨
-- 관련 시퀀스나 기타 객체들도 CASCADE로 함께 삭제됨
