# CamPost Backend

CamPost 백엔드 API 서버입니다.

- 기술 스택: Spring Boot 3, Java 17, PostgreSQL, JDBC
- 역할: 크롤러 로그/공지 조회 API 제공
- 실행 방식: Docker Compose 또는 로컬 Java 실행

## 프로젝트 구조

```
CamPost-backend/
	db/
		init.sql
	src/
		main/
			java/com/campost/backend/
				common/
					api/
					config/
					exception/
				features/
					health/
					crawl/
						controller/
						service/
						repository/
						dto/
					notices/
						controller/
						service/
						repository/
						dto/
			resources/
				application.yml
	Dockerfile
	docker-compose.yml
	pom.xml
```

## API 엔드포인트

- `GET /api/v1/health`
- `GET /api/v1/crawl/jobs?limit=20`
- `GET /api/v1/crawl/parse-logs?limit=20`
- `GET /api/v1/notices?limit=20`
- `POST /api/v1/importer/run`

응답은 공통 포맷을 사용합니다.

```json
{
	"success": true,
	"data": [],
	"timestamp": "2026-04-05T16:30:00+09:00"
}
```

## 실행 방법

### 1) 환경변수 준비

```bash
cp .env.example .env
```

Windows PowerShell/CMD:

```bash
copy .env.example .env
```

### 2) Docker Compose 실행

기본 실행 (DB + Backend + Pipeline):

```bash
docker compose up --build -d db backend pipeline
```

이 구성에서는 backend가 `/data/raw`를 주기적으로 스캔하여
`raw_notices`, `notices` 테이블로 자동 적재합니다.

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
curl -X POST http://localhost:8080/api/v1/importer/run
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

- 기능별 패키지(`features/*`) 안에서 `controller -> service -> repository` 흐름 유지
- 공통 설정/응답/예외는 `common/*`에만 위치
- DB 접근 SQL은 repository 계층에 집중