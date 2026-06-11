# CamPost Backend

대학교 학과 공지를 수집·가공·제공하는 **CamPost** 서비스의 백엔드(REST API 서버) 저장소입니다.
인증·회원, 공지 조회, 북마크, 크롤링 데이터 적재(Importer) 등 핵심 비즈니스 로직과 데이터 API를 담당합니다.

> **기본 브랜치는 `dev`입니다.** 모든 작업은 `dev`에서 분기하고 `dev`로 병합합니다. `dev` 직접 push는 금지입니다.

> **CamPost는 3개 저장소로 구성됩니다.**
>
> | 저장소               | 역할                          | 스택                  |
> | -------------------- | ----------------------------- | --------------------- |
> | **CamPost-frontend** | 사용자 화면                   | React · Vite          |
> | **CamPost-backend**  | REST API · 인증 · 데이터 적재 (현재 저장소) | Spring Boot · Java |
> | **CamPost-pipeline** | 공지 크롤링 · 가공            | Python · Playwright   |
>
> Pipeline이 크롤링한 데이터를 Backend Importer가 DB에 적재하고, Backend REST API가 Frontend에 제공합니다.

---

## 1. 기술 스택

| 구분         | 사용 기술                                       |
| ------------ | ----------------------------------------------- |
| 언어/프레임워크 | Java 17 · Spring Boot 3.4                        |
| 데이터 접근  | Spring JDBC (`JdbcClient`) — **JPA 미사용**      |
| 데이터베이스 | PostgreSQL (Neon) · Flyway 마이그레이션          |
| 인증         | JWT (jjwt) — Access/Refresh 토큰                 |
| 메일         | Spring Mail (Resend API / SMTP / Logging 모드)   |
| API 문서     | SpringDoc OpenAPI (Swagger UI)                   |
| 빌드/테스트  | Maven (`mvnw`) · JUnit · JaCoCo 커버리지         |
| 배포         | Render (GitHub Actions 자동 배포)                |

---

## 2. 시작하기 (신규 팀원용)

### 2-1. 사전 준비

- JDK 17
- (선택) Docker — 로컬 PostgreSQL/Mailpit 사용 시

### 2-2. 설치 및 실행

```bash
# 1. 저장소 클론 후 dev 브랜치로 이동
git clone <repo-url>
cd CamPost-backend
git switch dev

# 2. 최초 1회: .env 생성 및 초기화
bash scripts/setup-local.sh
#   .env 의 <REQUIRED> 값(DB 접속정보, JWT_SECRET 등)을 채운다

# 3. 로컬 실행 (DB는 Neon 사용 시 Docker 없이 JVM만 기동)
bash scripts/local-dev.sh
```

> **로컬 개발 핵심 규칙**: `docker compose up --build`를 매번 쓰지 않습니다.
> `local-dev.sh`는 `.env`의 `SPRING_DATASOURCE_URL`이 **Neon이면 Docker 없이 Spring Boot만 기동**하고,
> 아니면 로컬 PostgreSQL(`db`)·Mailpit을 Docker로 띄운 뒤 `./mvnw spring-boot:run`을 실행합니다.
> 코드 수정 후엔 `Ctrl+C` → `local-dev.sh` 재실행으로 반영합니다.

### 2-3. 주요 환경 변수 (`.env`)

| 변수                            | 설명                                            |
| ------------------------------- | ----------------------------------------------- |
| `SPRING_DATASOURCE_URL`         | PostgreSQL(Neon) 접속 URL                       |
| `SPRING_DATASOURCE_USERNAME/PASSWORD` | DB 계정                                    |
| `JWT_SECRET` · `JWT_EXPIRY_MS`  | JWT 서명 키 · 만료 시간                         |
| `APP_CORS_ALLOWED_ORIGINS`      | CORS 허용 오리진 (쉼표 구분)                    |
| `APP_MAIL_VERIFICATION_SENDER`  | 메일 발송 모드: `logging`(기본) / `resend` / `smtp` |
| `RESEND_API_KEY` · `APP_MAIL_FROM` | Resend 모드 발송 설정                        |
| `IMPORTER_ENABLED`              | raw JSON → DB 적재 스케줄러 on/off (로컬 적재 시 true) |

