# Plan 001 — Expense Logging & Retrieval (v1) implementation

- **Status:** Planned — not yet implemented
- **Date:** 2026-08-09

## Implements

- [`docs/product/001-expense-logging.md`](../product/001-expense-logging.md) — the product spec this plan
  delivers (scope, acceptance criteria, discovery log).
- [`docs/domain/expense-tracking.md`](../domain/expense-tracking.md) — the bounded context and invariants
  (I-1–I-4) this plan's tests must prove.
- [`SPEC.md`](../../SPEC.md) — supersedes the scaffold's "no business features" no-goal; this is that first
  feature.
- [`CLAUDE.md`](../../CLAUDE.md) — the layering rules (`api/application/domain/infrastructure`,
  feature-first frontend) this plan follows, using the `health` slice as the structural template.
- [`docs/adr/002-ports-only-where-they-earn-it.md`](../adr/002-ports-only-where-they-earn-it.md) — governs
  the "no domain-owned port for `ExpenseRepository`" call below.

## Context

`docs/product/001-expense-logging.md` and `docs/domain/expense-tracking.md`
specify the first real business capability for this app: recording and
retrieving expenses. Today the codebase only has the `health` slice, a
wiring reference with no business logic (per `CLAUDE.md`, it should be
"repurposed" once a real feature lands). This plan implements v1 exactly as
specified — record/edit/delete an expense, retrieve expenses for an
arbitrary date period (including a single day) — following the layered
feature structure `health` demonstrates, and applying ADR 002 (no
domain-owned ports for plain JPA persistence).

Two decisions were confirmed with the user before finalizing this plan:

1. **Categories are a fixed set of Kotlin objects, not a DB table, and not a
   GraphQL enum.** A `Category` is modeled as a sealed class with each
   category a `data object` carrying a `code` and a `label` — a real object
   / type, not a bare enum literal. This is deliberate: it exposes `Category`
   over GraphQL as an object type (`type Category { code: ID! label: String! }`),
   so a category is a queryable thing with fields, not a fixed wire literal —
   and it means adding a per-category attribute later (an icon, a color, a
   sort order) is a field addition, not a schema-shape change. The whole set
   still lives in one file (`Category.kt`) to keep the eventual move to a DB
   table contained, per the spec's framing of this as a near-future point of
   change. The concrete v1 list (no names were specified in the product spec)
   is: `GROCERIES`, `DINING_OUT`, `TRANSPORT`, `HOUSING`, `UTILITIES`,
   `HEALTH`, `ENTERTAINMENT`, `SHOPPING`, `TRAVEL`, `EDUCATION`,
   `PERSONAL_CARE`, `OTHER` (as `code`s; each gets a human-readable `label`).
2. **The `health` slice stays, moved to `/health`.** The frontend root route
   (`/`) now renders the expense logging feature; `/health` keeps the
   existing `HealthStatus` diagnostic page. Nothing in the backend `health`
   feature changes.

Every invariant in `docs/domain/expense-tracking.md` (I-1 amount > 0, I-2
exactly one category, I-3 category ∈ predefined set, I-4 currency = system
currency) must have a passing test, per the spec's acceptance criteria.

## Backend — `backend/src/main/kotlin/com/financeapp/features/expense/`

Follow the `health` slice's four-layer split
(`domain → infrastructure ← application → api`), reusing its actual
patterns (see `features/health/*` for the concrete style/annotations to
match) rather than reinventing them. Key decisions, made because `health`
sets no precedent for them:

- **No domain-owned port** for `ExpenseRepository` — `application` depends
  on the Spring Data interface directly, exactly like `HealthCheckService`
  depends on `DatabaseHealthAdapter` (ADR 002).
- **Invariants live in domain constructors** (`init` blocks throw on
  violation) so illegal `Expense`/`Money`/`Period` values are
  unconstructible, not just rejected after the fact.
- **New `common/error/` package** (currently `common/` only has `config/`):
  `DomainValidationException` (I-1/I-3/I-4/period-range violations) and
  `NotFoundException` (missing id on edit/delete). CLAUDE.md already names
  `common/` as the home for "shared error types" — this is the first use of
  that intent.
- **`api` never imports `domain` types by name.** `health`'s DTOs never had
  to think about this (all fields were already primitives). Here,
  `ExpenseDataFetcher.kt` owns its own `CategoryPayload(code: String, label: String)`
  and `ExpensePayload` (with `category: CategoryPayload`, not the domain
  `Category` type), and `RecordExpenseInput`/`EditExpenseInput` accept a
  plain `categoryCode: String`. The `Expense → ExpensePayload` and
  `Category → CategoryPayload` mappings are written inline using inferred
  lambda parameters (`.map { expense -> ExpensePayload(..., category = CategoryPayload(code = expense.category.code, label = expense.category.label), ...) }`)
  so no `import ...domain.Expense` / `import ...domain.Category` is ever
  needed — mirroring how `HealthDataFetcher` uses `result.status` without
  importing `HealthStatus`.
