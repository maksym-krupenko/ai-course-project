# 001 — Expense logging & retrieval

- **Status:** Specified — not yet planned or implemented
- **Date:** 2026-08-09
- **Product owner:** repository owner (single stakeholder)
- **Bounded context:** [Expense Tracking](../domain/expense-tracking.md)
- **Supersedes:** the "no business features" no-goal in `SPEC.md` — this is
  the first business capability

## Progress

- [x] Stage 1 — Product evaluation & assessment (discovery interview)
- [x] Stage 2 — Bounded context & ubiquitous language defined
- [x] Stage 3 — Specification written
- [x] Stage 4 — Sections reviewed and approved by the product owner
- [ ] Open questions resolved (see § Open questions)
- [ ] ADRs written for the decisions that warrant one (see § ADR candidates)
- [ ] Implementation planned
- [ ] Implemented

## The problem

The owner already tracks personal finances with existing tools. The problem
is **not** an absence of tooling and **not** an inability to see spending.

> "Existing solutions I use are not tailored to my preferences in recording
> and analysis."

The pain is **fit**. Every existing product imposes its own model of how
spending should be entered and how it should be examined. The owner's model
differs, and the gap shows up in two places:

**Recording.** Existing tools are slow — too many fields for what is a
several-times-a-day act — *and* they assume one purchase equals one
category, which is routinely false. A single supermarket payment is
genuinely two or three different kinds of spending.

**Analysis.** Existing tools lock analysis to calendar months, to their own
dimensions, offer no sense of whether a number is normal for this person,
and don't allow ad-hoc questions.

## Who it is for

One user: the repository owner. `[Decided]`

This is a real constraint, not a placeholder. It means: no multi-tenancy, no
sharing, no per-user data partitioning, and no pressure to resolve the
deferred auth decision in order to ship v1.

## Findings from discovery

Conclusions drawn from the interview, distinct from the raw answers logged
below.

**F-1 — The product is the fit, not the feature list.** Nothing here is
novel as a feature; the value is entirely in it matching one person's
preferences. Any decision that trades personal fit for generality is
backwards for this product.

**F-2 — Speed wins outright in v1; splitting must not tax it when it
returns.** The two recording pains pull in opposite directions: "one expense
≠ one purchase" argues for line items, "too slow / too many fields" argues
for three fields and one button. v1 resolves this by deferring splitting
entirely, so nothing competes with speed. When splitting does return, it
must be **optional and invisible until invoked** — a design where every
entry pays for the split capability has failed even if it is correct.

**F-3 — Analysis is the differentiator, and it does not ship in v1.** All
four analysis pains were confirmed, but v1 is recording plus date retrieval.
The consequence: **v1's data model must be shaped by analysis needs even
though no analysis ships.** Concretely — arbitrary periods rather than
calendar months, dates rather than timestamps, and category as a real
reference rather than free text.

**F-4 — Categories are not a pain point.** The owner did not select "their
categories aren't mine". A shipped, predefined, flat category set is
therefore sufficient, and user-managed taxonomies are deferred with a clear
conscience.

**F-5 — Ingestion is a near-future certainty, not a maybe.** CSV import was
named for v1-ish scope and further sources ("more options") beyond it. It is
scoped out of this spec, and sequenced after Analysis in the roadmap below
because Analysis is the differentiator while ingestion only changes how
expenses arrive — but the expense model must be able to accept
externally-sourced expenses without redefinition — and under
`docs/adr/002-ports-only-where-they-earn-it.md`, a bank-data provider is
precisely the volatile external dependency that will earn a domain-owned
port when it arrives.

## Scope — v1

### In scope `[Decided]`

- [ ] Record an expense: amount, expense date, category, and an optional note
- [ ] Edit an expense
- [ ] Delete an expense
- [ ] Retrieve expenses for a specific date
- [ ] Retrieve expenses for an arbitrary date period (inclusive `[from, to]`)
- [ ] A predefined, flat set of spending categories, available for selection

### Explicit no-goals for v1 `[Decided]`

Each was actively considered and excluded — none is an oversight. This table
is the single place the *why* is recorded; the context doc routes each
excluded term to the context that will eventually own it.

