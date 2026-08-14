---
status: Living
updated_at: "2026-08-14"
---

# Domain Context — accounts

<!--
CONTEXT.md is the domain glossary — not a PRD and not a scratch pad. NO implementation
detail here (no datastore/broker/framework names, no API contracts) — only domain words
and the boundaries between them. Implementation choices live in the SAD and ADRs;
behaviour lives in PRD.md.

Multi-context repos: each bounded context has its own CONTEXT.md at its root path
(registered in CONTEXT-MAP.md). System-wide terms that span all contexts live in the
repo-root CONTEXT.md. Never duplicate a term across files — pick one owner.

Terms get fixed inline, the moment they surface in an interview / PRD / review — never
batched «I'll consolidate later». Empty H2 → prune before commit; keep only the sections
that carry real content. ## Glossary is mandatory; the other two are optional.
-->

## Glossary

<!-- One line per term: name · one-sentence canonical definition · one-sentence boundary
     (what it is NOT / the concept it gets confused with). Alphabetical once there are a few. -->
- account — a named container that groups a user's expense and income entries and shows a running balance derived by summing them; a user may hold several (e.g. cash vs. bank). NOT category (an account represents where money physically sits; a category classifies what an expense/income was for).
- backfill — the one-time operation, run when the accounts feature ships, that attaches every expense/income entry a user already logged before the feature existed to their new default account, so no historical entry is left unattributed. NOT conversion (backfill is a one-time structural attribution of existing entries to an account; conversion is a repeated display-time currency translation).
- balance — the running total of an account, computed as the sum of all expense and income entries linked to it, expressed in that account's single currency. NOT account (balance is a derived number; account is the container it belongs to).
- default account — the one account that pre-exists for every user and receives an expense/income entry whenever no other account is explicitly chosen; every user always has exactly one. NOT "the only account" (users may create additional accounts; default only describes the fallback destination for unclassified entries).
- original amount — the optional starting balance a user enters when an account is created (or, for the default account, at the one-time rollout moment), representing money that pot already held before any expense/income entry existed; defaults to zero if not set, and is fixed thereafter. NOT balance (original amount is a fixed, one-time input captured once at creation; balance is the live, ever-changing derived total that original amount merely contributes to as one term).
