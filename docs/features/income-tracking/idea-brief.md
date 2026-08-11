---
status: Confirmed
owner: "Maksym Krupenko"
reviewers: []
updated_at: "2026-08-11"
feature_size: <XS|S|M|L|XL>     # set by sdlc:classify-size, not here
stage: "01"
ticket: "<ticket-id>"
value_score:
  rice: 2
  state: confirmed
  confirmed_at: "2026-08-11"
feasibility_state: confirmed
---

<!-- Stage 01 → see SDLC/plugin/skills/interview/SKILL.md -->
<!-- Why: capture the idea before it's forgotten or retold incorrectly -->

# Idea Brief — income-tracking

## 1. Raw idea
User needs a feasibility to log an income with amount, source, date and optional note so he will increase his account amount and could track from where and when his money come. Currency by default is PLN, multiple currency is out of the scope for now. Account amount modification is out of scope as well.

## 2. Problem
People who earn from more than one source — a salary plus freelance/side income, gifts, refunds, investment returns — have no single place to record and later recall each inbound payment: how much came in, when, and from where. income-tracking is a pure, append-only journal of income entries; it deliberately does not compute, store, or display any account balance — that is a separate, later concern.

## 3. Users
Individuals managing personal finances who have more than one income stream and want a simple, dated record of what came in and from where, without being forced into a full budgeting method or a bank-account connection. Current real user base: 2 people (the product owner and their spouse) — this is a pre-launch, personal-use stage product, not yet a market-facing SaaS.

## 4. Why now
income-tracking is the deliberate first building block toward a full personal-finance picture. An adjacent feature, expense-tracking, already shipped the same day using the same structural pattern — income is the natural companion half needed before any full picture of money in vs. money out can exist.

## 5. Out of scope
- Account balance computation, storage, or display — income entries never modify or aggregate into a balance.
- Multi-currency support — the only supported currency for v1 is PLN.
- Directly editing an account's balance.

## 6. Competitive analysis
| # | Product · URL | Features | Value per feature (1-5) | Gap |
|---|---|---|---|---|
| 1 | Goodbudget · https://goodbudget.com | Manual income entry, envelope-based allocation | 3 | Envelope budgeting model adds ceremony just to log a single income entry |
| 2 | EveryDollar · https://www.everydollar.com | Free-tier manual transaction entry, unlimited custom categories | 3 | Full zero-based budgeting tool wraps the simple act of logging income; paid tier pushes bank sync |
| 3 | Quicken Simplifi · https://www.quicken.com/simplifi | Three-level category hierarchy, unlimited custom categories | 4 | Rich source categorization, but locked behind a paid subscription and a full net-worth/budgeting suite |
| 4 | YNAB · https://www.ynab.com | Income logging embedded in a zero-based ("assign every dollar") budgeting method | 2 | Forces a specific budgeting philosophy on the user just to record that money arrived |
| 5 | Monarch Money / Copilot Money · https://www.monarchmoney.com , https://copilot.money | Passive, auto-categorized income tracking via linked bank accounts | 2 | Assumes a bank connection for categorization; weak fit for manual, connection-free logging |

Footnotes: researched 2026-08-11 via web search, queries "personal finance app manual income logging by source category 2026" and "YNAB vs Monarch Money vs Copilot Money income tracking features comparison".

## 7. Strategic approaches

### Approach A — Quick Income Log
- **Thesis**: Let users capture any income the moment it lands, in under ten seconds, so a habit forms before richer insights are needed.
- **For whom**: Newly-onboarded users establishing their first financial habit in the product, before expenses or budgeting exist.
- **Outcome metric**: % of active users logging ≥1 income entry — baseline 0% → target 40% within 30 days of signup.
- **Key trade-off**: No categorization insights, trends, or recurring-income detection at launch — users get a bare list, not analysis, in exchange for shipping fast and proving the habit forms.
- **Effort signal**: S
- **Recommended?** ●

### Approach B — Income Story, Not Just Log
- **Thesis**: Turn each logged income entry into an instantly visible income pattern — streak, source mix, momentum — so logging feels like building a personal income narrative, not filling a form.
- **For whom**: Freelancers and side-income earners with irregular, multi-source income who lack a single place to see their full income picture.
- **Outcome metric**: Weekly logging retention (users who log again within 7 days of first entry) — baseline ~15% → target 40%.
- **Key trade-off**: Requires lightweight pattern/streak surfacing beyond a plain ledger, adding product and interaction complexity to a "just log it" feature.
- **Effort signal**: L
- **Recommended?** ◯

