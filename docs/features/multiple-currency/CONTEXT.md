---
status: Living
updated_at: "2026-08-11"
---

# Domain Context — multiple-currency

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
- conversion — applying the current exchange rate to translate a stored amount into another currency purely for display or aggregation; the underlying stored amount and its original currency never change. NOT currency (conversion is the operation of translating between units; currency is the unit itself).
- currency — the unit (e.g. USD, EUR, UAH) that a single monetary amount (a transaction, balance, or budget line) is expressed in; a property of the amount itself, not of the account or user. NOT amount (currency is the unit a value is expressed in; amount is the numeric quantity itself).
- exchange rate — the current currency-to-currency conversion factor fetched at the moment an amount is converted or displayed; the same transaction can show different base-currency values over time as the rate updates.
