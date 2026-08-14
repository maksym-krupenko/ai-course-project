---
status: Approved
owner: "Maksym Krupenko"
reviewers: []
updated_at: "2026-08-14"
feature_size: "M"
stage: "03"
ticket: "<TBD>"
---

# PRD — accounts

> **Inputs (required):** [idea-brief](./idea-brief.md) · [CONTEXT](./CONTEXT.md)
> **Reference module:** `backend/src/main/kotlin/com/financeapp/features/{income,expense}` — code patterns used: the feature-first + layered structure this PRD's future implementation will follow, the `NotFoundException` / `DomainValidationException` error sentinels, and — most consequentially — the `Money` value object in both features, which currently validates `currency == "PLN"` and rejects anything else. No authZ/ownership pattern exists to reference (see §6.1).
> **External context channels used:** Reference module code (`income`/`expense` backend features) only — no MCP/docs/RAG channels selected.

## 1. Context

`expense-tracking` and `income-tracking` both shipped deliberately without any account or balance concept — every entry sits in one undifferentiated list, so a user managing their personal finances has no way to know how much money exists in any real-world pot (cash vs. bank vs. card) or trust a running balance anywhere in the product. The real, current user base is two people (the product owner and their spouse), pre-launch, personal-use stage.

`accounts` is a planned roadmap step, not a reaction to an incident: when `expense-tracking` and `income-tracking` shipped, both of their domain docs explicitly named "Accounts & Balances" as the next, deliberately deferred bounded context, and `income-tracking`'s own brief called itself "the deliberate first building block toward a full personal-finance picture" — `accounts` is that next building block.

The committed approach (idea-brief §13, Approach A — Default Account, Simple Balances): ship one default account plus manual account creation, computing each account's balance as a live sum of its linked expense/income entries, with nothing else — no deletion, no transfers, no multi-currency or currency editing.

Traceability and decisions made during this PRD's interview, on top of the idea-brief:
- Every monetary amount in the product is currently locked to `PLN` product-wide. Since a non-PLN account could therefore never receive a real entry under the system as it exists today, and multi-currency entries are the explicit scope of the separate, not-yet-started `multiple-currency` feature, account creation offers **PLN only** in this version — not the full currency-code universe the raw idea originally implied. Revisit once `multiple-currency` ships.
- Idea-brief Open Question 1 (currency-lock enforcement) is resolved: currency is fully locked at creation with no edit path at all — not even shown as a read-only field.
- Idea-brief Open Question 2 (default-account rename) is resolved: the default account keeps a visible default indicator regardless of what a user renames it to, so it stays recognizable as the fallback destination for entries logged without an explicit account.
- **Decision override / scope refinement:** during the §4 Socratic walk, the user extended account creation beyond the idea-brief's plain "simple sum" framing to include an optional **original amount** — a fixed, one-time starting balance representing money a pot already held before any expense/income entry existed. It applies to user-created accounts at creation and, once only, to the default account at its first provisioning; it defaults to zero and, like currency, is never editable afterward. This is additive within Approach A's spirit (still one deterministic term in a sum, no reconciliation logic) rather than a reopening of Approach B/C — but the critic pass correctly flagged that a new field, a second one-time capture flow (independent of backfill), and 5 additional ACs is a real scope increase beyond the idea-brief's "S / 1 person-week" estimate for Approach A. **Overridden by author, rationale: keep the full scope as decided; the feature is re-classified S → M** (see `.size`) rather than trimming original amount back to new-accounts-only.
- No authorization/ownership model exists anywhere in the product yet (`SPEC.md` explicitly lists "no per-user data model" as a current no-goal, and neither `income` nor `expense` scope any query/mutation by an owner). Per `CLAUDE.md`, this PRD does not bolt on ad hoc authorization to fill a coverage gap — see §5's explicit N/A.

## 2. Goals

- Every expense/income entry a user has ever logged — past or future — is attributed to a specific account they can trust as a real-world money pot, closing the "where did my money actually go" gap `expense-tracking`/`income-tracking` left open.
- Users can create additional accounts beyond the default to represent distinct real-world pots (e.g. cash vs. bank vs. card), matching how they already think about their money, starting from the real balance each pot already holds.
- Users see each account's current balance without doing their own math or cross-checking a spreadsheet.

## 3. Non-goals

