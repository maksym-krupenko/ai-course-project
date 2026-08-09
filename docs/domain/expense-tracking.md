# Bounded context: Expense Tracking

- **Status:** Defined — not implemented; the first business context in the
  product
- **Date defined:** 2026-08-09
- **Product spec:** [`docs/product/001-expense-logging.md`](../product/001-expense-logging.md)
- **Implementing feature:** `features/expense` (backend), `features/expense`
  (frontend) — not yet created

## Purpose

To be the **faithful record of money the user spent** — nothing more.

This context answers exactly two questions:

1. What did I spend, on what, and when?
2. What did I spend during this date or this date range?

It deliberately answers none of these: how much do I have, how much may I
spend, is this normal, where did the data come from. Those belong to other
contexts (see the [context map](README.md)).

## Ubiquitous language

### Expense

A single act of the user spending money, as recorded by the user.

- Has an **amount**, an **expense date**, exactly one **category**, and an
  optional **note**.
- Is **immutable in meaning, mutable in record**: correcting a typo edits
  the expense; it does not create a compensating entry. There is no
  double-entry bookkeeping in this context.
- Is **standalone**: it does not debit anything. Recording an expense
  changes no balance anywhere, because no balance exists.

> **Trap:** "Expense" is not "Transaction". If a concept needs a counterparty
> or a balance effect, it is not an Expense and does not belong in this
> context.

### Category

A named classification of spending, drawn from a **predefined set** the
system ships with.

- Flat — no parents, no children, no hierarchy.
- Not user-created in v1. This is a scope decision, not a principle: the
  product owner named categories as *not* a source of pain, so user-managed
  taxonomies are deferred rather than rejected.

> **Trap:** A category classifies *spending*, not *merchants* and not
> *accounts*. "Monobank" is never a category.

### Note

Optional free text the user attaches to an expense, recording what the
category alone cannot say.

- Never required. Recording an expense without one costs nothing and adds
  no step.
- Is for the user's own recall. Nothing parses, indexes, or derives meaning
  from it.

> **Trap:** A note is not a substitute for a dimension. If something is
> worth filtering or grouping by, it belongs as a real field — never as a
> convention inside note text ("#trip", "groceries+household").

### Money

An **amount** paired with a **currency**. Never a bare number.

- Amounts are greater than zero and carry no more decimal places than the
  currency allows.
- An amount is a **decimal number** — `19.20`, not an integer count of the
  currency's minor unit.
- v1 operates in a **single system currency, PLN**, with two decimal places,
  but the currency is carried in the model from day one so that
  multi-currency is an extension rather than a rewrite.

> **Trap:** An expense amount is always positive. Spending is not modelled
> as a negative number; the direction is implied by the concept "Expense".

### Expense Date

The date an expense is attributed to — the date it counts as spending for
the purpose of looking at a period. A date, not a timestamp, no time zone.

- Defaults to the date the money was paid, which is the common case.
- May be **in the future**. Tickets bought today for next month's trip can
  be dated to the trip, so the spending shows up where it belongs.
- Distinct from when the record was created, which is metadata and not part
  of the domain language.

> **Trap:** Future-dating is attribution, not planning. The money has
> already been spent; the user is choosing which period it belongs to.
> Recording money that has *not* been spent is out of scope for this
> context.

> **Trap:** Never reason about the expense date in UTC or convert it. If the
> user says the 3rd, it is the 3rd.

### Period

An inclusive range of expense dates, `[from, to]`, used to retrieve
expenses.

- Arbitrary — a period is **not** a calendar month. The product owner
  explicitly named rigid calendar periods as a pain.
- `from` and `to` may be the same date; retrieval for a single date is just
  a period of one day, not a separate concept.

## Invariants

Rules that must never be false. Each is testable, and each should have a
test that proves it.

| # | Invariant | Status |
|---|---|---|
| I-1 | An expense's amount is greater than zero. | `[Decided]` |
| I-2 | An expense references exactly one category. | `[Decided]` |
| I-3 | Every category referenced by an expense exists in the predefined set. | `[Decided]` |
| I-4 | Every expense amount is denominated in the system currency (PLN in v1). | `[Decided]` |

## Language this context deliberately does not have

Naming these keeps them out of the model by accident. This table routes each
term to its owner; *why* each is excluded from v1 is the spec's no-goals
table, which is not repeated here.

| Absent term | Where it belongs |
|---|---|
| Split, Line, Line item | Expense Tracking, in a later version |
| Account, Balance | Accounts & Balances |
| Income, Transfer | Accounts & Balances |
| Budget, Limit, Remaining | Budgeting |
| Merchant, Payee, Payment method | Expense Tracking, if/when reopened |
| Tag, Label | Expense Tracking, if/when reopened |
| Import, Statement, Deduplication | Ingestion |
| Report, Trend, Baseline, Insight | Analysis & Insights |
| User, Owner, Tenant | Undecided — needs its own ADR |

## Open modelling questions

Resolve before or during implementation; none block the specification.

- [ ] `[Open]` **Amount scalar and column type.** Decimal representation is
      `[Decided]` (see [Money](#money)); still open is which GraphQL scalar
      and which Postgres column type carry it. Affects every arithmetic
      path — expensive to reverse, so it wants an ADR.
- [ ] `[Open]` **System currency configuration.** PLN is decided; open is
      whether it lives in configuration or as a hardcoded constant.
- [ ] `[Open]` **Deletion.** Hard delete versus soft delete. Soft delete
      matters only if something downstream ever needs to know a record
      existed; nothing does yet.
