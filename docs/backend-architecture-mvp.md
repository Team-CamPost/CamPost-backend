# CamPost Backend Architecture (MVP)

## 배경

CamPost 백엔드는 Proovy 서버 구조의 장점인 `global + domain` 패턴을 참고하되,
현재 MVP 범위를 벗어나는 복잡한 레이어(예: OCR, 크레딧, 임베딩)는 제외한다.

핵심 목표:
- 도메인 단위로 책임 분리
- 기능 확장 시 파일 탐색 비용 최소화
- 팀원이 처음 봐도 어디를 수정해야 할지 즉시 파악 가능

## 기준 구조

```text
src/main/java/com/campost/backend/
  CamPostBackendApplication.java
  global/
    api/
    config/
    exception/
  domain/
    auth/
    collect/
      query/
      importer/
    post/
      notice/
    hub/
    personal/
    notification/
    admin/
    ugc/
    community/
    health/
```

## Proovy 참고 포인트

적용한 것:
- global(전역 설정/예외/응답) 분리
- domain(기능군) 중심 패키징
- 기능별 controller/service/repository/dto 분할

의도적으로 제외한 것:
- 과도한 인프라 세분화(초기 MVP 과설계 방지)
- 미사용 도메인(ocr, credit, embedding 등)

## MVP 기능 매핑

- Auth
  - 이메일 인증, 회원가입, JWT 로그인
  - 패키지: `domain/auth`

- Collect
  - 크롤링, 중복 제거, 첨부 파싱, 데이터 적재
  - 패키지: `domain/collect/query`, `domain/collect/importer`

- Post
  - 공지 상세, 원문 링크, 핵심 정보 카드
  - 패키지: `domain/post/notice`

- Hub
  - 대시보드, 검색, 스마트 필터
  - 패키지: `domain/hub`

- Personal
  - 북마크/스크랩, 관심 키워드/카테고리, 보관함
  - 패키지: `domain/personal`

- Notification
  - 키워드 알림, 마감 임박 알림
  - 패키지: `domain/notification`

- Admin
  - 사용자/역할 관리, 크롤러 상태 모니터링
  - 패키지: `domain/admin`

- UGC (후속)
  - 제보, 승인 대기, 수정/삭제 요청
  - 패키지: `domain/ugc`

- Community (후속)
  - 댓글, 대댓글, 좋아요
  - 패키지: `domain/community`

## 레이어 규칙

각 도메인 내부 권장 기본 구조:

```text
domain/{module}/
  controller/
  service/
  repository/
  dto/
  entity/ (JPA 도입 시)
```

의존성 방향:
- controller -> service -> repository
- repository는 DB/외부 연동 세부 구현
- global은 모든 도메인이 참조 가능
- 도메인 간 직접 참조는 최소화하고 필요한 경우 service 인터페이스로 노출

## 이행 계획

1. 신규 기능은 반드시 `domain/{mvp-module}` 아래에 추가
2. collect/post는 하위 모듈(`query`, `importer`, `notice`) 단위로 세분화
3. 레거시 API 경로(`/api/v1/crawl`, `/api/v1/importer`)는 단계적으로 제거

## 네이밍 규칙

- Controller: `*Controller`
- Service: `*Service`
- Repository: `*Repository`
- DTO: `*Request`, `*Response`, `*Dto`

## DB 마이그레이션 규칙

- 스키마 변경은 `src/main/resources/db/migration` 에서 Flyway로 관리
- 파일명 규칙: `V{number}__{description}.sql`
  - 예: `V2__add_notice_search_index.sql`

현재 분리:
- `V1__collect_schema.sql`
- `V2__post_schema.sql`
- `V3__user_personal_admin_schema.sql`
- `V4__seed_initial_data.sql`

## 검증 체크리스트

- 도메인 이름만 보고 기능 위치가 유추되는가
- 한 기능 수정 시 수정 파일이 한 도메인 안에서 끝나는가
- 글로벌 설정/예외가 중복 없이 공유되는가