- **Deleting an account** — not requested in the raw idea (only default + create + edit); out of scope for this version, though the product must still prevent a user from ending up with zero accounts.
- **Transfers / moving money between accounts** — no cross-account movement flow exists in v1; a user simulating one via matching expense+income entries is a known, accepted risk, not a supported feature.
- **Mixed-currency accounts or cross-currency conversion** — each account holds exactly one currency, fixed at creation; conversion between currencies is the separate, not-yet-scoped `multiple-currency` feature's concern.
- **Editing an account's currency or original amount after creation** — both are permanently locked the moment an account is created (or, for original amount on the default account, at its one-time provisioning moment); there is no edit path for either, confirmed during this PRD's interview (resolves idea-brief Open Question 1 for currency).

## 4. User stories

### US-01: Start with a default account

**As a** user
**I want** a default account waiting for me the first time I use the product, with a one-time chance to set its original amount
**So that** every entry has somewhere to belong without extra setup, and the account can start from the real balance I already had

### US-02: Create an additional account

**As a** user
**I want** to set a name and optional original amount when creating a new account (currency is fixed to PLN)
**So that** a new account can start from the real-world balance it already held, not always from zero

### US-03: Rename an account

**As a** user
**I want** to rename any of my accounts, including the default one
**So that** its label matches how I actually think of that pot

### US-04: See an account's running balance

**As a** user
**I want** to see each account's current balance
**So that** I know how much money exists in that pot without doing my own math

### US-05: Trust that past entries are attributed

**As a** user
**I want** every expense/income entry I logged before `accounts` existed to already be attached to my default account once the feature ships
**So that** I don't lose my balance history or have to redo attribution myself

### US-06: Recognize the default account

**As a** user
**I want** the default account to stay visually identifiable even after I rename it
**So that** I always know which account new, unclassified entries fall into

## 5. Acceptance criteria

> **Coverage note — authorization type:** explicitly N/A. No authorization/ownership model exists anywhere in this product yet — `SPEC.md` names "no per-user data model" as a current, deliberate no-goal, and neither of the two already-shipped sibling features (`income`, `expense`) scope any query or mutation by an owner. Per `CLAUDE.md`, this PRD does not invent ad hoc ownership checks to fill this coverage slot; authorization is a future cross-cutting decision that belongs in its own ADR when the product actually adds authentication.

### AC-01 (US-01) — happy path

**Given** a user opens the product for the very first time
**When** their account list loads
**Then** the system shows exactly one account already present, labeled as their default account, with a zero balance (if they skip setting an original amount)

### AC-02 (US-01) — happy path

