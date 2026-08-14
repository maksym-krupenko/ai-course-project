---
status: Confirmed
owner: "Maksym Krupenko"
reviewers: []
updated_at: "2026-08-14"
feature_size: <XS|S|M|L|XL>     # set by sdlc:classify-size, not here
stage: "01"
ticket: "<ticket-id>"
value_score:
  rice: 4
  state: confirmed
  confirmed_at: "2026-08-14"
feasibility_state: confirmed
---

<!-- Stage 01 → see SDLC/plugin/skills/interview/SKILL.md -->
<!-- Why: capture the idea before it's forgotten or retold incorrectly -->

# Idea Brief — accounts

## 1. Raw idea
A user has a default account that all their expenses and incomes are tracked against by default, but they can also create additional accounts. The user can edit an account's information (e.g. rename it). Each account — the default one and any user-created ones — shows its own running balance, computed from the expenses and incomes logged against it; each account holds a single currency, so its balance is a simple sum.

## 2. Problem
`expense-tracking` and `income-tracking` both shipped deliberately without any account or balance concept — every entry sits in one undifferentiated list, so a user has no way to know how much money exists in any real-world pot (cash vs. bank vs. card) or trust a running balance anywhere in the product.

## 3. Users
Individuals managing personal finances who log both expenses and income and want to know their actual balance per pot of money, without a full budgeting or accounting suite. Current real user base: 2 people (the product owner and their spouse) — pre-launch, personal-use stage, same as when `income-tracking` shipped.

## 4. Why now
`accounts` is a planned roadmap step, not a reaction to an incident. When `expense-tracking` and `income-tracking` shipped, both of their domain docs explicitly named "Accounts & Balances" as the next, deliberately deferred bounded context, and `income-tracking`'s own brief called itself "the deliberate first building block toward a full personal-finance picture" — `accounts` is that next building block.

## 5. Out of scope
- Deleting an account — not requested in the raw idea (only default + create + edit); the product must still prevent a user from ending up with zero accounts.
- Transfers / moving money between accounts — no cross-account movement flow exists in v1; a user simulating one by logging matching expense+income entries is a known risk (§9), not a supported feature.
- Mixed-currency accounts or cross-currency conversion — each account holds exactly one currency, fixed at creation and never editable afterward; balance is a simple same-currency sum.

## 6. Strategic approaches

### Approach A — Default Account, Simple Balances
- **Thesis**: Ship one default account plus manual account creation, computing balance as a live sum of linked expenses/incomes, with nothing else.
- **For whom**: The 2 current users, who just want to know how much is actually in each pot without any budgeting overhead.
- **Outcome metric**: Trusted balance visibility — 0% of entries have a known account today → 100% of expense/income entries attributed to an account within 1 week of launch.
- **Key trade-off**: No transfers, no deletion, no multi-currency — users work around gaps manually in exchange for shipping fast.
- **Effort signal**: S
- **Recommended?** ●

### Approach B — Balance Confidence Check-ins
- **Thesis**: Each account periodically asks "does this match reality?" so drift from missed or mistyped entries gets caught early, turning the balance into something users believe, not just see.
- **For whom**: Both current users — manual loggers with no external system cross-checking their entries.
- **Outcome metric**: Self-reported trust in balance accuracy — baseline unknown/low → 90%+ answer "yes, this matches my real pot" at first monthly check-in.
- **Key trade-off**: Small recurring interaction cost in exchange for catching silent entry errors before they compound.
- **Effort signal**: M
- **Recommended?** ◯

### Approach C — Per-Pot Trusted Balances
- **Thesis**: Give every real-world money pot its own account with an always-accurate running balance, without taking on multi-currency or transfer complexity yet.
- **For whom**: The two active users who want per-pot clarity (cash vs. bank vs. card), not full budgeting.
- **Outcome metric**: Manual balance double-checking (spreadsheet/mental math) — from ~weekly today → 0 times/week post-launch.
- **Key trade-off**: Ship single-currency, transfer-less accounts now instead of waiting to solve cross-account movement and multi-currency, which aren't blocking today's actual pain.
- **Effort signal**: M
- **Recommended?** ◯

## 7. Multi-perspective feedback

