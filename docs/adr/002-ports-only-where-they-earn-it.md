# ADR 002: Domain-owned ports are opt-in, not mandatory

- **Status:** Accepted
- **Date:** 2026-08-09

## Context

ADR 001 set up the `health` slice as a template for feature layering, and it
included a full ports-and-adapters pattern: `domain` defined a
`DatabaseHealthPort` interface, and `infrastructure`'s `DatabaseHealthAdapter`
implemented it, so `application` depended on the interface rather than the
concrete adapter. `CLAUDE.md` codified this as a blanket rule — every
`infrastructure` dependency needed a domain-owned port.

Ports-and-adapters earns its cost under specific conditions: multiple
implementations behind one interface (swappable providers, ORM/DB migration),
fast unit tests that fake external dependencies instead of hitting a real DB
or network, or multiple contributors who need the boundary structurally
enforced. Revisiting this before any real feature was built, none of those
conditions hold for this project:

- It's a long-lived personal project, but each feature is expected to have
  exactly one infrastructure implementation — no planned provider swaps, no
  planned DB/ORM migration.
- Testing will lean on integration tests against a real database
  (Testcontainers), not fake-backed unit tests — so the "fake the port"
  benefit isn't being used.
- It's solo work, so there's no need to structurally enforce a boundary
  between application and infrastructure to keep multiple contributors
  aligned.

Applied blanket, the pattern was adding an interface and a second class per
infrastructure dependency for a payoff the project isn't set up to collect.

## Decision

Domain-owned ports are **opt-in**, not a default. `application` may depend
directly on an `infrastructure` class (a Spring Data `JpaRepository`, a JDBC
adapter, a REST client wrapper) without an intervening domain interface.

Introduce a port when a dependency actually meets one of the conditions
above — most likely, in this project, a third-party/external API (e.g. a
future bank-data provider like Plaid) where isolating application logic from
a specific vendor SDK is worth it regardless of test strategy. Straightforward
persistence (an entity read/written via JPA) does not need one.

The `health` slice was updated to match: `DatabaseHealthPort` was removed,
and `HealthCheckService` now depends on the concrete `DatabaseHealthAdapter`
directly. `CLAUDE.md`'s layer-dependency rule was updated accordingly.

## Consequences

- Less boilerplate per feature: one class per infrastructure dependency
  instead of an interface-plus-implementation pair, when nothing needs the
  indirection.
- `HealthCheckServiceTest` now mocks the concrete `DatabaseHealthAdapter`
  class directly (MockK mocks final Kotlin classes natively, so this needs
  no additional tooling).
- If a future feature turns out to need a swappable/fakeable dependency
  after all (a second data provider shows up, or fast unit tests without a
  DB become worth it), introduce a port for *that* dependency at that point
  — this decision doesn't forbid ports, it just stops requiring them
  everywhere by default.
- If the project's shape changes materially (e.g. it gains other
  contributors, or a feature needs a genuine second implementation), revisit
  this ADR rather than silently reintroducing ports piecemeal.
