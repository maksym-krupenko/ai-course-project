# Plan 002 — Income Logging & Retrieval (v1) implementation

- **Status:** Planned — not yet implemented
- **Date:** 2026-08-09

## Implements

- [`docs/product/002-income-logging.md`](../product/002-income-logging.md) — the product spec this plan
  delivers (scope, acceptance criteria, discovery log).
- [`docs/domain/income-tracking.md`](../domain/income-tracking.md) — the bounded context and invariants
  (I-1–I-4) this plan's tests must prove.
- [`CLAUDE.md`](../../CLAUDE.md) — the layering rules (`api/application/domain/infrastructure`,
  feature-first frontend) this plan follows.
- [`docs/adr/002-ports-only-where-they-earn-it.md`](../adr/002-ports-only-where-they-earn-it.md) — governs
  the "no domain-owned port for `IncomeRepository`" call below.
- [`docs/plans/001-expense-logging-implementation.md`](001-expense-logging-implementation.md) — the sibling
  plan this one mirrors structurally. Income Tracking is deliberately shaped identically to Expense
  Tracking (see the product spec's F-1): same layering, same invariant-in-constructor style, same test
  shape. Every deviation from that plan is called out explicitly below; everything else should be assumed
  to follow it.

## Context

`docs/product/002-income-logging.md` and `docs/domain/income-tracking.md` specify the second real business
capability for this app: recording and retrieving income. Expense Tracking (`docs/plans/001-...md`) already
implemented the identical shape — record/edit/delete, retrieve by arbitrary period including a single day —
so this plan reuses every structural decision that plan made rather than re-deriving them, deviating only
where the domain doc or product spec explicitly requires it.

This plan resolves the product spec's open questions and ADR candidates the same way the Expense plan
resolved their Expense-side equivalents, for consistency across the two sibling contexts:

1. **`Money` and `Period` stay duplicated in `features/income/domain/`, not promoted to `common/`.**
   The product spec and domain doc both leave this open ("stay duplicated per feature until they diverge").
   Promoting to `common/` now would be a speculative abstraction before a second consumer actually needs to
   share behavior beyond the two value objects' shape — CLAUDE.md's cross-feature rule already allows this
   (features never share domain internals directly; a shared concept belongs in `common/` only when it
   earns it). Revisit if a third context needs the identical concept, or if Expense's and Income's `Money`/
   `Period` are ever found to diverge. `Money` here is `data class Money(val amount: BigDecimal, val
   currency: String = "PLN")` — byte-for-byte the same invariants (I-1, I-4) as Expense Tracking's, just a
   separate type in a separate package, per the no-cross-feature-import rule.
2. **Source is a fixed set of Kotlin objects, exactly mirroring `Category`.** Same reasoning as the Expense
   plan's Category decision: a sealed class of `data object`s (not a DB table, not a GraphQL enum), each
   carrying `code` and `label`, exposed over GraphQL as an object type (`type Source { code: ID! label:
   String! }`). The v1 set (from the product spec's F-3 / domain doc): `SALARY` ("Salary"),
   `FREELANCE_SIDE_INCOME` ("Freelance / Side Income"), `GIFT` ("Gift"), `REFUND` ("Refund"),
   `INVESTMENT_RETURN` ("Investment Return"), `OTHER` ("Other").
3. **Deletion is a hard delete**, mirroring Expense's v1 call (confirmed for Income in the spec's Round 3,
   Q7 — "mirror Expense exactly... deletion left open" resolves to hard delete, matching what Expense
   actually shipped).
4. **Amount scalar/column type mirrors Expense exactly**: `BigDecimal` GraphQL scalar (already registered
   in the app via `graphql-dgs-extended-scalars`, added in the Expense plan — no new dependency needed
   here), `NUMERIC(12, 2)` Postgres column. This resolves the product spec's shared ADR candidate for both
   contexts without a separate ADR, since it's a direct reuse of an already-made decision, not a new one.
5. **Frontend gets a second route, `/income`, plus a minimal nav.** The Expense plan made `/` render
   `ExpensePage` and moved `health` to `/health`; it added no navigation UI because only one real feature
   existed. With two real features now, `App.tsx` gains a small `<nav>` (links to "Expenses" `/` and
   "Income" `/income`) so the income page is reachable without hand-typing the URL — the smallest change
   that keeps both pages navigable. `/health` stays as-is and is intentionally left off the nav (it's a
   wiring diagnostic, not a product feature).

Every invariant in `docs/domain/income-tracking.md` (I-1 amount > 0, I-2 exactly one source, I-3 source ∈
predefined set, I-4 currency = system currency) must have a passing test, per the spec's acceptance
criteria — same structure as Expense's I-1–I-4.

## Backend — `backend/src/main/kotlin/com/financeapp/features/income/`

Follow the `expense` slice's four-layer split (`domain → infrastructure ← application → api`) file-for-file
— it is now the in-repo template, not `health` (per this plan's Context section). No domain-owned port for
`IncomeRepository`, same ADR 002 reasoning as Expense. Invariants live in `domain` constructors (`init`
blocks throwing `DomainValidationException`). Reuses the existing `common/error/` package as-is — no
changes there.

### Files

- `domain/Money.kt` — `data class Money(val amount: BigDecimal, val currency: String = "PLN")`; `init`
  enforces I-1 (`amount > ZERO`) and I-4 (`currency == "PLN"`), throwing `DomainValidationException`.
  Identical in content to `features/expense/domain/Money.kt`, duplicated per the Context section's decision
  1.
- `domain/Source.kt` — `sealed class Source(val code: String, val label: String)` with each source a `data
  object` (`data object Salary : Source("SALARY", "Salary")`, `data object FreelanceSideIncome :
  Source("FREELANCE_SIDE_INCOME", "Freelance / Side Income")`, `data object Gift : Source("GIFT", "Gift")`,
  `data object Refund : Source("REFUND", "Refund")`, `data object InvestmentReturn :
  Source("INVESTMENT_RETURN", "Investment Return")`, `data object Other : Source("OTHER", "Other")`), plus
  `companion object { val all: List<Source> = listOf(Salary, FreelanceSideIncome, Gift, Refund,
  InvestmentReturn, Other); fun fromCode(code: String): Source = all.find { it.code == code } ?: throw
  DomainValidationException(...) }` (enforces I-3). Same pattern as `Category.kt`.
- `domain/Period.kt` — `data class Period(val from: LocalDate, val to: LocalDate)`; `init` throws if
  `from.isAfter(to)`. Identical in content to Expense's, duplicated per decision 1.
- `domain/Income.kt` — `data class Income(val id: Long? = null, val amount: Money, val incomeDate:
  LocalDate, val source: Source, val note: String?)`. I-2 (exactly one source) is structural, same as
  Expense's I-2.
- `infrastructure/IncomeEntity.kt` — `@Entity @Table(name = "incomes")`, fields `amount: BigDecimal,
  currency: String, incomeDate: LocalDate, source: String, note: String?`, mirroring `ExpenseEntity`'s
  shape with `expenseDate`/`category` renamed to `incomeDate`/`source`.
- `infrastructure/IncomeRepository.kt` — `interface IncomeRepository : JpaRepository<IncomeEntity, Long>`
  with `findByIncomeDateBetweenOrderByIncomeDateDescIdDesc(from, to)`.
- `infrastructure/IncomeEntityMapper.kt` — `Income.toEntity()` / `IncomeEntity.toDomain()` extension
  functions, the one place the DB's `source` string column and the `Source` object set meet
  (`source.code` / `Source.fromCode(source)`), mirroring `ExpenseEntityMapper.kt`.
- `application/IncomeService.kt` — `@Service` with `record(amount, incomeDate?, sourceCode, note)`
  (defaults `incomeDate` to `LocalDate.now()` when null), `edit(id, amount, incomeDate, sourceCode, note)`
  (full-replace, throws `NotFoundException` if missing), `delete(id)` (throws `NotFoundException` if
  missing, otherwise hard delete per decision 3), `findByPeriod(from, to)` (constructs `Period`),
  `listSources() = Source.all`. Identical method shape to `ExpenseService`, `category`→`source`,
  `expenseDate`→`incomeDate`.
- `api/IncomeDataFetcher.kt` — `@DgsComponent` with `@DgsQuery fun incomes(from, to)`, `@DgsQuery fun
  sources()`, `@DgsMutation fun recordIncome(input)`, `@DgsMutation fun editIncome(id, input)`,
  `@DgsMutation fun deleteIncome(id): ID`. Owns `SourcePayload(code: String, label: String)` and
  `IncomePayload(id, amount, currency, incomeDate, source: SourcePayload, note)` DTOs plus
  `RecordIncomeInput`/`EditIncomeInput` (`sourceCode: String`, not `Source`) in the same file, per the
  `ExpenseDataFetcher`/`CategoryPayload`/`ExpensePayload` pattern — no import of `...domain.Income` or
  `...domain.Source` anywhere in `api`.

### Schema — `backend/src/main/resources/schema/income.graphqls`

```graphql
type Source {
    code: ID!
    label: String!
}

type Income {
    id: ID!
    amount: BigDecimal!
    currency: String!
    incomeDate: LocalDate!
    source: Source!
    note: String
}

input RecordIncomeInput {
    amount: BigDecimal!
    incomeDate: LocalDate
    sourceCode: ID!
    note: String
}

input EditIncomeInput {
    amount: BigDecimal!
    incomeDate: LocalDate!
    sourceCode: ID!
    note: String
}

extend type Query {
    incomes(from: LocalDate!, to: LocalDate!): [Income!]!
    sources: [Source!]!
}

extend type Mutation {
    recordIncome(input: RecordIncomeInput!): Income!
    editIncome(id: ID!, input: EditIncomeInput!): Income!
    deleteIncome(id: ID!): ID!
}
```

No `scalar BigDecimal` / `scalar LocalDate` declarations here — `expense.graphqls` already declares both
at the schema-wide level (DGS merges all `.graphqls` files under `schema/` into one document), so
redeclaring would be a duplicate-type error. For the same reason, `extend type Mutation` is used here
(`expense.graphqls` already declares `type Mutation` as the base); `extend type Query` for the same reason
as Expense's.

No new dependency: `graphql-dgs-extended-scalars` is already in `backend/build.gradle.kts` from the Expense
plan, and `common/config/GraphQLScalarConfig.kt` already registers the `LocalDate` scalar wiring — nothing
in `common/` changes for this feature.

### Migration — `backend/src/main/resources/db/migration/V3__create_incomes_table.sql`

```sql
CREATE TABLE incomes (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
    income_date DATE NOT NULL,
    source VARCHAR(32) NOT NULL CHECK (
        source IN (
            'SALARY', 'FREELANCE_SIDE_INCOME', 'GIFT', 'REFUND',
            'INVESTMENT_RETURN', 'OTHER'
        )
    ),
    note TEXT
);

CREATE INDEX idx_incomes_income_date ON incomes (income_date);
```

`V3` because `V1__init.sql` and `V2__create_expenses_table.sql` already exist — never renumber or edit
those. The `source` column stores a `Source.code` string; the `CHECK` list is hand-kept in sync with
`Source.kt`, same accepted coupling as Expense's `category` column.

### Backend tests (flat under `features/income/`, matching `expense`'s pattern)

- `MoneyTest.kt`, `SourceTest.kt`, `PeriodTest.kt` — pure unit tests (`kotlin.test`), proving I-1, I-3, I-4,
  and period range validation. `SourceTest.kt` covers `Source.fromCode` resolving each known code to its
  `data object` and throwing `DomainValidationException` for an unknown code — same shape as
  `CategoryTest.kt`.
- `IncomeServiceTest.kt` — MockK unit test mocking `IncomeRepository` directly (no Spring context): default-
  to-today on `record`, propagation of `DomainValidationException` for bad amount/source,
  `NotFoundException` on missing id for edit/delete. Mirrors `ExpenseServiceTest.kt`'s five cases.
- `IncomeQueryIntegrationTest.kt` — Testcontainers + `DgsQueryExecutor`: arbitrary non-calendar-month period
  retrieval, single-day retrieval (`from == to`), and `sources` returning `code`/`label` pairs for the full
  fixed set. Mirrors `ExpenseQueryIntegrationTest.kt`.
- `IncomeMutationIntegrationTest.kt` — record/edit/delete round-trips through GraphQL (`sourceCode` in,
  nested `source { code label }` out); a dedicated test recording an income with a **future** `incomeDate`
  asserting success. Mirrors `ExpenseMutationIntegrationTest.kt`.
- `IncomeValidationIntegrationTest.kt` — invalid input (zero/negative amount, unknown `sourceCode`, `from >
  to`, missing id on edit/delete) comes back as a populated `errors[]` array with HTTP 200. Mirrors
  `ExpenseValidationIntegrationTest.kt`.

## Frontend — `frontend/src/features/income/`

Follow `expense`'s pattern file-for-file: hand-written `gql` + colocated TS result types in
`api/queries.ts` (no codegen), functional components using `useQuery`/`useMutation` directly, tests
colocated as `Component.test.tsx` using `MockedProvider`, plain unstyled semantic JSX.

- `api/queries.ts` — `GET_INCOMES` (variables `from`/`to`), `GET_SOURCES`, `RECORD_INCOME`, `EDIT_INCOME`,
  `DELETE_INCOME`, each with a hand-written matching TS interface, plus shared `Source` (`{ code: string;
  label: string }`) and `Income` (`{ id: string; amount: number; currency: string; incomeDate: string;
  source: Source; note: string | null }`) interfaces. `RecordIncomeInput`/`EditIncomeInput` carry
  `sourceCode: string`. Identical structure to `expense/api/queries.ts` with `expense`→`income`,
  `category`→`source`, `expenseDate`→`incomeDate` renames throughout.
- `components/IncomeForm.tsx` — single controlled form for both record and edit (`initialIncome?: Income`
  prop switches mode), same field set as `ExpenseForm`: amount (`<input type="number" step="0.01"
  min="0.01" required>`), source (`<select required>` populated from `GET_SOURCES`, each `<option
  value={source.code}>{source.label}</option>`), date (`<input type="date">` defaulted to today's ISO
  date), note (`<textarea>`, optional, sent as `null` when empty) — one screen, one submit button.
  Mirrors `ExpenseForm.tsx` structurally, including its `refetchQueries: ["GetIncomes"]` on both mutations.
- `components/PeriodFilter.tsx` — two `<input type="date">` (`from`/`to`), both defaulting to today,
  purely controlled, lifts `onChange(from, to)` to the parent. Byte-for-byte the same component as
  `expense/components/PeriodFilter.tsx` (it has no feature-specific logic — it only manipulates two date
  strings) but duplicated into `features/income/components/`, not imported from `features/expense/`, per
  CLAUDE.md's cross-feature rule (features never import each other; a shared concept only moves to
  `shared/` when it earns it — one small presentational component used by exactly two features doesn't yet
  justify that move).
- `components/IncomeList.tsx` — presentational table of `incomes` prop + `onEdit(income)` callback,
  rendering `income.source.label`; owns its own `useMutation(DELETE_INCOME)` per row, evicting the deleted
  row from the Apollo cache (`__typename: "Income"`) on success. Mirrors `ExpenseList.tsx`.
- `components/IncomePage.tsx` — top-level page: owns `from`/`to` state (defaulted to today) and
  `editingIncome` state, runs `useQuery(GET_INCOMES, { variables: { from, to } })`, composes
  `PeriodFilter` + `IncomeForm` + `IncomeList`. Mirrors `ExpensePage.tsx`.

### Routing & navigation — `frontend/src/app/App.tsx`

Only this file changes. Add `IncomePage` import and a new route; add a minimal `<nav>` (per this plan's
Context section, decision 5):

```tsx
<GraphQLProvider>
  <BrowserRouter>
    <nav>
      <Link to="/">Expenses</Link>
      <Link to="/income">Income</Link>
    </nav>
    <Routes>
      <Route path="/" element={<ExpensePage />} />
      <Route path="/income" element={<IncomePage />} />
      <Route path="/health" element={<HealthStatus />} />
    </Routes>
  </BrowserRouter>
</GraphQLProvider>
```

`Link` imported from `react-router-dom` alongside the existing `BrowserRouter`/`Route`/`Routes` import.
Nothing else in `app/` changes.

### Frontend tests

- `IncomeList.test.tsx` — renders with a fixed `incomes` prop, asserts fields appear. Mirrors
  `ExpenseList.test.tsx`.
- `IncomeForm.test.tsx` — `MockedProvider` mocking `GET_SOURCES` (returning `code`/`label` pairs) and
  `RECORD_INCOME` with exact expected variables (today's date, chosen source's `code` as `sourceCode`, note
  omitted); fills amount + source only, submits, asserts the mutation fired with the right variables.
  Mirrors `ExpenseForm.test.tsx`.
- `IncomePage.test.tsx` — `MockedProvider` mocking `GET_INCOMES` + `GET_SOURCES`, asserts list renders from
  mocked data. Mirrors `ExpensePage.test.tsx`.

## Sequencing

1. Flyway migration `V3__create_incomes_table.sql`.
2. Backend `domain/`.
3. Domain unit tests (`MoneyTest`, `SourceTest`, `PeriodTest`).
4. Backend `infrastructure/` — verify app boots (`ddl-auto: validate` passes).
5. Backend `application/` + `IncomeServiceTest`.
6. `schema/income.graphqls`.
7. Backend `api/`.
8. Backend integration tests (`IncomeQueryIntegrationTest`, `IncomeMutationIntegrationTest`,
   `IncomeValidationIntegrationTest`).
9. `make be-test` + ktlint clean.
10. Frontend `api/queries.ts`.
11. Frontend components (`IncomeForm`, `PeriodFilter`, `IncomeList`, then `IncomePage`).
12. `app/App.tsx` routing + nav change.
13. Frontend component tests.
14. Frontend `test`, `lint`, `typecheck`/`build` clean.

## Verification

- `make be-test` (or `cd backend && ./gradlew ktlintCheck test`) — all new unit + Testcontainers
  integration tests pass, confirming every invariant (I-1–I-4) and both acceptance-criteria edge cases
  (future date, arbitrary period).
- `cd frontend && npm run lint && npm run typecheck && npm test` — new component tests pass.
- `make up` — start the full stack, open `http://localhost:5173/`, use the new nav to go to
  `http://localhost:5173/income`, record an income (amount + source only, date prefilled, no note),
  confirm it appears in the list for today; edit it; delete it; change the period filter to a past
  arbitrary range and confirm retrieval still works; navigate back to `/` and confirm Expense Tracking is
  unaffected; navigate to `/health` and confirm the existing health check still renders `Backend status:
  UP`.
