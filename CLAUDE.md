# CLAUDE.md

Rules for working in this repository. This is a **technical scaffold** for a
personal finance SaaS — see `SPEC.md` for goals/no-goals and `ARCHITECTURE.md`
for the system overview. No business features exist yet; the `health` slice
in both `backend` and `frontend` is a wiring reference (proves BE ↔ DB ↔
GraphQL ↔ FE work end to end), not a product feature. Treat it as a template
for how a real feature should be structured, and remove/repurpose it once the
first real feature lands.

## Backend (`backend/`) — feature-first + layered, Kotlin/Spring Boot/DGS

Base package: `com.financeapp`.

```
com.financeapp
├── common/            # cross-cutting: config, shared error types, base utils
└── features/
    └── <feature>/
        ├── api/            # DGS data fetchers + GraphQL-facing DTOs
        ├── application/    # use-case orchestration (@Service)
        ├── domain/         # entities, value objects — pure Kotlin
        └── infrastructure/ # adapters (JPA repositories, JDBC, external clients)
```

**Layer dependency rule (one-directional, enforced by convention today, ArchUnit later):**

- `domain` depends on nothing else in the feature. No Spring, no JPA, no DGS
  annotations.
- `application` depends only on its own feature's `domain` and `infrastructure`
  (+ `common`). It orchestrates use cases and may depend directly on an
  `infrastructure` class (e.g. a Spring Data `JpaRepository`, a JDBC adapter) —
  a domain-owned **port** (interface) is not required by default. See
  `docs/adr/002-ports-only-where-they-earn-it.md` for why.
- `infrastructure` may depend on Spring Data/JDBC/JPA and its own feature's
  `domain`. `@Component`/`@Repository` classes here don't need to implement a
  domain interface unless one exists for the reason below.
- Introduce a domain-owned **port** only when the dependency is genuinely
  volatile or swappable — a third-party/external API (e.g. a bank-data
  provider), something you expect to have multiple implementations of, or
  something you need to fake in a fast unit test without a real DB/network
  call. Straightforward persistence (an entity CRUD'd via JPA) does not
  need one.
- `api` depends only on its own feature's `application` (+ `common`). It never
  returns domain or JPA entities directly over GraphQL — always map to a
  dedicated payload/input type owned by `api`.

**Cross-feature rule:** a feature must never import another feature's
`domain`, `application`, or `infrastructure` package directly. If feature B
genuinely needs feature A's data, feature A exposes a narrow, clearly-named
interface in its own `application` layer for other features to call — or the
shared concept belongs in `common/` instead. Reaching into another feature's
internals is always a bug, not a shortcut.

**Migrations:** one Flyway file per change, `V{n}__description.sql`, in
`src/main/resources/db/migration`. Never edit a migration that has already
been applied (in any environment) — write a new one.

**GraphQL schema:** one `.graphqls` file per feature under
`src/main/resources/schema/`, named after the feature.

## Frontend (`frontend/`) — feature-first, React + TypeScript + Vite

```
src/
├── app/            # composition root: routing, providers, top-level layout
├── features/
│   └── <feature>/
│       ├── api/          # GraphQL queries/mutations + their result types
│       └── components/   # feature-specific React components
└── shared/         # cross-feature building blocks (components, hooks, utils, graphql client)
```

**Dependency direction:** `app → features → shared`. `shared` never imports
from `features` or `app`. Features never import from each other — if two
features need the same thing, promote it to `shared/`.

**GraphQL access:** components never call `fetch`/`axios`/a raw GraphQL
client directly. All access goes through a feature's own `api/` module using
Apollo Client (configured once in `app/providers/GraphQLProvider.tsx`).

## General

- Auth is **intentionally not implemented yet** (see `SPEC.md` no-goals).
  Don't bolt on ad hoc authentication/authorization to unblock a feature —
  raise it so a real decision gets made and recorded as an ADR.
- No business logic in DGS data fetchers or React components — they call into
  `application` services / feature `api` modules respectively.
- Record any decision that's expensive to reverse (schema-shaping choices,
  library swaps, cross-cutting patterns) as a new ADR in `docs/adr/`.

## Running the project

```bash
make up
```

Copies `.env.example` to `.env` (if missing) and starts `postgres`, `backend`
(http://localhost:8080/graphiql), and `frontend` (http://localhost:5173) via
Docker Compose. Opening the frontend should show a `health` status page
confirming the backend can reach the database.

For local (non-Docker) backend development:

```bash
make be-test
make be-run
```

If `backend/gradlew` is ever missing, regenerate it with `make be-wrapper`
(requires a local Gradle install, e.g. via sdkman) — see
`docs/adr/001-initial-setup.md` for why it isn't always pre-generated.

Run `make help` for the full list of backend/frontend run/build/test/lint
targets.

## Troubleshooting

- **Frontend can't reach the backend from the browser, but `curl` works
  fine.** `curl` doesn't enforce CORS; browsers do. Check that
  `CORS_ALLOWED_ORIGINS` in `.env` includes the exact origin the frontend is
  served from (`http://localhost:5173` by default). See `CorsConfig.kt` and
  `CorsConfigTest`.
