# Bounded context: Income Tracking

- **Status:** Defined — not implemented
- **Date defined:** 2026-08-09
- **Product spec:** [`docs/product/002-income-logging.md`](../product/002-income-logging.md)
- **Implementing feature:** `features/income` (backend), `features/income`
  (frontend) — not yet created

## Purpose

To be the **faithful record of money the user received** — nothing more.

This context answers exactly two questions:

1. What did I receive, from what, and when?
2. What did I receive during this date or this date range?

It deliberately answers none of these: how much do I have, does this affect
a balance, is this normal, what is my net cashflow. Those belong to other
contexts (see the [context map](README.md)).

## Ubiquitous language

### Income

A single act of the user receiving money, as recorded by the user.

- Has an **amount**, an **income date**, exactly one **source**, and an
  optional **note**.
- Is **immutable in meaning, mutable in record**: correcting a typo edits
  the income; it does not create a compensating entry. There is no
  double-entry bookkeeping in this context.
- Is **standalone**: it does not credit anything. Recording an income
  changes no balance anywhere, because no balance exists.

> **Trap:** "Income" is not "Deposit" or "Transaction". If a concept needs a
> counterparty or a balance effect, it is not an Income and does not belong
> in this context.

### Source

A named classification of income, drawn from a **predefined set** the
system ships with: Salary, Freelance / Side income, Gift, Refund,
Investment return, Other.

- Flat — no parents, no children, no hierarchy.
- Not user-created in v1. This is a scope decision, not a principle,
  mirroring Expense Tracking's Category (see F-3 in the spec).

> **Trap:** A source classifies *income*, not *payers*. "My employer's
> name" is never a source.

### Note

Optional free text the user attaches to an income, recording what the
source alone cannot say.

- Never required. Recording an income without one costs nothing and adds no
  step.
- Is for the user's own recall. Nothing parses, indexes, or derives meaning
  from it.

> **Trap:** A note is not a substitute for a dimension. If something is
> worth filtering or grouping by, it belongs as a real field.

### Money

An **amount** paired with a **currency**. Never a bare number.

- Amounts are greater than zero and carry no more decimal places than the
  currency allows.
- An amount is a **decimal number**, not an integer count of the currency's
  minor unit.
- v1 operates in a **single system currency, PLN**, with two decimal
  places, but the currency is carried in the model from day one.
- This is the same concept as [Expense Tracking's `Money`](expense-tracking.md#money).
  Whether it becomes one shared value object or stays duplicated per
  context until it diverges is an open question below.

> **Trap:** An income amount is always positive. Receiving money is not
> modelled as a negative number; the direction is implied by the concept
> "Income".

### Income Date

The date an income is attributed to — the date it counts as received for
the purpose of looking at a period. A date, not a timestamp, no time zone.

- Defaults to the date the money was received, which is the common case.
- May be **in the future**. An invoice raised today but due next month can
  be dated to when it's expected, so it shows up where it belongs.
- Distinct from when the record was created, which is metadata and not
  part of the domain language.

> **Trap:** Future-dating is attribution, not forecasting. This context
> does not model money that has not yet been received as a plan or
> projection — only where an already-decided receipt is attributed.

> **Trap:** Never reason about the income date in UTC or convert it. If the
> user says the 3rd, it is the 3rd.

### Period

An inclusive range of income dates, `[from, to]`, used to retrieve income
entries. Identical concept to [Expense Tracking's `Period`](expense-tracking.md#period).

- Arbitrary — a period is **not** a calendar month.
- `from` and `to` may be the same date; retrieval for a single date is just
  a period of one day, not a separate concept.

## Invariants

Rules that must never be false. Each is testable, and each should have a
test that proves it.

| # | Invariant | Status |
|---|---|---|
| I-1 | An income's amount is greater than zero. | `[Decided]` |
| I-2 | An income references exactly one source. | `[Decided]` |
| I-3 | Every source referenced by an income exists in the predefined set. | `[Decided]` |
| I-4 | Every income amount is denominated in the system currency (PLN in v1). | `[Decided]` |

## Language this context deliberately does not have

Naming these keeps them out of the model by accident.

| Absent term | Where it belongs |
|---|---|
| Split, Line item | Income Tracking, if/when reopened |
| Account, Balance | Accounts & Balances |
| Expense, spending Category | Expense Tracking |
| Budget, Limit, Remaining | Budgeting |
| Net, Cashflow | Analysis & Insights |
| Import, Statement, Deduplication | Ingestion, if it turns out to apply to income |
| Payer, Employer, Payment method | Income Tracking, if/when reopened |
| Tag, Label | Income Tracking, if/when reopened |
| User, Owner, Tenant | Undecided — same open question as Expense Tracking |

## Open modelling questions

Resolve before or during implementation; none block the specification.

- [ ] `[Open]` **Amount scalar and column type.** Same open question as
      Expense Tracking's — ideally resolved once, for both contexts
      together, since the representation should be identical.
- [ ] `[Open]` **Shared `Money`/`Period` value objects.** Now that two
      features need an identical concept, should it be promoted to
      `common/` (per `CLAUDE.md`'s cross-feature rule) rather than
      duplicated in each feature's `domain/`? Candidate for its own ADR.
- [ ] `[Open]` **Predefined source set.** Flyway seed, application config,
      or a Kotlin enum — same question as Expense's category set, and
      ideally answered the same way for both.
- [ ] `[Open]` **Deletion.** Hard delete versus soft delete — same open
      question as Expense Tracking.
- [ ] `[Open]` **Does Ingestion apply to income the same way it will for
      expenses?** Not raised in discovery; don't assume symmetry with
      Expense Tracking's CSV-import roadmap item.