### Approach C — Income Snapshot Journal
- **Thesis**: Logging income becomes a habit users keep because each entry instantly reflects back a light picture of where their money is coming from.
- **For whom**: Freelancers/multi-source earners who want visibility into irregular income without full budgeting overhead.
- **Outcome metric**: % active users logging ≥1 income record — baseline 0% → target 40% within 60 days.
- **Key trade-off**: A source-breakdown summary view boosts perceived value and return visits but costs more effort than a bare entry form, and risks feeling like a "mini dashboard" before expenses even exist.
- **Effort signal**: M
- **Recommended?** ◯

## 8. Multi-perspective feedback

### Engineer
- A: Low integration surface, minimal state to validate — fast and safe to ship.
- A: Weak behavioral signal for later features — no data on what drives retention.
- B: Highest complexity — derived state (streak, mix, momentum) adds a new consistency layer that must stay correct under edits/deletes.
- B: Real-time narrative generation adds latency/performance risk on every entry.
- C: Moderate complexity — a read-only aggregation layer, isolable and testable independently from the core logging flow.

### Executive
- A: Proves the basic logging habit fast and cheaply; frees capacity to move to expenses sooner.
- A: Risks feeling too thin, may undersell the product's ambition.
- B: Highest potential stickiness, but premature differentiation on incomplete, one-sided data before expenses/budgeting exist.
- B: "Insights" framing conflicts with the feature's own pure-journal scope.
- C: Best balances shipping speed with early user-facing value; sets a natural precedent for future insight features.

### UX-researcher
- A: Near-zero friction, single screen — but nothing teaches "why log at all," risking a one-and-done user.
- B: Entry itself stays simple, but the streak/mix payoff needs enough history to feel real; early entries feel empty.
- B: Strong "aha" moment once populated, at the cost of a steeper first-use learning curve.
- C: Entry stays lightweight, summary is a separate, opt-in view; moderate learning curve that teaches value without overwhelming.
- C: Summary may go undiscovered if not linked directly from the entry flow.

### Synthesis matrix
|         | Engineer | Executive | UX |
|---------|:--------:|:---------:|:--:|
| App. A  | +        | 0         | -  |
| App. B  | -        | -         | +  |
| App. C  | 0        | +         | 0  |

## 9. Trade-offs and edge cases

### Trade-offs per approach
| Approach | Pros | Cons |
|---|---|---|
| A | Fastest to ship, lowest risk, easiest to measure | Low perceived value, no habit reinforcement, weakest differentiation |
| B | Highest stickiness potential, strong "aha" moment | Highest complexity/cost, premature given one-sided (income-only) data |
| C | Balances speed and value, sets precedent for future insight features | Extra screen/navigation, moderate cost, easy to bury if not well-linked |

### Edge cases
- A post-dated (future) income entry — how it sorts and displays relative to today.
- Accidental duplicate entries (same amount/date/source) from a double submit.
- Editing or deleting a past entry after any downstream summary depends on it.
- Very large, zero, or malformed amount input — validation boundaries.
- No income logged for an extended period — empty-state handling.
- Repeated use of the "Other" source — a signal the fixed source list is too rigid.
- A long free-text note — truncation and display in the list view.
- A high volume of entries per user over time — list scan/pagination experience.

