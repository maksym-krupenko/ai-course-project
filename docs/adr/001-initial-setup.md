# ADR 001: Initial technical scaffold

- **Status:** Accepted
- **Date:** 2026-07-24

## Context

We're starting a personal finance SaaS from an empty repository. Before any
product work, we need a working technical skeleton: a backend, a frontend,
a database, and a way to run them together locally. The stack was given
(Kotlin/Spring Boot, React/TS, PostgreSQL, GraphQL, Docker) and the
architecture style was given (feature-first + layered). What was still open:
which GraphQL server library to use, how (or whether) to handle auth in this
first pass, and how much product scope to bake into the scaffold.

## Decision

1. **GraphQL server library: Netflix DGS**, not Spring for GraphQL or
   graphql-kotlin. Chosen explicitly by the team over the alternatives.
   Schema-first (`.graphqls` files), mature Spring Boot integration via
   `graphql-dgs-spring-boot-starter`.
2. **Auth is deferred entirely.** No login, session, JWT, or user model
   exists in this scaffold. This is intentional, not an oversight — auth is
   a decision with real security consequences and deserves its own ADR once
   there's an actual feature that needs to know "whose data is this."
3. **Scope is technical-only.** No finance-domain features (accounts,
   transactions, budgets, ...) exist yet. A single `health` vertical slice
   (backend `features/health/{api,application,domain,infrastructure}` +
   frontend `features/health/{api,components}`) exists purely to prove the
   full chain works: frontend → GraphQL → backend → Postgres → back to the
   frontend. It also serves as the reference example for how the
   `CLAUDE.md` layering rules apply in practice.
4. **Monorepo layout:** `backend/` and `frontend/` at the repo root,
   orchestrated by a single root `docker-compose.yml` and `Makefile`. This
   matches the `.gitignore` that already existed before this scaffold.
5. **Backend build tool: Gradle (Kotlin DSL)**, Kotlin 1.9.24, Spring Boot
   3.3.4, Java 21 toolchain. Flyway for migrations, ktlint for style
   (tabs, per `.editorconfig` — matches the codebase's existing indentation
   rather than fighting it).
6. **Frontend tooling:** Vite + React 18 + TypeScript, Apollo Client for
   GraphQL, Vitest + Testing Library for tests, ESLint (flat config).

## Consequences

- The `health` slice must be treated as a template, not a feature to build
  on top of. It should be removed or repurposed once the first real feature
  lands — see `CLAUDE.md`.
- Every future feature will eventually need to answer "whose data is this,"
  since there's no auth/user model yet. The auth ADR should happen before
  or alongside the first feature that stores user-specific data.
- `backend/gradlew` (the Gradle wrapper, pinned to Gradle 8.10.2) was
  generated via `make be-wrapper` and is committed. The Docker build path
  (`backend/Dockerfile`) doesn't need it either way — it builds via the
  `gradle:8.10.2-jdk21-alpine` image directly.
- **CORS had to be added.** The initial scaffold had no CORS configuration,
  which worked for `curl`/server-to-server checks but silently broke the
  browser: the `OPTIONS` preflight to `/graphql` came back `403`, so the
  frontend's `health` query failed with "Failed to fetch" even though the
  backend itself was healthy. Fixed with `common/config/CorsConfig.kt`,
  which allows the origin(s) in `app.cors.allowed-origins`
  (`CORS_ALLOWED_ORIGINS` env var, defaulting to `http://localhost:5173`).
  Covered by `CorsConfigTest` (allowed origin succeeds, unconfigured origin
  gets `403`) so this can't silently regress. Any new frontend origin
  (e.g. a deployed URL, later) needs to be added to that allow-list.
- **Fully verified end to end**, not just assumed: `make be-wrapper && ./gradlew
  ktlintCheck test bootJar` all pass (compile, ktlint, unit test, the
  `HealthQueryIntegrationTest` Testcontainers integration test, and the
  new `CorsConfigTest`, against a real Postgres container). The frontend
  passes install/typecheck/lint/test/build. `docker compose up` was run for
  real and the `health` query was confirmed rendering correctly
  (`Backend status: UP`, `Database reachable: true`) in an actual browser,
  with a clean console.
