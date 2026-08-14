---
status: Draft | Confirmed | Frozen
owner: "<PM name>"
reviewers: []
updated_at: "<YYYY-MM-DD>"
feature_size: <XS|S|M|L|XL>     # set by sdlc:classify-size, not here
stage: "01"
ticket: "<ticket-id>"
value_score:
  rice: <number>                 # computed by Claude
  state: proposed | confirmed
  confirmed_at: "<YYYY-MM-DD>"
feasibility_state: proposed | confirmed
---

<!-- Stage 01 → see SDLC/plugin/skills/interview/SKILL.md -->
<!-- Why: capture the idea before it's forgotten or retold incorrectly -->

<!-- Anti-pattern enforcement (Claude self-check, не user-visible):
     Заборонені терміни у тілі: Postgres, Redis, Kafka, конкретні library names,
     SM-2/FSRS/Leitner, схеми таблиць, API endpoints, latency targets, SLOs.
     Це PRODUCT brief. Tech живе у SPEC §6 + architecture-brief + ADR (gate 3+). -->

# Idea Brief — <feature name>

## 1. Raw idea
<1 paragraph verbatim from user, phase 1>

## 2. Problem
<1-3 sentences, facts/numbers>

## 3. Users
<who suffers, frequency, segments>

## 4. Why now
<trigger: incident/contract/deadline>

## 5. Out of scope
- bullet

## 6. Strategic approaches

### Approach A — <3-5 word name>
- **Thesis** (1 sentence, product language)
- **For whom** (which segment from §3 benefits most)
- **Outcome metric** (1 KPI: baseline → target)
- **Key trade-off** (1 line)
- **Effort signal**: S / M / L
- **Recommended?** ◯ / ●  (filled in §12)

### Approach B — ...
[same structure]

### Approach C — ...
[same structure]

## 7. Multi-perspective feedback

### Engineer
- 3-5 bullets (concerns / value / risks — abstract, NO library or DB names)

### UX-researcher
- 3-5 bullets

### Synthesis matrix
|         | Engineer | UX |
|---------|:--------:|:--:|
| App. A  | +        | 0  |
| App. B  | -        | -  |
| App. C  | 0        | +  |

6-word justifications in each cell.

## 8. Trade-offs and edge cases

### Trade-offs per approach
| Approach | Pros | Cons |
|---|---|---|
| A | ... | ... |
| B | ... | ... |
| C | ... | ... |

### Edge cases
- 5-8 items

## 9. Risks
- Top devil's advocate attack vector (from phase 7)
- Other identified risks

## 10. RICE — Claude proposed
- **Reach (R)**: <number> — rationale cites §3 Users
- **Impact (I)**: <0.25 | 0.5 | 1 | 2 | 3> — rationale cites Multi-perspective feedback + Problem severity (§2, §7)
- **Confidence (C)**: <0.5 | 0.7 | 0.8 | 1.0> — rationale cites count of TBDs / Open questions (§14)
- **Effort (E)**: <person-weeks> — rationale cites Effort signal from §6
- **RICE = R × I × C / E = <number>**
- **State**: proposed | confirmed

## 11. Feasibility — Claude proposed

- [☑/☐] **Tech**: <rationale — cite adjacent feature from repo scan>
- [☑/☐] **Skills**: <rationale>
- [☑/☐] **Time**: <rationale — cite similar past feature shipping time>
- **State**: proposed | confirmed

## 12. Recommendation
**Selected: Approach <X>** — <3-5 sentence rationale>

Rationale MUST cite:
- RICE score from §10
- Feasibility state from §11
- ≥1 multi-perspective cell from §7

**Locked-in pointer**: <what this commits us to for write-spec phase>

## 13. Parked & rejected approaches
| # | Approach | Status | Reason | Revisit trigger |
|---|---|:---:|---|---|
| B | <name> | parked | <reason> | <trigger> |
| C | <name> | parked | <reason> | <trigger> |
| - | <name> | rejected | <reason> | - |

## 14. Open questions
- [ ] <question> — owner: <name>, due: <date>
- [ ] ...

## Related
- Links to CONTEXT.md, ticket, related features

## DoD self-check
- [ ] 14 sections present
- [ ] No anti-pattern terms (Postgres/Redis/etc.)
- [ ] Length ≤ 5 pages (~2200 words)
- [ ] Frontmatter status: Confirmed
- [ ] RICE confirmed (state: confirmed)
- [ ] Feasibility confirmed (state: confirmed)
- [ ] Recommendation present with rationale citing 3 upstream sections (§7, §10, §11)