### 2-4. 엔드포인트 확인

| 항목         | URL                                            |
| ------------ | ---------------------------------------------- |
| Health       | `http://localhost:8080/api/v1/health`          |
| Swagger UI   | `http://localhost:8080/swagger-ui.html`        |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs`            |

---

## 3. 프로젝트 구조

`global + domain` 패턴. 각 도메인은 `controller → service → repository` 단방향 레이어로 구성됩니다.

```text
CamPost-backend/
├─ .github/
│  ├─ ISSUE_TEMPLATE/feature_request.md
│  ├─ workflows/
│  │  ├─ ci.yml                  # CI: 테스트 + JaCoCo 커버리지 (PR/푸시)
│  │  └─ deploy.yml              # CD: Render 자동 배포 (dev 푸시)
│  └─ pull_request_template.md
├─ scripts/
│  ├─ setup-local.sh             # 최초 1회 .env 초기화
│  ├─ local-dev.sh               # DB 기동 + Spring Boot 실행
│  ├─ check-local.sh             # 커밋 전 검증 (./mvnw test)
│  └─ compose-smoke.sh           # PR 전 통합 스모크 (db+backend+pipeline)
├─ src/main/
│  ├─ java/com/campost/backend/
│  │  ├─ global/                 # 공통 영역
│  │  │  ├─ api/                 #   공통 응답 포맷 (ApiResponse)
│  │  │  ├─ auth/                #   @LoginUser 인자 리졸버 (선택적 인증)
│  │  │  ├─ config/              #   CORS · OpenAPI · WebMvc 설정
│  │  │  ├─ exception/           #   전역 예외 처리
│  │  │  └─ jwt/                 #   JWT 토큰 서비스
│  │  └─ domain/                 # 도메인 영역
│  │     ├─ auth/                #   이메일 인증 · 회원가입 · JWT 로그인
│  │     ├─ user/                #   프로필 · 온보딩 · 비밀번호 · 탈퇴
│  │     ├─ post/                #   공지 조회(notice) · 북마크(bookmark)
│  │     ├─ collect/             #   크롤 현황 조회(query) · raw 적재(importer)
│  │     ├─ hub/                 #   대시보드 · 검색 · 스마트 필터
│  │     ├─ personal/            #   북마크 · 관심 키워드/카테고리
│  │     ├─ notification/        #   키워드 알림 · 마감 임박 알림
│  │     ├─ admin/               #   사용자/역할 관리 · 크롤러 모니터링
│  │     ├─ ugc/  community/     #   제보 · 댓글/좋아요 (후속)
│  │     └─ health/              #   헬스체크
│  └─ resources/
│     ├─ db/migration/           # Flyway 마이그레이션 (V{n}__{설명}.sql)
│     └─ application.yml
├─ docker-compose.yml            # db · mailpit · backend · pipeline · frontend
├─ Dockerfile
├─ pom.xml
└─ README.md
```

### 아키텍처 규칙

- **레이어**: `controller → service → repository` 단방향. 도메인 간 직접 참조는 최소화하고 필요 시 service 인터페이스로 노출
- **네이밍**: `*Controller` · `*Service` · `*Repository` · `*Request`/`*Response`/`*Dto`
- **DB 마이그레이션**: Flyway 사용. `src/main/resources/db/migration/V{number}__{설명}.sql` (예: `V15__add_r2_url_to_attachments.sql`)

---

## 4. 협업 워크플로우

> 모든 변경은 **이슈 → 브랜치 → 커밋 → PR → 리뷰 → `dev` 머지**의 흐름을 따릅니다.

### 4-1. 브랜치 전략

- **`dev` = 기본(default) 브랜치**, 개발 통합용 — **직접 push 금지**
- 기능 브랜치는 `dev`에서 분기 후 PR로 `dev`에 병합
- 머지 방식: **Merge commit** (PR 단위 이력·개별 커밋 보존)

```bash
git switch dev
git pull origin dev
git switch -c feat/12-login-api    # 타입/이슈번호-기능명
```

### 4-2. 작업 순서 (Step by Step)

1. **이슈 발행** — 하나의 이슈 = 하나의 기능. 템플릿(`.github/ISSUE_TEMPLATE`) 사용, Assignee·Label·체크리스트 작성
2. **로컬 최신화** — `git switch dev && git pull origin dev`
3. **브랜치 생성** — `타입/이슈번호-기능명`
4. **개발 & 검증** — 커밋 전 `bash scripts/check-local.sh`(테스트) 실행 권장
5. **푸시 & PR 생성** — PR 템플릿 작성, `Closes #이슈번호` 연결
6. **CI 자동 검증** — 테스트 + 커버리지 통과 확인
7. **코드 리뷰** — **1명 이상 Approve 필수**
8. **`dev` 머지** — 머지 시 Render 자동 배포

