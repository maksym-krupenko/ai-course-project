# Product documentation

Living documentation for **what** this product is and **why**, as opposed to
`docs/adr/` (technical decisions) and `CLAUDE.md` (code layout rules).

## What lives here

| File | Purpose |
|---|---|
| `NNN-<slug>.md` | One product specification per capability. Contains the discovery interview that produced it, the scope boundary, and acceptance criteria. |

Domain modelling (bounded contexts, ubiquitous language, invariants) lives in
`docs/domain/` and is linked from each spec.

## The process every spec follows

A spec is not written up-front from imagination. It is the *output* of three
stages, in order:

1. **Product evaluation & assessment.** Interview the product owner until the
   problem — not the solution — is crystallized. Record every question and
   answer verbatim in the spec's Discovery Log.
2. **Bounded context & ubiquitous language.** Decide whether the capability
   belongs to an existing bounded context or needs a new one. Define every
   term precisely in `docs/domain/<context>.md` before any code names things.
3. **Specification.** Write the spec here: problem, scope, explicit
   no-goals, acceptance criteria, open questions.
4. **Section-by-section review.** Nothing in stages 2 and 3 is written in
   bulk. Each section is presented to the product owner for approval first,
   prefaced by one sentence stating what it solves and why it is needed — a
   section that cannot justify its own existence does not belong in the
   document. Only approved sections are written to disk. The rationale
   sentence is for the review conversation; it does not go into the file.

Only then does planning/implementation start.

## Conventions

- **Status markers** appear throughout. `[Decided]` — the product owner has
  settled it. `[Proposed]` — drafted here but not yet put to them.
  `[Open]` — genuinely undecided. `CLAUDE.md` carries the rule for what to do
  with the last two.
- **Checkboxes** (`- [ ]` / `- [x]`) track live progress. They are updated in
  place as work lands; they are not a historical record.
- **The Discovery Log is append-only.** Answers are recorded as given. If an
  answer is later reversed, add a new dated entry — never edit the old one.
  It is the audit trail of *why* the scope is what it is.
- **Specs are living.** When reality diverges from a spec, the spec is wrong
  and gets updated in place.

## For LLM sessions

Read in this order before proposing work on a capability:
`SPEC.md` (scaffold scope) → this file → the relevant `docs/product/NNN-*.md`
→ the `docs/domain/*.md` it references → `CLAUDE.md` (layering rules, plus
the two rules that bind you when using these documents) → `docs/adr/`
(technical decisions).

## Index

| Spec | Capability | Status |
|---|---|---|
| [001](001-expense-logging.md) | Expense logging & retrieval | Specified — not implemented |