### Engineer
- A: Lowest state-management burden — balance is derived, not stored, so no reconciliation logic; main correctness risk is a one-time, atomic/idempotent backfill of historical entries.
- A: Smallest integration surface — one new entity plus a read-time aggregation over existing records.
- B: Adds a second write path (confirmation events) alongside the entry stream, so balance now depends on two async inputs that must reconcile.
- B: A rushed or mistaken confirmation can mask a real entry error rather than catching it — inverts the intended guarantee.
- C: Same computational shape as A, but "always-accurate" is a stronger implicit guarantee — raises the bar on correctness under concurrent writes as account count grows.

### UX-researcher
- A: Zero setup friction (default account exists on first use), but no way to verify/dispute a balance that looks "off," which invites silent distrust.
- A: No delete/undo for a mistakenly-created account raises anxiety at creation time, suppressing experimentation.
- B: Strong onboarding payoff (users learn balances are "sums of what you logged") but recurring prompts risk reading as nagging if not tuned to activity.
- C: Matches the way people already think about money ("my wallet vs. my savings") — lowest conceptual friction of the three.
- C: No transfers is the biggest friction point — moving money between pots is a common mental model with no first-class flow, forcing manual double-entry workarounds.

### Synthesis matrix
|         | Engineer | UX |
|---------|:--------:|:--:|
| App. A  | +        | 0  |
| App. B  | -        | 0  |
| App. C  | 0        | +  |

- A/Engineer: smallest surface, one contained backfill risk.
- A/UX: frictionless setup, but no trust-building signal.
- B/Engineer: second write path adds real complexity.
- B/UX: builds trust but risks feeling like nagging.
- C/Engineer: same shape as A, stronger guarantees needed.
- C/UX: matches mental model; missing transfers is friction.

## 8. Trade-offs and edge cases

### Trade-offs per approach
| Approach | Pros | Cons |
|---|---|---|
| A | Fastest to ship, lowest complexity, no reconciliation logic | No way to catch mistyped entries; no undo on account creation |
| B | Builds long-term trust in the numbers; catches drift early | Second async state to reconcile; degrades to A if prompts are ignored, while keeping the extra complexity |
| C | Matches users' natural "money pot" mental model; ships fast without waiting on multi-currency/transfers | Correctness burden grows with account count; missing transfers is a real gap for a common use case |

### Edge cases
- Backfilling pre-existing expense/income entries into the default account must be atomic and idempotent — a re-run must not double-attach entries.
- Default-account identity must stay a single, protected reference — no code path should leave a user with zero or multiple accounts marked default.
- Deleting or emptying the last remaining account is not requested, but the product must prevent a user from ending up with zero accounts.
- A user simulating a transfer by logging matching expense+income entries across two accounts of different currencies, silently producing balances that don't sum to anything real.
- Editing or deleting an underlying expense/income entry after it has contributed to an account's balance — the balance must stay correct.
- Renaming an account whose name might be shown elsewhere (history, exports) — no stale copies of the old name.
- An account with zero entries — balance display and empty state.
- Rounding/precision drift in a running balance summed over many entries.

