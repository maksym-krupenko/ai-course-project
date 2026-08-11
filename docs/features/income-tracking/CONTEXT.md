---
status: Living
updated_at: "2026-08-11"
---

# Domain Context — income-tracking

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
- income — a user-logged record of money received into an account from any source (salary, gift, refund, side income, etc.), the inbound counterpart to an expense. NOT transaction (a transaction is any addition or subtraction to an account's balance; income is specifically an inbound one).
- income source — a user-defined or predefined label describing where an income record came from (e.g. Salary, Freelance, Gift), attached to an income entry for grouping/reporting. NOT category (income source is the income-side equivalent of an expense category — distinct terms per direction, not interchangeable).
