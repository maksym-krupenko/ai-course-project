# Domain documentation

The domain model of this product: its bounded contexts, the ubiquitous
language inside each, and the invariants that must always hold.

`docs/product/` says *what we're building and why*. This folder says *what
the words mean and what can never be false*. `CLAUDE.md` says *where the code
goes*.

## Context map

One bounded context maps to **one or more features** on each side of the
stack — `backend/src/main/kotlin/com/financeapp/features/` and
`frontend/src/features/`. A feature never reaches into another feature's
internals (see `CLAUDE.md`); by extension, a context never silently shares a
model with another context.

A context's ubiquitous language applies across its whole slice — domain
classes, GraphQL schema types, React components, UI copy. A term that means
something different on the backend than on the screen is a defect here, not
a translation.

Only the first context exists today. The rest are named here so that future
work has a place to land — and, more importantly, so that concepts are *kept
out* of the Expense Tracking context when they belong elsewhere.

| Context | Status | Owns | Deliberately does NOT own |
|---|---|---|---|
| [Expense Tracking](expense-tracking.md) | **Defined — not implemented** | The record of money spent: expenses, the spending categories, retrieval by date and by period | Balances, income, budgets, forecasting, import mechanics, analytics |
| [Income Tracking](income-tracking.md) | **Defined — not implemented** | The record of money received: income entries, the income sources, retrieval by date and by period | Balances, expenses, budgets, net/cashflow, import mechanics, analytics |
| Ingestion | `[Open]` — not started | Getting expense/income data in from outside (CSV files, later bank feeds): parsing, column mapping, deduplication, import runs | What an expense or income *means*; it produces candidates and hands them to the owning context |
| Analysis & Insights | `[Open]` — not started | Aggregation over arbitrary periods, custom slicing, baselines ("is this normal for me?"), net cashflow, natural-language questions | The records themselves, and the Period concept it aggregates over; it is a read-only consumer of Expense Tracking and Income Tracking |
| Accounts & Balances | `[Open]` — not started | Accounts, opening balances, running balances, reconciliation | Nothing in v1 — expenses and income are explicitly standalone |
| Budgeting | `[Open]` — not started | Limits per category per period, remaining-to-spend | The spending record |

**Relationship note:** Analysis & Insights is a *downstream consumer* of
Expense Tracking. This is why the v1 expense model is shaped by analysis
needs (see the spec's "Analysis is the differentiator" finding) even though
no analysis ships in v1. Ingestion is an *upstream supplier*: it must
conform to the Expense Tracking model, not the other way round.

## Rules for changing this folder

- A new context is a significant decision. Record it as an ADR in
  `docs/adr/` as well as here.
- Adding a term to a glossary is cheap; changing what an existing term means
  is expensive — every glossary here is already referenced by a spec. Prefer
  a new, precise term over overloading an existing one.