- **Rely on DGS's default exception→GraphQL-error behavior** — no custom
  `DataFetcherExceptionHandler`. A thrown exception in a resolver already
  becomes an `errors[]` entry with HTTP 200 (never a 500), which is enough
  to satisfy "surfaces as a GraphQL error, not a crash." No precedent exists
  in this codebase for finer-grained error classification, and CLAUDE.md
  says not to build for hypothetical needs — revisit only if the frontend
  later needs to distinguish input-invalid from actual-bug errors.

### Files

- `domain/Money.kt` — `data class Money(val amount: BigDecimal, val currency: String = "PLN")`; `init` enforces I-1 (`amount > ZERO`) and I-4 (`currency == "PLN"`), throwing `DomainValidationException`.
- `domain/Category.kt` — `sealed class Category(val code: String, val label: String)` with each category a `data object` (`data object Groceries : Category("GROCERIES", "Groceries")`, ... through `data object Other : Category("OTHER", "Other")`), plus `companion object { val all: List<Category> = listOf(Groceries, ...); fun fromCode(code: String): Category = all.find { it.code == code } ?: throw DomainValidationException(...) }` (enforces I-3). The single seam to touch when categories become a DB table later — and the first sealed-class-of-`data object`s pattern in this codebase (chosen over `enum class` so `Category` is a real typed object exposed as a GraphQL object type, not a wire-level enum literal).
- `domain/Period.kt` — `data class Period(val from: LocalDate, val to: LocalDate)`; `init` throws if `from.isAfter(to)`. A single-date lookup is just `from == to` — no separate type/method for it, per the domain doc.
- `domain/Expense.kt` — `data class Expense(val id: Long? = null, val amount: Money, val expenseDate: LocalDate, val category: Category, val note: String?)`. I-2 (exactly one category) is structural: `category` is a single non-nullable value, never a collection.
- `infrastructure/ExpenseEntity.kt` — `@Entity @Table(name = "expenses")`, fields mirroring the migration below (`amount: BigDecimal`, `currency: String`, `expenseDate: LocalDate`, `category: String`, `note: String?`). This is the **first real JPA `@Entity`** in the codebase (`health` only used raw `JdbcTemplate`) — `spring.jpa.hibernate.ddl-auto: validate` means the migration must exist and match exactly before the app will boot.
- `infrastructure/ExpenseRepository.kt` — `interface ExpenseRepository : JpaRepository<ExpenseEntity, Long>` with `findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(from, to)`.
- `infrastructure/ExpenseEntityMapper.kt` — `Expense.toEntity()` / `ExpenseEntity.toDomain()` extension functions; the one place the DB's `category` string column and the `Category` object set meet (`category.code` / `Category.fromCode(category)`).
- `application/ExpenseService.kt` — `@Service` with `record(amount, expenseDate?, categoryCode, note)` (defaults `expenseDate` to `LocalDate.now()` when null — the "defaults to today" rule is a backend guarantee, not just a UI nicety), `edit(id, amount, expenseDate, categoryCode, note)` (full-replace, throws `NotFoundException` if missing), `delete(id)` (throws `NotFoundException` if missing, otherwise hard delete — the domain doc explicitly says soft delete only matters once something downstream needs it, and nothing does yet), `findByPeriod(from, to)` (constructs `Period` to get range validation), `listCategories() = Category.all`.
- `api/ExpenseDataFetcher.kt` — `@DgsComponent` with `@DgsQuery fun expenses(from, to)`, `@DgsQuery fun categories()`, `@DgsMutation fun recordExpense(input)`, `@DgsMutation fun editExpense(id, input)`, `@DgsMutation fun deleteExpense(id): ID` (returns the deleted id so Apollo can evict it from cache). Owns `CategoryPayload`/`ExpensePayload`/`RecordExpenseInput`/`EditExpenseInput` DTOs in the same file, per the `HealthPayload` pattern; `categories()` maps `Category.all` to `List<CategoryPayload>` inline.

### Schema — `backend/src/main/resources/schema/expense.graphqls`

