# 002 — Income logging & retrieval

- **Status:** Specified — not yet planned or implemented
- **Date:** 2026-08-09
- **Product owner:** repository owner (single stakeholder)
- **Bounded context:** [Income Tracking](../domain/income-tracking.md)

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

The owner has the same fit problem for income that motivated
[Expense Tracking](../domain/expense-tracking.md): existing tools don't
match how they want to record — and eventually analyze — money received.

v1 kills the recording pain by making income entry as fast and
unopinionated as expense entry: no forced account, no forced balance model,
just a fast, faithful record.

## Who it is for

One user: the repository owner. `[Decided]` — same constraint as Expense
Tracking. No multi-tenancy, no sharing, no per-user data partitioning.

## Findings from discovery

**F-1 — Income mirrors Expense in shape, not in the context map's earlier
tentative routing.** `docs/domain/expense-tracking.md` originally routed
"Income" to a not-yet-defined Accounts & Balances context. Discovery
confirmed that's wrong: income is standalone, exactly like Expense —
"capital increasing" describes the *sign* of the record, not a different
domain shape. Income Tracking is a new sibling context to Expense Tracking,
not a feature of Accounts & Balances.

**F-2 — v1's data model stays analysis-ready even though no analysis
ships.** Same reasoning as Expense Tracking's F-3: arbitrary periods rather
than calendar months, dates rather than timestamps, source as a real
reference rather than free text. This is what lets a future net-cashflow
view (income − expenses) exist later without redefining the Income model.

**F-3 — Sources are not a named pain point.** A predefined, flat set
(Salary, Freelance/Side income, Gift, Refund, Investment return, Other) is
sufficient, mirroring Expense Tracking's F-4 for categories.

**F-4 — Ingestion is not assumed symmetric with Expense.** Expense
Tracking's spec named CSV import explicitly as a near-future certainty. No
such signal was given for income in this discovery round. Ingestion for
income is left as an open question, not pre-scoped into the roadmap.

## Scope — v1

### In scope `[Decided]`

- [ ] Record an income: amount, income date, source, and an optional note
- [ ] Edit an income
- [ ] Delete an income
- [ ] Retrieve income entries for a specific date
- [ ] Retrieve income entries for an arbitrary date period (inclusive
      `[from, to]`)
- [ ] A predefined, flat set of income sources, available for selection

### Explicit no-goals for v1 `[Decided]`

| Excluded | Why |
|---|---|
| Splitting one income across sources | Mirrors the Expense v1 decision — one income carries exactly one source. |
| Accounts, balances | Explicitly rejected in discovery — income is standalone and credits nothing. See F-1. |
| Net / income-minus-expense view | Deferred to Analysis & Insights, same as Expense totals. |
| Aggregation and reporting | Retrieval returns income records, not sums — all aggregation belongs to Analysis & Insights. |
| CSV import, bank sync | Not raised for income in this discovery round (contrast with Expense, where it was named explicitly) — see F-4. Revisit only if actually needed. |
| Tags, payer, employer, payment method | Not raised; source and date are sufficient for now, mirroring Expense's decision. |
| User-created sources | See F-3. |
| Multi-currency | Single system currency in v1 — same `Money` model as Expense Tracking. |
| Authentication | Single user; `SPEC.md` no-goal stands. Must not be bolted on ad hoc — see `CLAUDE.md`. |

### Acceptance criteria

v1 is done when all of these hold:

- [ ] An income can be recorded with amount, date and source, and nothing
      else is required of the user
- [ ] The date defaults to today, so a same-day income needs no date input
- [ ] Recording a same-day income takes at most three fields — of which the
      date is prefilled — and one confirmation; the note is optional and
      adds no step when skipped
- [ ] An income can be dated in the future, and the system does not object
- [ ] Income entries for a given date can be retrieved
- [ ] Income entries for an arbitrary `[from, to]` period can be retrieved,
      including periods that are not calendar months
- [ ] Every invariant in the [context doc](../domain/income-tracking.md)
      has a test proving it

## Roadmap beyond v1

Order is a current belief, not a commitment.

1. **Analysis & Insights.** Once it exists for Expense Tracking, extend it
   to consume Income Tracking read-only as well — this is what enables a
   net-cashflow view without either context owning aggregation itself.
2. **Ingestion — only if it turns out to be needed.** Unlike Expense, this
   wasn't named as a near-future certainty; revisit when there's an actual
   signal.
3. **Later, unordered.** Accounts & Balances (which would give both
   Expense and Income a place to affect a running balance, should that
   ever become a real need); user-managed sources; splitting.

Each of these repeats the four-stage process from scratch — including a
fresh discovery interview.

## Open questions

- [ ] `[Open]` Should `Money` and `Period` be promoted to `common/` as
      shared value objects between Expense Tracking and Income Tracking,
      or stay duplicated per feature until they diverge? See the
      [context doc](../domain/income-tracking.md#open-modelling-questions).
- [ ] `[Open]` Does Ingestion apply to income the same way it will for
      expenses?
- [ ] `[Open]` Other modelling questions listed in the
      [context doc](../domain/income-tracking.md#open-modelling-questions).

## ADR candidates

- [ ] The GraphQL scalar and Postgres column type for an **amount** —
      ideally one ADR resolving this for both Expense Tracking and Income
      Tracking together, since the representation should be identical.
- [ ] Whether the predefined source set is seeded by Flyway migration,
      application config, or a Kotlin enum — same question as Expense
      Tracking's category set.
- [ ] Whether `Money` (and `Period`) become shared value objects in
      `common/` now that two features need them identically.

---

## Discovery log

Append-only record of the interview that produced this spec. Answers are as
given. Never edit a past entry; add a new dated one if an answer changes.

### Round 1 — 2026-08-09: placement and problem framing

**Q1. The core architectural question: should Income be a standalone
record mirroring Expense (no balance, no account), or does recording
income imply a balance/account exists that it credits?**
**A:** Standalone, mirrors Expense — no Account, no Balance. Recording
income changes nothing else.

**Q2. Whose problem is this v1 solving, and is the pain the same one named
for expenses (existing tools don't fit how you record/analyze)?**
**A:** Same owner, same pain shape.

### Round 2 — 2026-08-09: making the model concrete

**Q3. What income sources/categories does v1 need to cover?**
**A:** Predefined flat set — Salary, Freelance / Side income, Gift,
Refund, Investment return, Other.

**Q4. Should recording an income entry behave exactly like recording an
expense — amount + date (defaults to today, can be future-dated) +
source, note optional, one confirmation?**
**A:** Yes, exact mirror of Expense recording.

**Q5. Does v1 need anything beyond record + retrieve (by date / by
period) — e.g. a combined net (income − expenses) view?**
**A:** Record + retrieve only, no net/aggregation.

### Round 3 — 2026-08-09: context shape and mirror rules

**Q6. How should the bounded context be organized, given Income and
Expense are structurally identical? The context map currently routes
"Income" to Accounts & Balances — that routing needs correcting.**
**A:** New sibling context: Income Tracking, mirroring Expense Tracking as
its own bounded context with its own glossary. Neither owns balances,
budgets, or aggregation.

**Q7. Should an income entry support splitting into multiple sources, and
is deletion hard delete or left open like Expense v1?**
**A:** Mirror Expense exactly — no split, deletion left open.
