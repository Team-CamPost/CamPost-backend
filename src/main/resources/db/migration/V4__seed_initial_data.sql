-- ============================================================
-- CamPost Seed Data
-- ============================================================

INSERT INTO crawl_sources (name, base_url, department, crawler_type, dept_code) VALUES
  ('소프트웨어학과',     'https://cms.dankook.ac.kr/web/sw/-1',            '소프트웨어학과',     'card',  'SW'),
  ('컴퓨터공학과',       'https://cms.dankook.ac.kr/web/ace/notice',       '컴퓨터공학과',       'card',  'ACE'),
  ('모바일시스템공학과', 'https://cms.dankook.ac.kr/web/mobilesystems/-8', '모바일시스템공학과', 'card',  'MOBILE'),
  ('통계사이언스학과',   'https://cms.dankook.ac.kr/web/dkustat/-6',       '통계사이언스학과',   'card',  'STAT'),
  ('사이버보안학과',     'https://cms.dankook.ac.kr/web/indsec/-4',        '사이버보안학과',     'card',  'INDSEC'),
  ('SW중심대학사업단',   'https://swcu.dankook.ac.kr/en/-5',               'SW중심대학사업단',   'card',  'SWCU')
ON CONFLICT DO NOTHING;

INSERT INTO users (email, password_hash, name, role)
VALUES (
  'admin@dankook.ac.kr',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'CamPost Admin',
  'ADMIN'
)
ON CONFLICT DO NOTHING;
