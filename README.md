# CamPost Backend

CamPost 백엔드 API 서버입니다.

- 기술 스택: Spring Boot 3, Java 17, PostgreSQL, JDBC
- 역할: 크롤러 로그/공지 조회 API 제공
- 실행 방식: Docker Compose 또는 로컬 Java 실행

## 프로젝트 구조

```
CamPost-backend/
	db/
		README.md
	docs/
		backend-architecture-mvp.md
	src/
		main/
			java/com/campost/backend/
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
					# 공통/운영
					health/
			resources/
				db/migration/
					V1__collect_schema.sql
					V2__post_schema.sql
					V3__user_personal_admin_schema.sql
					V4__seed_initial_data.sql
				application.yml
	Dockerfile
	docker-compose.yml
	pom.xml
```

상세 설계 문서: `docs/backend-architecture-mvp.md`

## API 엔드포인트

- `GET /api/v1/health`
- `GET /api/v1/collect/jobs?limit=20`
- `GET /api/v1/collect/parse-logs?limit=20`
- `GET /api/v1/notices?limit=20`
- `POST /api/v1/collect/importer/run`

하위 호환(legacy) 경로도 당분간 유지합니다.
- `GET /api/v1/crawl/*`
- `POST /api/v1/importer/*`

요청 헤더 표준:

```json
{
	"Authorization": "Bearer <JWT_ACCESS_TOKEN>",
	"Content-Type": "application/json"
}
```

응답은 공통 포맷을 사용합니다.

```json
{
	"isSuccess": true,
	"code": "COMMON200",
	"message": "요청이 성공했습니다.",
	"result": []
}
```

에러 응답 포맷:

```json
{
	"isSuccess": false,
	"code": "ERROR_CODE",
	"message": "에러 메시지"
}
```

공통 에러 코드:

| HTTP Status | Code | Message |
| --- | --- | --- |
| 400 | COMMON400 | 잘못된 요청입니다. |
| 401 | TOKEN401 | 액세스 토큰이 만료되었습니다. |
| 401 | TOKEN402 | 유효하지 않은 토큰입니다. |
| 403 | AUTH403 | 접근 권한이 없습니다. |
| 404 | COMMON404 | 리소스를 찾을 수 없습니다. |
| 409 | COMMON409 | 리소스 충돌이 발생했습니다. |
| 500 | SERVER500 | 서버 내부 오류가 발생했습니다. |

## Swagger(OpenAPI) 문서

백엔드 실행 후 아래 URL에서 API 문서를 확인할 수 있습니다.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### Swagger 문서 작성 방법

1. 컨트롤러 클래스에 `@Tag` 추가
2. 각 API 메서드에 `@Operation` 추가
3. 쿼리 파라미터에는 `@Parameter`로 설명 추가

예시:

```java
@Tag(name = "Post", description = "공지 컨텐츠 조회 API")
@RestController
@RequestMapping("/api/v1/notices")
public class NoticeQueryController {

	@Operation(summary = "공지 목록 조회", description = "최신 공지 목록을 조회합니다.")
	@GetMapping
	public ApiResponse<List<NoticeDto>> getNotices(
			@Parameter(description = "조회 개수 (1~100)")
			@RequestParam(defaultValue = "20") int limit
	) {
		return ApiResponse.ok(...);
	}
}
```

JWT를 적용할 때는 OpenAPI 보안 스키마 이름(`bearerAuth`)을 기준으로
`@SecurityRequirement(name = "bearerAuth")`를 엔드포인트에 추가하면 됩니다.

## 실행 방법

### 1) 환경변수 준비

```bash
cp .env.example .env
```

Windows PowerShell/CMD:

```bash
copy .env.example .env
```

필수 환경변수(`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`, `APP_CORS_ALLOWED_ORIGINS`,
`JWT_SECRET`, `JWT_EXPIRY_MS`)는 반드시 실제 값으로 채워야 합니다.

값이 비어 있거나 `<REQUIRED>` 같은 placeholder 상태면
백엔드는 시작 단계에서 즉시 실패하도록 구성되어 있습니다.

### 2) Docker Compose 실행

기본 실행 (DB + Backend + Pipeline):

```bash
docker compose up --build -d db backend pipeline
```

이 구성에서는 backend가 `/data/raw`를 주기적으로 스캔하여
`raw_notices`, `notices` 테이블로 자동 적재합니다.

DB 스키마는 Postgres init.sql 마운트가 아니라 Flyway로 관리합니다.
기본 마이그레이션 위치는 `src/main/resources/db/migration` 입니다.

현재 버전 분리 기준:
- `V1`: Collect 도메인 스키마 (crawl_sources/crawl_jobs/parse_logs)
- `V2`: Post 도메인 스키마 (raw_notices/notices/attachments/import log)
- `V3`: User/Personal/Admin 스키마 (users/bookmarks/keywords/notifications)
- `V4`: 초기 시드 데이터 (crawl_sources, admin 사용자)

상태 확인:

```bash
docker compose ps
docker compose logs -f backend
```

헬스체크:

```bash
curl http://localhost:8080/api/v1/health
```

Importer 수동 실행:

```bash
curl -X POST http://localhost:8080/api/v1/collect/importer/run
```

DB 마이그레이션 이력 확인:

```bash
docker compose exec db psql -U campost -d campost -c "select * from flyway_schema_history order by installed_rank;"
```

### 3) Frontend를 컨테이너로 띄우고 싶을 때만

`frontend` 서비스는 profile로 분리되어 있습니다.

```bash
docker compose --profile frontend up --build -d
```

## 로컬(Java) 실행

Docker 없이 백엔드만 로컬에서 실행할 수도 있습니다.

```bash
mvn spring-boot:run
```

이 경우 PostgreSQL이 필요하며, `.env` 대신 시스템 환경변수 또는 `application.yml` 기본값을 사용합니다.

## 팀 개발 규칙

- 기능별 패키지(`domain/*`) 안에서 `controller -> service -> repository` 흐름 유지
- 공통 설정/응답/예외는 `global/*`에만 위치
- DB 접근 SQL은 repository 계층에 집중
- 신규 MVP 기능은 `auth/collect/post/hub/personal/notification/admin` 도메인 기준으로 추가
- 후순위 기능은 `ugc/community` 도메인에 스캐폴딩 후 점진 구현
- 스키마 변경은 `src/main/resources/db/migration` 에 버전 파일로 추가 (`V{N}__*.sql`)