```graphql
scalar BigDecimal
scalar LocalDate

type Category {
    code: ID!
    label: String!
}

type Expense {
    id: ID!
    amount: BigDecimal!
    currency: String!
    expenseDate: LocalDate!
    category: Category!
    note: String
}

input RecordExpenseInput {
    amount: BigDecimal!
    expenseDate: LocalDate
    categoryCode: ID!
    note: String
}

input EditExpenseInput {
    amount: BigDecimal!
    expenseDate: LocalDate!
    categoryCode: ID!
    note: String
}

extend type Query {
    expenses(from: LocalDate!, to: LocalDate!): [Expense!]!
    categories: [Category!]!
}

type Mutation {
    recordExpense(input: RecordExpenseInput!): Expense!
    editExpense(id: ID!, input: EditExpenseInput!): Expense!
    deleteExpense(id: ID!): ID!
}
```

`Category` is a GraphQL object type, not an enum — matching the Kotlin
sealed-class-of-objects representation. Mutation inputs can't reference an
output object type directly, so `RecordExpenseInput`/`EditExpenseInput` take
a `categoryCode: ID!` scalar (matching `Category.code`) rather than a
`Category` input; the resolver looks up the full `Category` object from that
code via `Category.fromCode`.

`extend type Query` is required since `health.graphqls` already declares
`type Query`. `type Mutation` (not `extend`) is correct — this is the first
mutation type in the schema.

**New dependency:** add `implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars")`
to `backend/build.gradle.kts` (version resolved via the existing DGS BOM) —
first use of extended scalars in this repo, needed for `BigDecimal`/`LocalDate`
scalars (`Float` would violate the amount-precision invariant; a plain
`String` loses type safety). DGS 9.x auto-registers scalars whose schema name
matches an `ExtendedScalars` constant with no extra code — verify this at
integration-test time; if it doesn't auto-wire, add a small
`common/config/GraphQLScalarConfig.kt` `@DgsComponent` with a
`@DgsRuntimeWiring` method registering `ExtendedScalars.GraphQLBigDecimal`
and `ExtendedScalars.LocalDate`.

### Migration — `backend/src/main/resources/db/migration/V2__create_expenses_table.sql`

```sql
CREATE TABLE expenses (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
    expense_date DATE NOT NULL,
    category VARCHAR(32) NOT NULL CHECK (
        category IN (
            'GROCERIES', 'DINING_OUT', 'TRANSPORT', 'HOUSING', 'UTILITIES',
            'HEALTH', 'ENTERTAINMENT', 'SHOPPING', 'TRAVEL', 'EDUCATION',
            'PERSONAL_CARE', 'OTHER'
        )
    ),
    note TEXT
);

CREATE INDEX idx_expenses_expense_date ON expenses (expense_date);
```

The `category` column stores a `Category.code` string; the `CHECK` list is
hand-kept in sync with the codes declared in `Category.kt` — accepted
coupling given categories are explicitly code, not a table, for now.

### Backend tests (flat under `features/expense/`, matching `health`'s pattern)

- `MoneyTest.kt`, `CategoryTest.kt`, `PeriodTest.kt` — pure unit tests
  (`kotlin.test`), proving I-1, I-3, I-4, and period range validation at the
  domain level. `CategoryTest.kt` covers `Category.fromCode` resolving each
  known code to its `data object` and throwing `DomainValidationException`
  for an unknown code.
- `ExpenseServiceTest.kt` — MockK unit test mocking `ExpenseRepository`
  directly (no Spring context, same style as `HealthCheckServiceTest`):
  default-to-today on `record`, propagation of `DomainValidationException`
  for bad amount/category, `NotFoundException` on missing id for edit/delete.
- `ExpenseQueryIntegrationTest.kt` — Testcontainers + `DgsQueryExecutor`
  (same pattern as `HealthQueryIntegrationTest`): arbitrary non-calendar-month
  period retrieval, single-day retrieval (`from == to`) proving "a single
  date is just a period," and `categories` returning `code`/`label` pairs for
  the full fixed set.
- `ExpenseMutationIntegrationTest.kt` — record/edit/delete round-trips
  through GraphQL (mutations pass `categoryCode`, responses assert the
  nested `category { code label }`); a dedicated test recording an expense
  with a **future** `expenseDate` and asserting success (explicit acceptance
  criterion).
- `ExpenseValidationIntegrationTest.kt` — proves invalid input (zero/negative
  amount, unknown `categoryCode`, `from > to`, missing id on edit/delete)
  comes back as a populated `errors[]` array with HTTP 200, never a crash.

## Frontend — `frontend/src/features/expense/`

Follow `health`'s pattern exactly: hand-written `gql` + colocated TS result
types in `api/queries.ts` (no codegen), functional components using
`useQuery`/`useMutation` directly, tests colocated as `Component.test.tsx`
using `MockedProvider`. No styling library or CSS exists anywhere in the
frontend today — stay with plain unstyled semantic JSX, matching
`HealthStatus.tsx`.

