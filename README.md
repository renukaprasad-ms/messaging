# Messaging

A Spring Boot + React modular monolith. Authentication, users, sessions, companies, roles, and notifications have separate modules.

## Code structure

Controllers call services from their own module. Services access repositories only from their own module; cross-module work goes through the other module's service. DTOs define API contracts, repositories handle queries, and `AccessPolicy` defines company access rules. `ModuleBoundaryTests` checks the repository boundary.

Examples:

- `PlatformAdminController → PlatformAdminService → UserService → UserRepository`
- `AuthController → AuthService → UserService / UserSessionService`
- `CompanyController → CompanyService → CompanyAccessService / CompanyMembershipService`

Java uses ordinary imports and Google Java Format. The only qualified class names inside queries are required JPQL constructor/enum references. Frontend formatting uses Prettier.

## Access rules

| Account or role | Access |
| --- | --- |
| Guest | Sign in, register, password recovery |
| Pending verification | Current account, email verification, password change, logout |
| Active, verified user | Create a company; enter companies with an active membership |
| Company OWNER / ADMIN | Read and update that company's details |
| Company MANAGER / MEMBER | Read that company's details |
| Platform ADMIN / SUPERADMIN | Platform user administration |
| Suspended / disabled | No authenticated access |
| Temporary bootstrap password | Password change required before workspace/admin access |

Company roles do not grant platform privileges. Platform roles do not bypass company membership checks. Public registration cannot assign privileged roles. Role changes are currently operator-managed; there is no public role-grant endpoint.

Every authenticated request checks the live user, roles, and session with one indexed query. Logout and password changes revoke access immediately. Refresh rotation is serialized per user with a database row lock; different users do not block each other. JWTs have random IDs so tokens issued in the same second remain distinct.

## Passwords and verification

New passwords require **8–72 characters**, uppercase, lowercase, a digit, and an ASCII special character. The backend also enforces BCrypt's 72 UTF-8 byte limit. Registration, reset, and password-change screens share the same strength bar and checklist. The bar indicates rule completion and length, not a cryptographic entropy estimate.

OTP issuance, attempt counting, and one-time consumption use atomic Redis scripts. Keys are purpose-specific, expire automatically, and hash contact details. An email verification code cannot reset a password. Password-reset tokens use atomic get-and-delete; a failed database transaction consumes the token and the user must request another code (fail closed).

Cookie-authenticated mutations require CSRF tokens. The frontend fetches `/api/auth/csrf`, sends `X-XSRF-TOKEN`, restores `/api/auth/me` on reload, and shares a single refresh request across concurrent 401 responses. See [Spring Security's CSRF documentation](https://docs.spring.io/spring-security/reference/7.0/servlet/exploits/csrf.html).

## Database migrations and bootstrap accounts

Flyway owns the schema; Hibernate only validates it. See [Spring Boot migration configuration](https://docs.spring.io/spring-boot/how-to/data-initialization.html).

- V1 creates the existing schema on an empty database.
- V2 adds platform-role assignments and revocable access sessions. Existing sessions are invalidated once.
- V3 seeds `renukaprasadms00@gmail.com` and `renukaprasad.dev@gmail.com` as SUPERADMIN. Only BCrypt hashes are committed. New accounts require a password change. Existing matching accounts keep their passwords and status.

The generated temporary credentials for this workspace are in **`.tools/superadmin-credentials.txt`**, which is ignored by Git. Keep that file private. After a password change, the user signs in again.

For an existing Hibernate-managed database, back it up and compare its schema to V1 first. Then set `FLYWAY_BASELINE_ON_MIGRATE=true` for the first migration run only. This records the existing schema as version 1 and applies V2 onward. Return the setting to `false` afterwards. Do not enable baselining against an arbitrary database. Test upgrades on a restored copy before production.

## Small server defaults: 2 CPUs / 2 GiB RAM

The default Compose stack runs the frontend, backend, PostgreSQL, Redis, and Kafka.

| Service | Container memory limit |
| --- | ---: |
| Backend | 896 MiB (512 MiB Java heap) |
| PostgreSQL | 384 MiB |
| Redis | 128 MiB (64 MiB data cap, no eviction) |
| Kafka | Configure for your host capacity |
| Nginx frontend | 64 MiB |

These limits total 1,472 MiB, leaving approximately 576 MiB for the OS and Docker on a 2 GiB host. They are starting limits, not a throughput guarantee. Build images on a workstation or CI; building Maven and Node images on this host can exceed its memory budget. Monitor resident memory, CPU, database connections, response latency, and Redis capacity under representative load.

The backend shares one five-connection pool when read and write database settings match. It uses at most 25 HTTP worker threads. Lists return 25 records, fetch related company/role data together, and avoid total-count queries. Membership checks use indexed `EXISTS` queries. Page-number pagination has O(offset + page size) database work at deep pages; switch to cursor pagination if those lists become large. Password checking is O(password length), bounded at 72 bytes; BCrypt is intentionally CPU-expensive and login requests are rate-limited.

Email uses SMTP directly when async notifications are disabled, with five-second network timeouts; delivery failures return an error and allow retry. Configure a real SMTP provider. **SMS is not implemented** and explicitly reports unavailable; it never logs OTPs. By default, `APP_NOTIFICATION_ASYNC=true` and Compose starts Kafka for queued notification delivery. Publishing waits for broker acknowledgement; consumers retry twice and then publish to a `.DLT` topic. Delivery is at-least-once, so duplicate email is possible. Restrict broker access, monitor dead letters, and do not replay expired OTPs. Kafka adds memory overhead beyond the small-host baseline.

Use HTTPS with `JWT_COOKIE_SECURE=true`, keep signing keys and SMTP credentials outside Git, and configure `APP_CORS_ALLOWED_ORIGINS` for the actual frontend origin. PostgreSQL and Redis host ports bind only to loopback. The application ignores raw forwarding headers; configure trusted proxy handling before relying on client IP limits behind a proxy. Clients sharing an IP also share its 30-login-per-minute limit.

## Development and checks

Requires Java 25, Node, and Docker for integration tests. Copy `.env.example` to `.env` and configure signing keys and SMTP. Existing `.env` values override the new defaults; reduce any old pool/worker values before deploying to the small host.

```sh
docker compose -f compose.test.yml up -d --wait
cd backend
./mvnw test
./mvnw spotless:check
cd ../frontend
npm ci
npm test
npm run build
npm run lint
npm run format:check
```

Tests use a separate PostgreSQL database on port 55432 and Redis on port 56379. Email is mocked; tests do not deliver messages. Set `TEST_HOST=host.docker.internal` when running Maven inside Docker. Stop test services with `docker compose -f compose.test.yml down`.

Format code with `./mvnw spotless:apply` and `npm run format`. CI runs the same checks. Tests cover password boundaries, module boundaries, role/tenant access, CSRF, account restrictions, concurrent OTP consumption and refresh rotation, session revocation, and frontend session/route behavior.

The frontend layout adapts [Messaging Dashboard V2 in Figma](https://www.figma.com/design/H1P6tz6ARebL4JeC13vs3U/Messaging-SaaS-%E2%80%94-Authentication?node-id=9-3). Future messaging features are visibly unavailable, and dashboard metrics show empty states until a real analytics API exists.