### 4-3. 네이밍 컨벤션

| 항목        | 규칙                         | 예시                            |
| ----------- | ---------------------------- | ------------------------------- |
| 브랜치      | `타입/이슈번호-기능명`       | `feat/84-resend-api`            |
| 커밋 메시지 | `타입: 설명 (#이슈번호)`     | `feat: Resend 메일 발송 (#84)`  |
| PR 제목     | `타입(#이슈번호): 핵심 내용` | `Fix(#90): 로그인 응답 보완`    |

**사용 타입**: `feat`(기능) · `fix`(버그) · `refactor`(구조 개선) · `style`(스타일) · `chore`(설정) · `docs`(문서)

---

## 5. CI/CD

### 5-1. CI — `.github/workflows/ci.yml`

PR 생성 및 `dev` 푸시 시 자동 실행됩니다.

| 단계            | 내용                                   |
| --------------- | -------------------------------------- |
| Run tests       | `./mvnw test` (JUnit 단위 테스트)      |
| Report coverage | JaCoCo 라인 커버리지 측정·출력         |

- 테스트는 외부 의존성(DB 등) 없이 도는 **순수 단위 테스트**
- 공급망 보안: 액션을 **커밋 SHA로 고정**, `persist-credentials: false`

### 5-2. CD — `.github/workflows/deploy.yml`

`dev` 브랜치에 머지(푸시)되면 **Render로 자동 배포**됩니다.

- Render Deploy Hook 호출 → **배포 완료(live)까지 폴링**해 결과 확인
- Flyway 마이그레이션은 애플리케이션 기동 시 **자동 적용**
- `RENDER_DEPLOY_HOOK_URL` · `RENDER_API_KEY` · `RENDER_SERVICE_ID`는 GitHub Secret으로 주입

---

## 6. 실행 명령어

```bash
# 로컬 개발
bash scripts/setup-local.sh    # 최초 1회 .env 초기화
bash scripts/local-dev.sh      # DB 기동(Neon이면 생략) + Spring Boot 실행

# 검증
bash scripts/check-local.sh    # 커밋 전 테스트
./mvnw test                    # 전체 테스트
./mvnw test -Dtest=ClassName#methodName   # 단일 테스트

# 통합 스모크 (PR 전, Docker 필요)
bash scripts/compose-smoke.sh  # db + backend + pipeline 통합 실행
```

> **데이터 적재(Importer)**: Pipeline이 생성한 `data/raw/*.json`을 Backend Importer가 읽어 DB에 적재합니다.
> 로컬에서 적재하려면 `.env`의 `IMPORTER_ENABLED=true`로 설정합니다. (Render 배포 환경은 `false` 유지)

---

## 7. 협업 원칙 요약

- 작은 단위의 **이슈 / 브랜치 / PR**로 나눠 작업합니다.
- 규칙 기반 네이밍과 템플릿으로 커뮤니케이션 비용을 줄입니다.
- **`dev` 직접 push 금지** — 모든 변경은 PR + 1명 이상 리뷰를 거칩니다.
- 코드 품질(테스트/커버리지/CI)과 자동 배포(CD)로 일관성과 안정성을 유지합니다.