**Given** a user is at the one-time moment their default account is first provisioned (whether that is the feature's rollout, for a user who already existed, or their first-ever use of the product, for a user who joins afterward)
**When** they enter an original amount
**Then** the system sets the default account's balance to include that amount as its starting point

### AC-03 (US-01) — domain invariant

**Given** a user already set (or skipped) their default account's original amount at its one-time provisioning moment
**When** they look for a way to set it again
**Then** the system offers no such option — it is fixed, like any other account's original amount

### AC-04 (US-02) — happy path

**Given** a user is creating a new account
**When** they submit a name, PLN as the currency, and an optional original amount
**Then** the system creates the account with a balance equal to that original amount (or zero if omitted), shows it in their account list, and confirms the creation to the user

### AC-05 (US-02) — error

**Given** a user is creating or renaming an account
**When** they submit a blank or whitespace-only name
**Then** the system blocks the action and tells the user the account name is required

### AC-06 (US-02) — domain invariant

**Given** a user is creating or renaming an account
**When** they submit a name matching an existing account's name
**Then** the system blocks the action and tells the user that account names must be unique

<!-- Uniqueness is system-wide, not per-user: no per-user data model exists yet (SPEC.md), matching how income/expense currently have no ownership scoping either. Becomes per-user-scoped once a real auth/ownership decision is made — see the authorization N/A note above. -->

### AC-07 (US-02) — error

**Given** a user is creating an account
**When** they submit a negative original amount
**Then** the system blocks the creation and tells the user the original amount cannot be negative

### AC-08 (US-02) — domain invariant

**Given** a user has already created an account
**When** they view or edit that account's information
**Then** the system offers no way to change its original amount — it is fixed at creation

### AC-09 (US-02) — domain invariant

**Given** a user has already created an account
**When** they view or edit that account's information
**Then** the currency field does not appear anywhere in the edit form — the system offers no way to change it

### AC-10 (US-03) — happy path

**Given** a user owns an account (default or user-created)
**When** they rename it to a new, non-blank, unique name
**Then** the system updates the account's display name and confirms the change to the user

### AC-11 (US-06) — domain invariant

**Given** a user renames their default account
**When** the rename is saved
**Then** the system keeps the account's default status and its visible default indicator unchanged, alongside the new name

### AC-12 (US-04) — happy path

**Given** a user has an account with one or more linked expense/income entries
**When** they view that account
**Then** the system shows a balance equal to its original amount plus the sum of all its linked entries, expressed in the account's currency

### AC-13 (US-04) — domain invariant

**Given** a user has an account with no linked entries and no original amount set
**When** they view that account
**Then** the system shows a defined balance of zero in the account's currency, never an empty or missing state

### AC-14 (US-04) — cross-context

**Given** an account's balance includes an expense/income entry
**When** a user edits or deletes that underlying entry from within expense-tracking or income-tracking
**Then** the account's balance reflects the updated or removed entry correctly the next time it is viewed

### AC-15 (US-05) — happy path

**Given** a user had expense/income entries logged before `accounts` existed
**When** the `accounts` feature becomes available to them
**Then** the system has already attached every one of those pre-existing entries to their default account, reflected in its balance, with no action required from the user

### AC-16 (US-05) — domain invariant

**Given** the one-time backfill has already run
**When** the backfill process is triggered again over the same pre-existing entries
**Then** the system leaves every affected default account's balance unchanged rather than attaching any entry a second time

### AC-17 (US-01) — domain invariant

**Given** any account operation runs (creation, rename, or the default account's one-time provisioning)
**When** the operation completes
**Then** the system has never left a user with zero default accounts or with more than one account marked default at the same time

## 6. Non-functional requirements

| Aspect | Target | Measurement |
|---|---|---|
| Latency p95 create/rename account (write) | TBD | see §8 |
| Latency p95 list accounts + balances (read) | TBD | see §8 |
| Throughput | TBD | see §8 |
| Balance accuracy under concurrent writes | 0 drift — a derived balance always equals the exact sum of its original amount plus currently-linked entries | integration test: concurrent entry writes followed by a balance-reconciliation check |
| Availability | TBD | see §8, no production deployment story exists yet (`SPEC.md`) |

## 6.1 Security / privacy

- **Data classification:** Internal. Account names, original amounts, and per-account balances are personal financial data visible only within the product's single implicit user scope — not shared/public, and not a regulated data category (no SSN/health-grade PII involved).
- **Personal data touched:** Two new fields beyond what `income`/`expense` already touch — the user-chosen account name (free-text label) and the optional original amount (a monetary value). No new sensitive PII.
- **AuthZ/AuthN impact:** None. This feature introduces no new permission or capability model; it reuses the same (currently absent) trust boundary as the already-shipped `income`/`expense` features. See §5's explicit authorization N/A.
- **Abuse cases:**
  - **Spam account creation:** no rate limit or cap exists (confirmed during this PRD's interview) — accepted as low-risk at the current 2-person personal-use scale rather than mitigated; revisit if usage grows (§8).
  - **Injection through the account name field:** the name is stored and displayed as opaque text and is never interpreted as executable content, regardless of what characters a user enters.
  - **Backfill misattribution:** if the one-time backfill attached entries to the wrong destination, a user's balance would misrepresent real money; mitigated by AC-16's idempotency guarantee and by treating the backfill as a single, carefully reviewed run rather than a routine operation.
- **Security review:** N/A — no new authorization boundary and no regulated PII is introduced; the feature operates within the same trust boundary as the already-shipped `income`/`expense` features.

## 7. Metrics / KPIs

- **Entry attribution rate** — baseline: 0% of expense/income entries attributed to an account today, target: 100% of entries (including backfilled ones) attributed to an account within 1 week of launch.
- **Manual balance double-checking frequency** — baseline: ~weekly (self-reported spreadsheet/mental math, per idea-brief), target: 0 times/week within 30 days of launch.
- **Multi-account adoption** — baseline: 0 (no accounts concept exists today), target: at least 1 non-default account created by each active user within 30 days of launch.

## 8. Open questions

- [ ] What should the p95 latency and throughput targets be for account create/rename/list operations? Default now: TBD, no numeric commitment. — owner: Maksym Krupenko, due: before `/sdlc-break-tasks`
- [ ] What Availability target should this feature commit to? Default now: TBD, no numeric commitment. — owner: Maksym Krupenko, due: before a production deployment story exists (`SPEC.md` currently excludes one)
- [ ] Should a cap be added to account creation? Default now: no cap. — owner: Maksym Krupenko, due: when usage grows beyond the current 2-person personal-use scale

## Related

- Links to `docs/features/accounts/CONTEXT.md` (glossary: account, backfill, balance, default account, original amount), `docs/features/accounts/idea-brief.md`.
- Adjacent shipped features: `docs/product/001-expense-logging.md`, `docs/product/002-income-logging.md`, `docs/domain/expense-tracking.md`, `docs/domain/income-tracking.md`.
- Adjacent not-yet-scoped feature: `docs/features/multiple-currency/` — will lift the PLN-only constraint noted in §1.
