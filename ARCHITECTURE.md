# ARCHITECTURE.md

One-pager. For rationale behind these choices see `SPEC.md` (§ Key decisions)
and `docs/adr/`. For folder-level rules see `CLAUDE.md`.

## System overview

```
┌──────────────────┐        GraphQL (HTTP)        ┌──────────────────────┐        SQL         ┌──────────────┐
│  frontend         │  ────────────────────────▶  │  backend              │  ───────────────▶  │  postgres     │
│  React + TS + Vite │                              │  Kotlin + Spring Boot │                     │  (Flyway-     │
│  Apollo Client     │  ◀────────────────────────  │  Netflix DGS          │  ◀───────────────  │   managed)    │
└──────────────────┘                              └──────────────────────┘                     └──────────────┘
```

All three run as separate containers via `docker-compose.yml` for local
development; there is no production deployment topology defined yet
(see `SPEC.md` no-goals).

## Backend

- **Style:** feature-first at the top level, layered within each feature
  (`api → application → domain ← infrastructure`). Full rules in `CLAUDE.md`.
- **API:** GraphQL via Netflix DGS. Schema files live per-feature under
  `src/main/resources/schema/`; resolvers (`@DgsComponent`) live in each
  feature's `api` layer.
- **Persistence:** Spring Data JPA + Flyway. Every schema change is a new,
  immutable migration file — no migration is ever edited after it's applied.
- **Domain layer independence:** domain code defines interfaces ("ports") for
  anything it needs from the outside world; `infrastructure` implements them.
  This keeps business logic (once there is any) testable without a database
  and free of framework annotations.
- **Cross-feature communication:** never direct package access into another
  feature's internals — only through a feature's own `application`-layer
  interface, or via `common/` for genuinely shared concepts.

## Frontend

- **Style:** feature-first. Each feature owns its GraphQL operations
  (`api/`) and components (`components/`). `shared/` holds cross-feature
  building blocks; `app/` is the composition root (routing, Apollo provider).
- **Data fetching:** exclusively through Apollo Client, configured once in
  `app/providers/GraphQLProvider.tsx`. No feature talks to the network
  directly — components consume hooks generated from each feature's own
  GraphQL documents.
- **Dependency direction:** `app → features → shared`, never sideways
  between features, never backwards from `shared`.

## Local environment

`docker-compose.yml` defines three services: `postgres`, `backend`,
`frontend`. `make up` copies `.env.example` to `.env` (if missing) and
starts everything. The `health` GraphQL query is the smoke test that proves
the whole chain works: frontend queries backend, backend queries postgres,
result renders in the browser.

## What's deliberately not here yet

Authentication, authorization, a real domain model, and a production
deployment target. See `SPEC.md` no-goals for the full list and why.