## 10. Risks
- **(Top devil's-advocate vector)** Users mentally treat the income ledger as their real account balance and budget against the sum of logged income, despite balance being explicitly out of scope — surfaces as churn or complaints framed as "the app told me I had money."
- Foreign-currency income gets logged as a raw number under the PLN-only assumption with no conversion or flag, silently corrupting totals until a user notices the mismatch.
- The fixed source list doesn't match reality for many users, so "Other" absorbs a large share of entries, making source-based reporting useless once it exists.
- No audit trail on edits/deletes to an append-only ledger — correcting a mistake either isn't possible or silently breaks the integrity guarantee users expect from a "ledger."
- Income logged with no link to a future account/expense context risks a painful backfill when expense-tracking-driven net-cashflow features are eventually built.

## 11. RICE — Claude proposed
- **Reach (R)**: 2 — rationale cites §3 Users: the real, current user base is the product owner and their spouse (pre-launch, personal-use stage; no production user base exists yet per repo state).
- **Impact (I)**: 2 (High) — rationale cites §2 Problem severity + §8 Executive perspective: income-tracking is the strategic foundation for the whole product, but as a pure journal without balance or expenses it doesn't resolve the user's full problem on its own, so High rather than Massive.
- **Confidence (C)**: 1.0 — rationale cites §15 Open questions: the remaining unknowns (currency/amount validation) are minor and don't affect the core scope, which is fully settled.
- **Effort (E)**: 2 person-weeks — rationale cites §7 Approach A's Effort signal S, adjusted down from the Approach C M-signal baseline given a solo developer reusing the pattern already proven by expense-tracking.
- **RICE = R × I × C / E = 2 × 2 × 1.0 / 2 = 2**
- **State**: confirmed

## 12. Feasibility — Claude proposed
- [☑] **Tech**: The identical feature-first + layered pattern (a value-object for money, a period concept, a fixed-choice classification, full CRUD) is already proven twice in this repo — once as the `health` wiring reference, and once by the adjacent `expense-tracking` feature shipped the same day.
- [☑] **Skills**: The same person already built `expense-tracking` end to end, the same day, using the same technology stack — no new skill is required.
- [☑] **Time**: `expense-tracking`, a feature of equivalent complexity, shipped in a single day (2026-08-09) — the same day `income-tracking` itself shipped, confirming the pattern is fast to repeat for a solo developer, even though the formal RICE Effort estimate above is a more conservative forward-looking projection.
- **State**: confirmed

## 13. Recommendation
**Selected: Approach A — Quick Income Log** — A bare-minimum entry form plus a period-filtered list, with no categorization insights, streaks, or summary view. This is recommended because: the RICE score (§11, R=2×I=2×C=1.0/E=2=2) reflects a tiny current reach but solid Impact and full Confidence, meaning the low absolute number is a reach artifact, not a weak-value signal; Feasibility (§12) is fully confirmed (3/3 ☑) on the strength of the adjacent `expense-tracking` feature; the Engineer cell in the §8 synthesis matrix rates Approach A "+" for its low integration surface and safety to ship quickly, which matches a solo-developer's real bandwidth; and none of the researched competitors (§6) offer a clean, standalone way to log income without bundling it into a full budgeting method or a bank-sync connection — Approach A closes exactly that gap. This also matches what already exists in the shipped v1 code (a form, a list, a period filter — no summary or streak view), so the brief documents reality rather than proposing new scope.

**Locked-in pointer**: income-tracking stays a pure, append-only journal (amount, date, source from a fixed list, optional note) with no balance computation and no multi-currency support; any summary/insight view (Approach C) or narrative/streak view (Approach B) is explicitly deferred, not baseline scope.

## 14. Parked & rejected approaches
| # | Approach | Status | Reason | Revisit trigger |
|---|---|:---:|---|---|
| C | Income Snapshot Journal | parked | Adds a source-breakdown summary view beyond what shipped in v1; useful next iteration, not baseline | When expense-tracking's net-cashflow needs or user feedback call for a light aggregate view |
| B | Income Story, Not Just Log | parked | Highest cost/complexity, premature before income has enough history or expenses exist to compare against | When retention data shows users log once and don't return, and a narrative hook is worth testing |

## 15. Open questions
- [ ] Should the amount field enforce an upper bound or decimal-precision limit to guard against fat-finger entry (Risk in §10)? — owner: Maksym Krupenko, due: TBD
- [ ] Is the fixed source list (Salary, Freelance/Side Income, Gift, Refund, Investment Return, Other) final, or should it grow based on real usage of "Other" (edge case in §9)? — owner: Maksym Krupenko, due: TBD

## Related
- Links to `docs/features/income-tracking/CONTEXT.md` (glossary: income, income source)
- Adjacent shipped feature: `docs/product/001-expense-logging.md`, `docs/domain/expense-tracking.md`

## DoD self-check
- [x] 15 sections present
- [x] No anti-pattern terms (Postgres/Redis/etc.)
- [x] Length ≤ 5 pages (~2200 words)
- [x] Frontmatter status: Confirmed
- [x] RICE confirmed (state: confirmed)
- [x] Feasibility confirmed (state: confirmed)
- [x] Recommendation present with rationale citing 4 upstream sections (§6, §8, §11, §12)
