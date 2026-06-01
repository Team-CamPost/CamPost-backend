# Phase 1 Backend Deploy

## Target

- Runtime: Render Docker web service
- Database: Neon PostgreSQL
- Branch: dev
- Issue: #71

## Render

- Blueprint: `render.yaml`
- Runtime: Docker
- Auto deploy: `dev` branch
- Health check: `/api/v1/health`

## Environment

Set `sync: false` values in the Render dashboard after creating the Blueprint service.


```text
SPRING_DATASOURCE_URL=jdbc:postgresql://<pooled-host>/<db>?sslmode=require
SPRING_DATASOURCE_USERNAME=<neon-user>
SPRING_DATASOURCE_PASSWORD=<neon-password>
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=3
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=0
APP_CORS_ALLOWED_ORIGINS=https://<vercel-domain>
JWT_SECRET=<secret>
JWT_EXPIRY_MS=3600000
JWT_REFRESH_EXPIRY_MS=1209600000
APP_MAIL_VERIFICATION_SENDER=logging
IMPORTER_ENABLED=false
AI_MOCK=true
```

## Seed

Phase 1 seeds data from local tooling.

1. Point local backend env to Neon.
2. Run local backend once so Flyway applies migrations.
3. Run local pipeline to create raw JSON files.
4. Run local backend importer against Neon.
5. Deploy Render backend with `IMPORTER_ENABLED=false`.

## Checks

- `GET /api/v1/health`
- Swagger UI: `/swagger-ui.html`
- Frontend API calls from Vercel domain
