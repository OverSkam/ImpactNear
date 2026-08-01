# ImpactNear

A map-based platform for discovering and signing up to local volunteer events. Volunteers browse and apply to nearby events; organizers post events and review applicants; admins approve requests to become an organizer. Built with Spring Boot on the backend and React on the frontend.

(Internal codename: `projectV` — the Java package, service hostname, and this repo's history still use that name; `ImpactNear` is the public brand.)

This is a solo, self-taught project, built without prior commercial experience. It's intentionally broad in scope — JWT revocation, OAuth2, geospatial queries, object storage, caching, schema migrations — to demonstrate real backend engineering concerns rather than a CRUD-only demo.

## Features

**Authentication & authorization**
- Email/password login plus Google OAuth2. OAuth2 uses a dedicated `(provider, subject)` identity table rather than matching accounts by email, rejects unverified provider emails, invalidates the server session right after minting the JWT, and returns the token in the URL fragment so it never lands in server access logs.
- JWT access tokens carry a `tokenVersion` claim, checked on every request against a Redis-cached (MySQL-backed) per-user version. Bumping the version instantly invalidates every previously issued token — wired into password reset and organizer-role promotion. `POST /api/v1/auth/logout-all` exposes this as an explicit "log out everywhere" action.
- Redis reads are read-through with an 8-day TTL and a 2-second connection timeout; on a Redis outage the check fails open to MySQL so an infrastructure blip degrades performance rather than breaking auth.
- Email verification and password reset return identical responses whether or not the account exists, to prevent user enumeration.
- Roles: volunteer (`ROLE_USER`), organizer (`ROLE_ORGANIZER`), admin (`ROLE_ADMIN` — renamed from an earlier `ROLE_MODERATOR` via a dedicated migration). Admins review and approve requests to become an organizer.

**Events & geospatial discovery**
- Event locations are stored as MySQL spatial `POINT` columns (SRID 4326) via Hibernate Spatial.
- Nearby-event search runs a two-stage query: an `MBRContains` bounding-box prefilter (index-friendly) followed by exact `ST_Distance_Sphere` distance filtering, backed by a spatial index.
- The bounding-box calculation applies the `cos(latitude)` correction to the longitude delta, so results stay accurate away from the equator.
- Paginated, sortable listings with an allowlist on sortable fields to block property-path injection via `?sortBy=`.

**Participation workflow**
- Volunteers apply to events with an optional message; organizers approve or reject with their own reply message.
- Ownership-scoped queries throughout (`existsByIdAndUserId` and similar) so a user only sees what belongs to them.

**Event media**
- Uploaded images are re-encoded to JPEG server-side, which neutralizes polyglot-file attacks regardless of the client-supplied (and spoofable) `Content-Type`.
- Images are stored in Cloudflare R2. Deletes are DB-first, then R2, with warn-and-continue on storage failure — a deliberate trade-off favoring a recoverable orphaned file over an inconsistent database.

**Schema management**
- Liquibase is the single source of truth, with `spring.jpa.hibernate.ddl-auto=validate` enforced in every profile so Hibernate only verifies the schema at boot and never mutates it. Changesets are numbered and never edited once applied — a value rename, for example, goes through a new migration rather than a hand edit.

**Transactional email**
- Verification, password reset, and related emails go through the Resend HTTP API (`com.resend:resend-java`), not SMTP — HTML bodies, a verified sending domain, and room for delivery/bounce webhooks later.

## Tech stack

| Layer | Technology |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.0.1 (Web MVC, Security, Validation, Data JPA) |
| Build | Maven (`./mvnw`) |
| Database | MySQL + Hibernate Spatial (JTS), spatial `POINT` columns, spatial index |
| Migrations | Liquibase |
| Cache | Redis / Aiven Valkey (Lettuce client) |
| Object storage | Cloudflare R2 |
| Auth | JWT (`jjwt` 0.12.6), Spring Security, OAuth2 (Google) |
| Email | Resend HTTP API |
| Entity ↔ DTO mapping | ModelMapper |
| API docs | springdoc-openapi (Swagger UI) |
| Frontend | React 19.2, Vite 8, TypeScript 6 |
| Frontend state | Zustand 5 |
| Frontend forms | react-hook-form + Zod |
| Maps | Leaflet + react-leaflet |
| Frontend UI | Radix primitives + Tailwind CSS (shadcn/ui style) |
| Deploy target | Backend: Docker image (Render). Frontend: Vercel. |

## Architecture

Two-tier app: a React SPA talks to the Spring Boot REST API over JSON, authenticated with versioned JWT bearer tokens. In dev, Vite proxies `/api`, `/oauth2`, and `/login/oauth2` to the backend so the SPA can call relative URLs.

```
React SPA (Vite :5173) --/api/v1/** (JWT)--> Spring Boot (:8080) --JPA/Hibernate Spatial--> MySQL
                                                    |--tokenVersion cache--> Redis / Aiven Valkey
                                                    |--HTTPS-------------> Resend (email)
                                                    |--S3 API------------> Cloudflare R2 (images)
```

Backend package layout (`mm.projectV.*`): `config`, `controller`, `dto`, `enums`, `exception`, `filter`, `handler`, `mapper`, `model`, `repository`, `service`, `util`, `validation`.

### API surface

Base path `/api/v1`. Every response uses a standard envelope: `{ error, message, data }`.

- **`/auth`** — login, register, email verification, resend-verification, forgot/reset password (all public), plus `POST /logout-all` (authenticated — bumps the caller's token version).
- **`/events`** — public discovery (`random-events`), authenticated discovery (`recommended-events`, radius-based), single-event lookup, organizer CRUD (`ROLE_ORGANIZER`-gated), and the participation workflow (apply, review, list — organizer-gated where applicable).
- **`/users`** — `GET/PUT/PATCH /me` for the caller's own profile and location, plus an organizer-only `GET /{userId}` that deliberately returns just `{ name, surname }` as a privacy gate.
- **`/admin`** — organizer-request review/approval (`ROLE_ADMIN`).

Design decisions with non-obvious trade-offs (why token revocation uses a version counter instead of a denylist, why client-side logout doesn't revoke tokens server-side, etc.) are recorded as ADRs in the project wiki.

## Getting started

### Prerequisites

- Java 21
- Node.js (recent LTS)
- MySQL running locally, database `projectv`
- Redis running locally (e.g. `redis:7-alpine` via Docker) — used for JWT token-version caching
- A [Resend](https://resend.com) API key and a verified sending domain, for transactional email
- Cloudflare R2 (or S3-compatible) bucket + credentials, for event image storage

### Backend

```bash
cd projectV                 # backend repo root
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The default active profile is `prod`, which requires production env vars — always pass `-Dspring-boot.run.profiles=dev` for local runs. The dev profile points at a local MySQL (`root`, empty password) and local Redis; every other secret (Resend key, R2 credentials, OAuth client) is still read from the environment. The API listens on `http://localhost:8080`; Swagger UI is available once springdoc is on the classpath (check `/swagger-ui.html` or `/swagger-ui/index.html`).

Key environment variables (prod profile; see `application-prod.properties` for the full list):

| Variable | Controls |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | MySQL connection |
| `DB_PORT` | **Misnamed** — actually sets `server.port`, not the DB port |
| `FRONTEND_URL` | Allowed CORS origin |
| `URL_FOR_EMAIL` | Base URL used in verification / reset links |
| `RESEND_API_KEY`, `MAIL_FROM` | Transactional email (Resend) |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_USERNAME`, `REDIS_PASSWORD`, `REDIS_SSL` | Redis/Valkey connection (prod requires TLS) |

### Frontend

```bash
cd projectV-frontend
npm install
npm run dev          # http://localhost:5173, proxies /api to :8080
```

Set `VITE_PROXY_TARGET` if the backend runs somewhere other than `localhost:8080`.

### Smoke test

1. Register a user, then check the backend log / Resend dashboard for the verification email.
2. Click the verification link, then log in — you should land on `/events`.
3. To exercise organizer/admin flows, promote a user's role directly in the database or via the admin approval flow once an admin account exists.

## Known limitations & roadmap

This project has been through a structured code review. Documenting the findings here rather than hiding them — fixing these in priority order is the current roadmap.

**Open issues, by severity**

1. **IDOR on participation requests.** Organizer ownership of an event isn't verified before listing its participation requests — one organizer can currently view another organizer's applicants and private review notes. Sibling methods in the same service do check ownership (`existsByIdAndUserId`); this one was missed. Fix: apply the same ownership check here.
2. **Some admin responses serialize JPA entities directly** instead of going through a DTO mapper, which can leak sensitive fields (e.g. password hashes) or trigger lazy-loading recursion on bidirectional relations. Fix: route every response through a DTO, no exceptions.
3. **Unprotected capacity race condition.** Concurrent approvals of participation requests can oversell an event — the capacity check was identified, then commented out, and the participant counter is a read-modify-write with no locking. There's no `@Version` field anywhere in the model layer. Planned fix: optimistic locking (`@Version`) or an atomic conditional update (`UPDATE ... WHERE participants_number < capacity`). The same endpoint also doesn't validate the incoming status value.
4. **Test suite needs a rewrite.** Existing tests target routes that no longer exist, and none cover the actual risk areas (auth filter, participation state machine, ownership checks). Planned: rebuild around `@DataJpaTest` + Testcontainers (real MySQL, needed for the spatial query), wired into CI.
5. **Some error paths return 500 instead of a proper 4xx** — e.g. bad login credentials fall through to a generic 500 rather than a handled 401.
6. **Smaller polish items:** Lombok `@Data`/`@EqualsAndHashCode` on entities with lazy bidirectional relations (known landmine); duplicate dependency declarations in `pom.xml`; inconsistent config access in one utility class; a profile-update path that doesn't bump `tokenVersion` on password change, unlike the reset flow; mutation endpoints all returning `200` instead of `201`/`204`; a couple of redundant repository method overrides.

**Roadmap, in priority order**

1. Rebuild the test suite and wire it into CI.
2. Add concurrency control (optimistic locking) to the participation/capacity flow.
3. Centralize authorization so ownership is enforced structurally, not re-derived per method.
4. Enforce a strict API boundary — DTOs only, correct HTTP status codes, RFC 9457 `ProblemDetail` for errors.
5. Consistency sweep across auth flows (token revocation, enumeration-proofing, ownership checks) so every rule applies everywhere it should.

**Explicitly deprioritized:** more infrastructure breadth (e.g. Kafka, Kubernetes). The project already covers Redis, OAuth2, spatial SQL, object storage, and migrations — the next unit of value is correctness and test coverage, not new tech.

## License

Add a license (e.g. MIT) before publishing publicly.