- `api/queries.ts` — `GET_EXPENSES` (variables `from`/`to`), `GET_CATEGORIES`,
  `RECORD_EXPENSE`, `EDIT_EXPENSE`, `DELETE_EXPENSE`, each with a
  hand-written matching TS interface, plus shared `Category`
  (`{ code: string; label: string }`) and `Expense`
  (`{ id: string; amount: number; currency: string; expenseDate: string; category: Category; note: string | null }`)
  interfaces. `RecordExpenseInput`/`EditExpenseInput` TS types carry
  `categoryCode: string`, matching the schema's input shape.
- `components/ExpenseForm.tsx` — single controlled form for both record and
  edit (`initialExpense?: Expense` prop switches mode). Concretely satisfies
  "≤3 fields + 1 confirm, date prefilled, note optional with no extra step":
  amount (`<input type="number" step="0.01" min="0.01" required>`), category
  (`<select required>` populated from `GET_CATEGORIES`, each `<option value={category.code}>{category.label}</option>`),
  date (`<input type="date">` defaulted to today's ISO date), note
  (`<textarea>`, not required, omitted/sent as `null` when empty) — all on
  one screen, one submit button; submits `categoryCode` from the selected
  option's value.
- `components/PeriodFilter.tsx` — two `<input type="date">` (`from`/`to`),
  both defaulting to today; purely controlled, lifts `onChange(from, to)` to
  the parent.
- `components/ExpenseList.tsx` — presentational table of `expenses` prop +
  `onEdit(expense)` callback, rendering `expense.category.label`; owns its
  own `useMutation(DELETE_EXPENSE)` per row (components call their feature's
  own `api/` module directly, per CLAUDE.md), evicting the deleted row from
  the Apollo cache on success.
- `components/ExpensePage.tsx` — top-level page: owns `from`/`to` state
  (defaulted to today) and `editingExpense` state, runs
  `useQuery(GET_EXPENSES, { variables: { from, to } })`, composes
  `PeriodFilter` + `ExpenseForm` + `ExpenseList`. No business logic here —
  purely data wiring and state, per CLAUDE.md.

### Routing — `frontend/src/app/App.tsx`

Only this file changes. Add `ExpensePage` import; replace the single
`<Route path="/" element={<HealthStatus />} />` with:

```tsx
<Route path="/" element={<ExpensePage />} />
<Route path="/health" element={<HealthStatus />} />
```

Nothing else in `app/` changes.

### Frontend tests

- `ExpenseList.test.tsx` — renders with a fixed `expenses` prop, asserts
  fields appear.
- `ExpenseForm.test.tsx` — `MockedProvider` mocking `GET_CATEGORIES`
  (returning `code`/`label` pairs) and `RECORD_EXPENSE` with exact expected
  variables (today's date, chosen category's `code` as `categoryCode`, note
  omitted); fills amount + category only, submits, asserts
  the mutation fired with the right variables (proves the 3-fields-plus-one-
  confirm, date-prefilled, note-optional acceptance criteria).
- `ExpensePage.test.tsx` — `MockedProvider` mocking `GET_EXPENSES` +
  `GET_CATEGORIES`, asserts list renders from mocked data.

## Sequencing

1. Flyway migration `V2__create_expenses_table.sql`.
2. Backend `domain/` + `common/error/`.
3. Domain unit tests.
4. Backend `infrastructure/` — verify app boots (`ddl-auto: validate` passes).
5. Backend `application/` + `ExpenseServiceTest`.
6. Add `graphql-dgs-extended-scalars` to `build.gradle.kts`.
7. `schema/expense.graphqls`.
8. Backend `api/`.
9. Backend integration tests.
10. `make be-test` + ktlint clean.
11. Frontend `api/queries.ts`.
12. Frontend components (`ExpenseForm`, `PeriodFilter`, `ExpenseList`, then `ExpensePage`).
13. `app/App.tsx` routing change.
14. Frontend component tests.
15. Frontend `test`, `lint`, `typecheck`/`build` clean.

## Verification

- `make be-test` (or `cd backend && ./gradlew ktlintCheck test`) — all new
  unit + Testcontainers integration tests pass, confirming every invariant
  (I-1–I-4) and both acceptance-criteria edge cases (future date, arbitrary
  period).
- `cd frontend && npm run lint && npm run typecheck && npm test` — new
  component tests pass.
- `make up` — start the full stack, open `http://localhost:5173/`, record an
  expense (amount + category only, date prefilled, no note), confirm it
  appears in the list for today; edit it; delete it; change the period
  filter to a past arbitrary range and confirm retrieval still works;
  navigate to `http://localhost:5173/health` and confirm the existing health
  check still renders `Backend status: UP`.
