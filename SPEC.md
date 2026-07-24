# SPEC.md

## Product

A SaaS for managing personal finances. This document currently covers the
**technical scaffold only** — the empty skeleton the product will be built
into. Business scope (accounts, transactions, budgets, etc.) is deliberately
out of scope until a dedicated product spec is written.

## Goals (this scaffold)

- A monorepo with a Kotlin/Spring Boot backend and a React/TypeScript
  frontend that run together locally via Docker Compose with one command.
- A GraphQL API (Netflix DGS) serving the frontend, with schema and
  resolvers organized per-feature.
- PostgreSQL wired up with Flyway migrations, proven to work end to end
  (not just configured — an actual query round-trips through it).
- A `feature-first + layered` folder structure on both sides of the stack,
  documented in `CLAUDE.md`, illustrated with one minimal example slice
  (`health`) rather than left abstract.
- A Makefile with the standard commands a new contributor needs
  (`up`, `down`, `*-run`, `*-test`, `*-lint`, `*-build`).
- `.env.example` covering every environment variable the stack needs.
- A record of the foundational technical decisions as an ADR
  (`docs/adr/001-initial-setup.md`), so "why DGS and not X" etc. is answered
  once and not re-litigated.

## No-goals (for now)

- **No business features.** No accounts, transactions, categories, budgets,
  reports, or any finance-domain modeling. The `health` slice exists purely
  to prove the stack is wired correctly, not as a template business feature.
- **No authentication/authorization.** Explicitly deferred. The backend has
  no login, no session/JWT handling, and no per-user data model. This is a
  known gap, not an oversight — it needs its own decision (and likely its
  own ADR) before any real feature can be built, since every future feature
  will need to know "whose data is this."
- **No production deployment story.** Docker Compose here is for local
  development only. Hosting, CI/CD, secrets management, and observability
  in a deployed environment are not addressed.
- **No multi-tenancy, multi-currency, or bank-sync/Plaid-style integration**
  — these are real product decisions for a later spec, not scaffold concerns.
- **No API versioning strategy, rate limiting, or caching layer** — premature
  before there's a real API surface to protect.

## Key decisions

| Decision | Choice | Rationale |
|---|---|---|
| Monorepo layout | `backend/` + `frontend/` at repo root | Matches existing `.gitignore`; simplest for a small team, one Docker Compose file can orchestrate both. |
| Backend language/framework | Kotlin + Spring Boot 3.3 (Java 21 toolchain) | As specified; Java 21 is the current LTS supported by Spring Boot 3.3. |
| GraphQL server library | Netflix DGS | Chosen explicitly over Spring for GraphQL / graphql-kotlin — schema-first, mature, well-documented Spring Boot integration. |
| Architecture style | Feature-first at the top level, layered (`api/application/domain/infrastructure`) within each feature | As specified; balances discoverability (features) with clear dependency direction (layers). Full rules in `CLAUDE.md`. |
| Database | PostgreSQL 16, Flyway-managed migrations | As specified; Flyway gives an explicit, reviewable migration history from day one. |
| Frontend | React 18 + TypeScript + Vite | Fast dev loop, no framework lock-in beyond React itself. |
| GraphQL client | Apollo Client | De facto standard for React + GraphQL; normalized cache, generated-types-friendly. |
| Auth | **Deferred** — no auth in this scaffold | Explicit choice from the team to not guess at an auth strategy before it's needed; must be revisited via its own ADR before the first real feature ships. |
| Local orchestration | Docker Compose (postgres + backend + frontend) | One-command local environment; no k8s/Helm needed at this stage. |
| Backend build | Gradle (Kotlin DSL) | Standard for Kotlin/Spring projects; matches existing `.gitignore` (`backend/build/`, `backend/.gradle/`). |

## Open questions (not blocking the scaffold, worth revisiting soon)

- Auth strategy (self-rolled JWT vs. external IdP) — needs its own ADR before
  the first feature that touches user data.
- Whether GraphQL codegen (frontend types from the schema) is worth adopting
  once the schema grows past a couple of features.
- Whether ktlint/detekt rules should be tightened (currently ktlint defaults
  + tabs via `.editorconfig`, no detekt).
