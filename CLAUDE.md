# projectV — Claude Notes

## Liquibase

Liquibase is the schema source of truth in both dev and prod. `spring.jpa.hibernate.ddl-auto=validate` in both profiles. Changesets live under `src/main/resources/db/changelog/` and are wired into `db.changelog-master.yaml` (numerical order).

### Workflow for entity changes

1. Update the `@Entity` field in Java.
2. Add `00N-description.yaml` in `db/changelog/` with the corresponding `addColumn` / `modifyDataType` / etc.
3. Add an `include` line to `db.changelog-master.yaml`.
4. Restart app — Liquibase applies, Hibernate validates.
5. Commit both files together.

**Never edit an applied changeset.** Liquibase fails fast on checksum mismatch. Add a new one instead.

### Things worth knowing

- `events.location` and `users.location` are `POINT SRID 4326`. `generateChangeLog` emits them as `GEOMETRY(65535)` — hand-fix in any regenerated baseline.
- `role` is a native MySQL `ENUM`. Adding a `Role` enum value needs an explicit `ALTER TABLE users MODIFY COLUMN role ENUM(...)` changeset. To switch to VARCHAR, add `columnDefinition = "VARCHAR(32)"` on `User.role`.
- `verification_tokens.user_id` is unique (1:1 `@OneToOne`) — a user can't simultaneously hold an email-verify and password-reset token.
- `events.start` and `events.end` are SQL reserved words; Liquibase quotes them but it's fragile.
- Spring Boot 4.x splits per-feature autoconfig out of `spring-boot-autoconfigure`. Engine-only deps (e.g. `liquibase-core`) are silently ignored — the matching `spring-boot-starter-*` must be on the classpath.
- Liquibase 5.x Maven goals are camelCase: `generateChangeLog`, `changelogSync`.

### Related wiki

`C:\Users\super\Obsidian\ProjectV-obsidian-claude` — broader project docs.