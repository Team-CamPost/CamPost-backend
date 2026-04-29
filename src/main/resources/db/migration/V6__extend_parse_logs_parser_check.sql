-- parse_logs.parser CHECK constraint에 'pyhwp' 추가
-- pyhwp(hwp5) 라이브러리로 HWP BodyText 전체 파싱 지원 추가에 따른 변경

ALTER TABLE parse_logs DROP CONSTRAINT IF EXISTS parse_logs_parser_check;

ALTER TABLE parse_logs
    ADD CONSTRAINT parse_logs_parser_check
    CHECK (parser IN ('pdfplumber', 'olefile', 'hwpx_xml', 'none', 'pyhwp'));