| Excluded | Why |
|---|---|
| Splitting one expense across categories | Deferred for simplicity after being specified — see the Discovery Log reversal. One expense carries exactly one category. This leaves a named recording pain unaddressed in v1. |
| Accounts, balances, net worth | Owner chose standalone expenses. No balance means no reconciliation, no opening balances, no correction entries — a materially smaller domain. |
| Income, transfers | v1 records spending only. |
| Budgets, limits, forecasting | No plan to spend against yet. |
| Aggregation and reporting (even "total by category") | Confirmed in review. Retrieval returns expenses, not sums — all aggregation belongs to the Analysis & Insights context. |
| Flexible slicing, baselines, natural-language questions | The differentiator, deliberately deferred to its own context so it can be designed properly rather than bolted on. |
| CSV import, bank sync | Next capability, not this one. |
| Tags, merchant, payment method | Owner: "Category and date are sufficient for now." |
| User-created categories | See F-4. |
| Multi-currency | Single system currency in v1 — see [`Money`](../domain/expense-tracking.md#money) for how the model stays open to it. |
| Authentication | Single user; `SPEC.md` no-goal stands. Must not be bolted on ad hoc — see `CLAUDE.md`. |

### Acceptance criteria

v1 is done when all of these hold:

- [ ] An expense can be recorded with amount, date and category, and nothing
      else is required of the user
- [ ] The date defaults to today, so a same-day expense needs no date input
- [ ] Recording a same-day expense takes at most three fields — of which the
      date is prefilled — and one confirmation; the note is optional and
      adds no step when skipped (finding F-2)
- [ ] An expense can be dated in the future, and the system does not object
- [ ] Expenses for a given date can be retrieved
- [ ] Expenses for an arbitrary `[from, to]` period can be retrieved,
      including periods that are not calendar months (finding F-3)
- [ ] Every invariant in the [context doc](../domain/expense-tracking.md)
      has a test proving it

## Roadmap beyond v1

Order is a current belief, not a commitment.

1. **v2 — Analysis & Insights.** The differentiator: arbitrary periods,
   aggregation, slicing. Ships as its own bounded context consuming Expense
   Tracking read-only.
2. **v3 — Ingestion.** CSV import first, additional sources later.
3. **v4 — Splitting.** One expense across several categories, deferred from
   v1. Import likely forces this sooner rather than later: a single
   supermarket row is exactly the case splitting exists for.
4. **Later, unordered.** Income and accounts; budgeting; user-managed
   categories, tags and further dimensions; natural-language questions over
   the data; multi-currency.

Each of these repeats the four-stage process from scratch — including a
fresh discovery interview. The answers above are answers about *v1*, and
several were explicitly "not now" rather than "not ever".

## Open questions

- [ ] `[Open]` Does the `health` slice stay as a wiring reference once a
      real feature exists?
- [ ] `[Open]` Modelling questions listed in the
      [context doc](../domain/expense-tracking.md#open-modelling-questions).

## ADR candidates

Decisions expensive enough to reverse that they warrant `docs/adr/` entries
before implementation:

- [ ] The GraphQL scalar and Postgres column type for an **amount** —
      decimal representation is decided, how it crosses the wire and how it
      is stored is not
- [ ] Whether the predefined category set is seeded by Flyway migration,
      application config, or a Kotlin enum — each has very different
      consequences for ever making categories user-managed

---

## Discovery log

Append-only record of the interview that produced this spec. Answers are as
given. Never edit a past entry; add a new dated one if an answer changes.

### Round 1 — 2026-08-09: problem framing

**Q1. Whose problem does the first version solve?**
**A:** Just me. *(single user)*

**Q2. What is the single central pain the first version must kill?**
**A:** *(free-form)* "Existing solutions I use are not tailored to my
preferences in recording and analysis."

**Q3. Where does the money data come from in v1?**
**A:** *(free-form)* "We will start from Manual + CSV import but in the
future I am planning to have more options."

**Q4. What is the thinnest version that would actually be useful — the
primary job-to-be-done?**
**A:** *(free-form)* "Let's start from logging expenses only with a set of
predefined categories."

### Round 2 — 2026-08-09: making "my preferences" concrete

**Q5. What specifically is wrong with how existing apps make you RECORD an
expense?** *(multi-select)*
**A:** "One expense ≠ one purchase" **and** "Too slow / too many fields".

**Q6. What specifically is wrong with how existing apps let you ANALYZE?**
*(multi-select)*
**A:** All four — rigid time periods; can't slice by my own dimensions; no
"is this normal for me?"; can't ask my own questions.

**Q7. In v1, does logging an expense affect an account balance, or is an
expense standalone?**
**A:** Standalone expenses only. No accounts, no balances.

**Q8. Does v1 need more than one currency?**
**A:** Single now, multi later — model `Money` as amount + currency from day
one so multi-currency isn't a rewrite.

### Round 3 — 2026-08-09: resolving the model

**Q9. How should a multi-part purchase work?**
**A:** Optional split lines. One expense with a total, optionally broken
into lines each with its own amount and category; lines must sum to the
total. The fast path stays one screen.

**Q10. Besides category and date, which dimensions do you want to slice
by?**
**A:** *(free-form)* "Category and date are sufficient for now."

**Q11. What does "can't ask my own questions" mean mechanically?**
**A:** *(free-form)* "Let's postpone it for the future."

**Q12. Does v1 ship with any analysis at all?**
**A:** *(free-form)* "Record + fetch by specific date and date period."

### Round 4 — 2026-08-09: revisions during section review

Recorded during the section-by-section review of this spec. **Q13 and Q15
reverse earlier answers**; the earlier entries stand as originally recorded.

**Q13. (reverses Q9)** Should an expense support optional split lines?
**A:** No — "ok for simplicity for now let's leave only one category."
Splitting is deferred to a later version, not rejected. Consequence: the
"one expense ≠ one purchase" pain from Q5 is unaddressed in v1.

**Q14. Should the "Expense Line" term exist in the glossary?**
**A:** No — deleted along with splitting.

**Q15. (reverses proposed invariant I-8)** Must an expense date be in the
past? **A:** No — "could be in the future if the person for example pays
for tickets to the trip of the next month."

**Q16. If dates may be in the future, what does the date mean?**
**A:** The date the expense is attributed to, defaulting to the payment
date. Consequence: "Occurrence Date" renamed to "Expense Date"; the
constraint is dropped entirely.

**Q17. Which currency does v1 run in?**
**A:** PLN — złoty. Minor unit grosz, two decimal places.

**Q18. Should v1 retrieval include per-category totals for the period?**
**A:** No — expenses only. All aggregation belongs to Analysis & Insights.

**Q19. Should an expense carry an optional free-text note?**
**A:** Yes — one optional field, never required.

### Round 5 — 2026-08-09: amount representation

**Q20. Are amounts modelled as an integer count of the currency's minor unit,
or as decimal numbers?**
**A:** Decimal numbers with digits after the comma (`19.20`) — not minor
units. Consequence: the modelling question in the context doc is narrowed to
the GraphQL scalar and the column type; Q17's "minor unit grosz" phrasing
stands as recorded but is no longer how the amount is represented.
