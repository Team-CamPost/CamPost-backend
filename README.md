# CamPost Backend

CamPost Backend 저장소의 개발 워크플로우와 백엔드 실행 규칙을 정리한 문서입니다.
팀원 누구나 동일한 기준으로 이슈 발행, 브랜치 작업, PR 리뷰, 로컬 실행, 통합 스모크 테스트를 진행할 수 있도록 작성했습니다.

## 1. 개발 전 작업

### 1-1. Issue 발행

- 이슈 하나(브랜치 하나)에서는 하나의 기능만 개발합니다.
- 이슈 제목 규칙:
  - [이슈종류] 이슈 제목
  - 예시: [Feat] example API 구현, [Fix] dev 브랜치 충돌 해결
- 이슈 템플릿:
  - .github/ISSUE_TEMPLATE/feature_request.md
- 이슈 작성 시 필수:
  - Assignees 지정
  - Labels 지정
  - 작업 체크리스트 작성

### 1-2. 로컬 최신화

개발 시작 전 반드시 최신 변경 사항을 반영합니다.

```bash
git fetch
git pull
```

## 2. 브랜치 전략

### 2-1. Git Flow 기반 운영

- dev 브랜치: default 브랜치, 개발 통합용
- 기능 브랜치: dev에서 분기 후 dev로 병합

### 2-2. 브랜치 네이밍 규칙

- 규칙: 타입/이슈번호-기능명
- 예시:
  - feat/12-init-project
  - fix/3-add-login
  - refactor/22-cart-page
  - docs/15-readme

사용 타입:

| 타입 | 설명 |
| --- | --- |
| chore | 프로젝트 설정 |
| docs | 문서 수정 |
| feat | 기능 개발 |
| fix | 버그 수정 |
| refactor | 구조 개선 |
| style | 스타일 수정 |

## 3. 개발 후 Commit & Push

### 3-1. 코드 정리 규칙

- 커밋 전 로컬 검증 권장:

```bash
bash scripts/check-local.sh
```

- Java 코드는 IDE 포맷터 + 정적 분석 규칙을 준수합니다.
- dev 브랜치 직접 push는 금지합니다.

### 3-2. 커밋 메시지 규칙