## 9. Risks
- **(Top devil's-advocate vector)** The backfill migration that attaches pre-existing entries to the default account isn't idempotent, and a redeploy or retried job re-runs it — silently doubling the default account's balance on day one, a data-integrity incident that undermines the feature's entire trust promise before it's even used.
- Default-account identity is a single point of failure — if it's tracked loosely (e.g. a flag or a first-row assumption), a future edit path could leave the system with zero or multiple accounts marked default, and new entries would fail to save or silently attach to the wrong account.
- Users work around the no-transfer limitation by logging matching expense/income entries across two accounts with different currencies, producing balances that look correct individually but don't represent any real combined total — and the app has no way to detect this.

## 10. RICE — Claude proposed
- **Reach (R)**: 2 — rationale cites §3 Users: the real, current user base is the product owner and their spouse (pre-launch, personal-use stage).
- **Impact (I)**: 2 (High) — rationale cites §2 Problem (currently zero account/balance concept exists) + §7 Engineer cell rating Approach A "+"; a real, foundational improvement, but not Massive since transfers and multi-currency remain unsolved after this ships.
- **Confidence (C)**: 1.0 — rationale cites §14 Open questions: the two remaining opens (currency-lock enforcement wording, default-account rename semantics) are minor implementation details, not scope unknowns.
- **Effort (E)**: 1 person-week — rationale cites §6 Approach A's Effort signal S, adjusted to the low end given the same solo developer shipped `income-tracking` (a comparable-shape feature) same-day in practice.
- **RICE = R × I × C / E = 2 × 2 × 1.0 / 1 = 4**
- **State**: confirmed

## 11. Feasibility — Claude proposed
- [☑] **Tech**: The feature-first + layered pattern (entity, migration, GraphQL CRUD) is already proven twice — the `health` wiring reference and the shipped `expense-tracking`/`income-tracking` features; the one net-new piece, the one-time backfill migration, is a bounded script within that same proven shape.
- [☑] **Skills**: The same developer already built `expense-tracking` and `income-tracking` end-to-end, same stack, same day each — no new skill required.
- [☑] **Time**: `income-tracking`, a feature of comparable shape, shipped in a single day (confirmed 2026-08-11) despite a more conservative formal estimate — the same precedent applies here, adjusted for the added backfill step.
- **State**: confirmed

## 12. Recommendation
**Selected: Approach A — Default Account, Simple Balances** — This is recommended because: RICE (§10) scores 4 (R=2 × I=2 × C=1.0 / E=1), reflecting a small but genuine, high-confidence, low-effort win; Feasibility (§11) is fully confirmed (3/3 ☑) on the strength of the twice-proven expense/income-tracking pattern and its same-day shipping precedent; the Engineer cell in the §7 synthesis matrix rates Approach A "+" for having the smallest integration surface and only one concentrated, manageable risk (the backfill), versus Approach B's "-" (a second async write path to reconcile) or Approach C's "0" (a stronger multi-account correctness burden for a UX gain A already gets close to); and Approach A's outcome metric (100% of entries attributed to an account) most directly matches the confirmed success criterion, accurate balance visibility, without taking on speculative mechanisms the other two approaches require.

**Locked-in pointer**: `accounts` ships as a default account (protected, cannot be deleted) plus user-created accounts, each with a name and one fixed single currency; users can edit an account's name; each account's balance is a live sum of its linked expense/income entries; pre-existing entries are backfilled into the default account exactly once. No deletion, no transfers, no multi-currency or currency editing is baseline scope — Approaches B (trust check-ins) and C (per-pot always-accurate framing) are explicitly deferred.

## 13. Parked & rejected approaches
| # | Approach | Status | Reason | Revisit trigger |
|---|---|:---:|---|---|
| B | Balance Confidence Check-ins | parked | Adds a second async write path and recurring-interaction cost beyond what a first release needs; useful once real usage exists to justify it | When users report not trusting a displayed balance, or a real data-entry error goes unnoticed for a meaningful stretch |
| C | Per-Pot Trusted Balances | parked | Same effort tier as B for a UX gain (matching the "money pot" mental model) Approach A already gets close to; the framing matters more once account count grows | When a user routinely maintains 3+ accounts and manual mental-summing across pots becomes a real complaint |

## 14. Open questions
- [ ] Should "edit account info" ever be allowed to touch currency after creation, or is currency permanently locked at account creation? (Risk in §9) — owner: Maksym Krupenko, due: TBD
- [ ] What should renaming the default account do to its "default" status and display label — does it stay recognizable as the fallback account after a rename? (Edge case in §8) — owner: Maksym Krupenko, due: TBD

## Related
- Links to `docs/features/accounts/CONTEXT.md` (glossary: account, default account, balance)
- Adjacent shipped features: `docs/product/001-expense-logging.md`, `docs/product/002-income-logging.md`, `docs/domain/expense-tracking.md`, `docs/domain/income-tracking.md` — both named "Accounts & Balances" as the deferred bounded context this feature fulfills.

## DoD self-check
- [x] 14 sections present
- [x] No anti-pattern terms (Postgres/Redis/etc.)
- [x] Length ≤ 5 pages (~2200 words)
- [x] Frontmatter status: Confirmed
- [x] RICE confirmed (state: confirmed)
- [x] Feasibility confirmed (state: confirmed)
- [x] Recommendation present with rationale citing 3 upstream sections (§7, §10, §11)
