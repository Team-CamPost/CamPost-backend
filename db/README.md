# Database Schema Management

The database schema is now managed by Flyway migrations.

Migration files are located at:
- `src/main/resources/db/migration`

Use Flyway migration scripts in that directory for all schema changes.

Current migrations:
- `V1__collect_schema.sql`
- `V2__post_schema.sql`
- `V3__user_personal_admin_schema.sql`
- `V4__seed_initial_data.sql`