- 규칙: 타입: 커밋 설명 (#이슈번호)
- 예시:

```bash
git commit -m "feat: 로그인 구현 (#9)"
git commit -m "fix: 카드 페이지 수정 (#10)"
git commit -m "refactor: 아이콘 리팩토링 (#13)"
```

- 커밋 Body에는 변경 이유와 테스트 내용을 상세히 작성합니다.

## 4. PR 생성 및 Merge

### 4-1. PR 제목/본문 규칙

- PR 제목 규칙: 타입(#이슈번호): 핵심 PR 내용
- 예시:
  - Feat(#9): 로그인 구현
  - Fix(#10): 카드 페이지 수정
  - Refactor(#13): 아이콘 리팩토링
- PR 템플릿:
  - .github/pull_request_template.md

### 4-2. 리뷰 및 머지 규칙

- PR 작성 후 Reviewer, Assignee, Label을 지정합니다.
- 테스트 결과(스크린샷 또는 로그)를 PR에 첨부합니다.
- dev 브랜치 머지는 1명 이상의 Approve 이후 진행합니다.

## 5. 표준 개발 워크플로우

아래 순서로 팀 협업을 진행합니다.

1. Issue 발행
2. 브랜치 생성 (타입/이슈번호-기능명)
3. 기능 개발 및 테스트
4. Commit & Push
5. PR 생성 (템플릿 작성 + 테스트 결과 첨부)
6. 코드 리뷰 반영
7. Approve 후 dev 머지

## 6. 백엔드 개발 실행 가이드 (스크립트 기준)

### 6-0. 로컬 개발 DB 설정 (최초 1회)

기본 개발 DB는 **Neon PostgreSQL**을 사용합니다. Docker PostgreSQL은 마이그레이션/초기화 테스트 등 예외적인 경우에만 사용합니다.

1. `.env.example`을 복사해 `.env` 생성: `cp .env.example .env`
2. `.env`의 `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`를 Neon 연결 정보로 채웁니다.
   - Neon Dashboard > Connection Details > **Pooled connection string** 사용
   - Direct connection string은 사용하지 않습니다.
3. `JWT_SECRET`을 32자 이상의 랜덤 문자열로 채웁니다.
4. `.env`는 절대 Git에 커밋하지 않습니다.

> Neon 접속 정보는 팀 내부 채널로 공유됩니다.

### 6-1. 코드를 수정할 때마다 backend 컨테이너를 중지해야 하나요?

아닙니다. 평소 개발에서는 backend를 컨테이너로 띄우지 않고 로컬에서 실행하는 방식이 가장 효율적입니다.

- 평소 개발 권장 방식:
  - docker compose up -d --build 를 매번 사용하지 않음
  - scripts/local-dev.sh 실행
  - Neon DB 사용 시 Docker DB 기동을 자동으로 스킵하고 바로 Spring Boot 실행
- 코드 수정 후 반영 방법:
  - local-dev.sh 실행 터미널에서 Ctrl + C
  - 다시 scripts/local-dev.sh 실행
  - 도커 재빌드 없이 수정 사항 즉시 반영

### 6-2. PR(Merge) 전 최종 테스트는 compose-smoke.sh로 하나요?

네. 정확합니다.

- 목적:
  - 로컬에서만 동작하는 코드가 아니라, 도커 통합 환경에서도 정상 동작하는지 검증
- scripts/compose-smoke.sh는 다음 통합 구성을 대상으로 스모크 테스트를 수행:
  - db + backend + pipeline
- 즉, 프론트엔드를 제외한 핵심 백엔드 시스템이 실제 배포 유사 환경에서 정상 기동되는지 확인하는 단계입니다.

### 6-3. compose-smoke.sh 실행 전에 무엇을 꺼야 하나요?

- 도커 컨테이너는 수동으로 미리 내릴 필요가 없습니다.
- compose-smoke.sh가 --build 옵션으로 최신 코드 기준 재조립/재기동을 수행합니다.
- 주의할 점:
  - local-dev.sh로 실행 중인 로컬 backend 서버는 반드시 Ctrl + C로 종료 후 실행
  - 종료하지 않으면 8080 포트 충돌이 발생할 수 있습니다.

## 7. 프로젝트 폴더 구조

아래는 backend 저장소의 핵심 구조입니다.

```text
CamPost-backend/
├─ .github/
│  ├─ ISSUE_TEMPLATE/
│  │  └─ feature_request.md
│  └─ pull_request_template.md
├─ db/
│  └─ README.md
├─ docs/
│  └─ backend-architecture-mvp.md
├─ scripts/
│  ├─ setup-local.sh
│  ├─ local-dev.sh
│  ├─ check-local.sh
│  └─ compose-smoke.sh
├─ src/
│  └─ main/
│     ├─ java/com/campost/backend/
│     │  ├─ global/            # 공통 설정/예외/응답
│     │  └─ domain/            # 도메인별 비즈니스 로직
│     │     ├─ auth/
│     │     ├─ collect/
│     │     │  ├─ query/
│     │     │  └─ importer/
│     │     ├─ post/
│     │     │  └─ notice/
│     │     ├─ hub/
│     │     ├─ personal/
│     │     ├─ notification/
│     │     ├─ admin/
│     │     ├─ ugc/
│     │     ├─ community/
│     │     └─ health/
│     └─ resources/
│        ├─ db/migration/
│        │  ├─ V1__collect_schema.sql
│        │  ├─ V2__post_schema.sql
│        │  ├─ V3__user_personal_admin_schema.sql
│        │  └─ V4__seed_initial_data.sql
│        └─ application.yml
├─ .env.example
├─ .env.local.example       # deprecated
├─ docker-compose.yml
├─ Dockerfile
├─ pom.xml
├─ mvnw
└─ README.md
```

## 8. 빠른 실행 명령어

```bash
# 최초 1회: .env 생성 및 설정 안내
bash scripts/setup-local.sh

# 로컬 개발 (Neon DB 사용 시 Docker 없이 바로 실행)
bash scripts/local-dev.sh

# 로컬 검증
bash scripts/check-local.sh

# PR 전 통합 스모크 테스트
bash scripts/compose-smoke.sh
```

## 9. 백엔드 API/문서 확인

- Health: http://localhost:8080/api/v1/health
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 10. 협업 원칙 요약

- 작은 단위 이슈/브랜치/PR로 나눠 작업합니다.
- 규칙 기반 네이밍과 템플릿으로 커뮤니케이션 비용을 줄입니다.
- 로컬 개발 효율(local-dev)과 배포 유사 검증(compose-smoke)을 분리해 품질을 관리합니다.
