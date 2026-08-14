---
name: solo-dev-interview
description: >
  Consolidated 13-phase ideation skill — Socratic interview, 3 strategic
  approaches via parallel sub-agents, multi-perspective review (Engineer/UX),
  devil's advocate, Claude-proposed RICE/Feasibility. Single entry-point for
  ideation phase. Produces idea-brief.md (14 sections, ≤5 pages). Triggers on
  "raw idea", "capture an idea", "interview a feature X", "brief for X", "new
  feature X", "idea brief", "intake feature X", "start new feature",
  "ideation for {slug}", "/sdlc-interview {slug}". Replaces the prior intake
  + brainstorm + interview trio. ADRs are no longer part of this skill — they
  are spawned inline by the architecture-design skill at gate 04-05. Not to
  be confused with the global `interview` skill (stress-testing ideas) — this
  one is bound to SDLC ideation phase and writes an artifact into
  docs/features/.
---

# Skill: interview (SDLC ideation phase — single entry-point)

Consolidated 13-phase ideation runner. Single entry-point for the ideation phase. Replaces the prior atomic trio `intake` + `brainstorm` + `interview` with one autonomous Claude-driven protocol. Output: a single `docs/features/<slug>/idea-brief.md` with 14 sections (≤5 pages), no separate `brainstorm.md` / `initiatives.md`.

## Why this consolidation

Single entry-point for ideation. Replaces intake + brainstorm + interview. Autonomous Claude-driven research (strategic approaches, multi-perspective review, devil's advocate, RICE/Feasibility proposals) with user confirmation via AskUserQuestion. No more user-input RICE numbers («calculator game»). No more separate brainstorm.md / initiatives.md. ADR is not a gate-1 concern — it moves to gate 3 (after sad.md (architecture-design) §Trade-offs); the ideation phase stays pure product.

## Owner

Idea author (PM / Eng / CTO / anyone). Tech Lead joins at multi-perspective review (phase 5) if asked.

## When to use

- «capture an idea <slug>», «new brief for <feature>», «raw idea for <feature>».
- «interview a feature <slug>», «ideation for <slug>», «brief for <feature>».
- «intake feature <slug>», «start new feature with CONTEXT», «full intake for <slug>».
- User drops a raw idea in prose and asks «format this per SDLC» / «run ideation for <slug>».
- Glossary-aware: on start the skill reads `CONTEXT.md` if it exists (repo root or `docs/features/<slug>/`), keeps the glossary as session state, and triggers `sdlc:fix-term` inline for new domain terms.
- Skip if `docs/features/<slug>/idea-brief.md` already exists with `status: Confirmed` and is fresh (≤2 weeks) — update it first, don't rewrite.

## Inputs

- `<slug>` — kebab-case, short (`rate-limiting`, `goals-tracking`). If the user didn't give one — suggest 2-3 options based on the idea.
- (Optional) prior notes / links / ticket the user already has.

## Mode handling

**This skill is planning-mode-native.** Phases 0-10 execute entirely in read-only mode (Read, WebSearch, Agent, AskUserQuestion). Only on the 10 → 11 transition is `ExitPlanMode` called with the synthesized plan, after which Phase 11 copies the template and writes `idea-brief.md` to disk.

**Why:** AskUserQuestion checkpoints in Phases 1, 2, 8, 9, 10 are not «clarifying questions», but a **mandatory data-input protocol**. The user must actually go deep into the idea; without this the artifact = a reconstruction from the model's memory, not an interview.

**Auto Mode override:** if the `Auto Mode` system-reminder is active in the session, it does **not** cancel the AskUserQuestion checkpoints in this skill. Auto Mode applies to «pause to check whether I should proceed» moments between phases, not to data input. Fabricating the raw idea, Socratic answers, RICE/Feasibility/Recommendation confirms = nullify whole interview.

## AskUserQuestion style

Every `AskUserQuestion` in this skill (Phases 1, 2, 8, 9, 10) is phrased so that **a PM without a technical background or a junior developer in their first year** can answer without help nearby.

**Mandatory shape:**

1. **`question` field — 3-4 sentences** made of three blocks:
   - **CONTEXT** — where this question came from, which Phase we're at, what's already been gathered (1-line recap)
   - **WHY IT MATTERS** — what breaks if the answer is wrong (e.g. «RICE without understanding Effort = decision based on wishful thinking; Phase 10 will have a broken foundation»)
   - **WHAT KIND OF JUDGMENT IS NEEDED** — what to look at before choosing; whether the prior phase's output needs re-reading

2. **Option `description` — 3-5 sentences** made of three elements:
   - **What technically happens**: which line in the idea-brief changes, which later phases this answer affects
   - **What the option means in plain words** — no jargon:
     - Not «RICE score 80, Approach C» → «the RICE formula (Reach × Impact × Confidence / Effort) gave 80 for variant C; this means C looks more resource-justified than A (60) or B (45) — but this is a Claude forecast, not facts»
     - Not «Feasibility 3/3 ☑» → «all three feasibility blocks (Skills, Time, Tech) were marked "confirmed" by Claude — this means the team has the expertise, there's room in the release windows, and the tech stack isn't a blocker. If any of these is TBD — Phase 10 Recommendation will carry a warning»
     - Not «strategic vector» → «the main direction we're heading in: e.g. "consolidate content delivery inside BeerLMS" — anything that suggests expanding scope beyond this direction will be flagged as scope creep in Phase 6-7»
   - **Hidden trade-off** — if an option has a consequence a junior might not see (e.g. «Mark recommendation as TBD» → «all downstream skills (write-prd, architecture-design) hard-refuse until status is Confirmed — this blocks the entire SDLC pipeline for this feature») — state this explicitly in the description

**Forbidden:** one-line descriptions; technical terms without explanation; trade-offs hidden in a follow-up.

**Why:** the PM audience of this skill works in product language, not engineering jargon; the junior audience doesn't have full context on the SDLC pipeline. Direct quote of feedback from 2026-05-23: «The explanations need to be even clearer for people who are literally juniors in development» (context — sdlc:architecture-design, with the requirement «bake this not only into the architecture but also into the idea brief and into write-prd»). This requirement is mirrored in `sdlc:architecture-design/references/ask-examples.md` and `sdlc:write-prd/references/ask-examples.md`.

**Planning mode compatibility:** since all Write operations are concentrated in Phase 11 (post-ExitPlanMode), the skill starts correctly in any permission mode (default / acceptEdits / plan / Auto). If ExitPlanMode is unavailable (i.e. the session wasn't started in plan mode) — Phase 11 runs immediately after Phase 10, without a transition.

## Protocol

**13 phases. Phases 0-10 read-only. Phase 10.5 = ExitPlanMode. Phases 11-13 execute writes + self-check + commit propose.**

### 0. Pre-plan setup (read-only)

- **Read** `./templates/idea-brief.md` — load the skeleton into session memory (NO copy yet).
- **Read** `CONTEXT.md` (root and `docs/features/<slug>/` if it exists) — load `## Glossary` into session state.
- **Verify** `docs/features/<slug>/idea-brief.md` does not exist with `status: Confirmed` (else: skip, update existing).
- **NO Write / Edit / mkdir.** Setup becomes one of the plan's steps, executed in Phase 11.

### 1. Idea capture (AskUserQuestion — mandatory)

One AskUserQuestion for a raw paragraph: «describe the idea in 1-3 sentences in your own words». Persist verbatim in session memory as §1 Raw idea draft. Do not edit — this is the baseline.

### 2. Socratic deep dive (AskUserQuestion — mandatory)

Pick 3-5 questions from 5 categories based on the idea's shape:
- **Problem clarity** (what exactly hurts, for whom, how often).
- **Solution validation** (why this exact solution, what was tried before).
- **Success criteria** (what "it worked" means — a concrete metric).
- **Constraints** (timeline, budget, team capacity, dependencies).
- **Strategic fit** (how this fits the roadmap / OKR / business outcome).

Delivery: AskUserQuestion in batches of 2-3 (not all at once).

### 3. Glossary capture (deferred fix-term)

For every new domain word in the user's answers — add the term to the session-state list `pending_glossary_terms`. **Do not call** `sdlc:fix-term` now — that would write to CONTEXT.md, which is not allowed in planning mode. Skip generic tech terms (HTTP, JSON, queue, cache, database). Terms are applied in Phase 11 (post-ExitPlanMode) before writing idea-brief.md.

### 4. Strategic approaches (3 parallel Agent.tool calls, read-only)

Shared prompt template, 3 personas run in parallel via separate sub-agents:
- **Variant-A (Simplicity):** shortest path, MVP-style, minimum moving parts.
- **Variant-B (Differentiation):** wow-factor / strategic moat / unique angle.
- **Variant-C (Balanced):** trade-off between A and B.

Each sub-agent returns a 1-paragraph approach with:
- **Name** (3-5 words).
- **Thesis** (1 sentence, product language — NO tech terms like Redis/Postgres/Kafka).
- **For whom** (which segment from §3 Users).
- **Outcome metric** (1 KPI: baseline → target).
- **Key trade-off** (1 line).
- **Effort signal**: S / M / L.

### 5. Multi-perspective review (2 parallel Agent.tool calls, read-only)

Two personas run in parallel via sub-agents, each seeing all 3 approaches from §6:
- **Engineer** — concerns / risks / blockers. Explicitly told in the prompt: «no library/DB names — abstract concerns only (latency, throughput, complexity, integration surface)».
- **UX-researcher** — user friction / discoverability / onboarding curve.

Each returns 3-5 bullets with concerns / value / risks for **each** of the 3 approaches.

Build §7 Synthesis matrix (2 personas × 3 approaches) with 6-word justifications per cell (+/0/-) in session memory.

### 6. Trade-offs + edge cases (synthesis, read-only)

Claude synthesizes in session memory (no user input — review/edit only):
- Trade-offs per approach: pros / cons table.
- 5-8 edge cases any approach must handle (data, integrations, failure modes, ops).

### 7. Devil's advocate (1 Agent.tool call with clean context, read-only)

Spawn 1 sub-agent with a clean context (NO upstream session memory), prompt: «find how this could fail. 5-10 attack vectors with production signals (what exactly breaks, how it shows up in monitoring/customer churn/incidents)».

The most critical attack vector → reserved for §9 Risks. The rest — for §8 Edge cases.

### 8. Claude-proposed RICE (AskUserQuestion — mandatory)

Claude computes R/I/C/E from upstream sections:
- **Reach** ← §3 Users (number of users / quarter affected).
- **Impact** ← §2 Problem severity + Multi-perspective feedback bullets (§7).
- **Confidence** ← number of TBDs / open questions; many unresolved → 0.5; all facts concrete → 1.0.
- **Effort** ← Effort signal from §6 approaches (S = 1-2 person-weeks, M = 3-5, L = 6-12).

Compute `R × I × C / E`. AskUserQuestion per number (4 separate checkpoints or 1 multiSelect batch) with options: `Confirm N` / `Adjust higher` / `Adjust lower` / `Mark TBD`. Rationale in the idea-brief cites the upstream section.

### 9. Claude-proposed Feasibility (read-only repo scan + AskUserQuestion — mandatory)

Claude scans the project repo (read-only `find`/`ls`/`Glob` over feature dirs `docs/features/`, `src/`, `app/`, `pkg/`, `services/`, project-specific paths) for adjacent features that already shipped similar tech / workflow.

Proposes 3 checkboxes:
- **Tech** ☑/☐ — with justification («similar to <existing feature> in <module>»).
- **Skills** ☑/☐ — with justification («team already shipped <X>, same skill applies»).
- **Time** ☑/☐ — with justification («similar feature <X> shipped in <N> weeks»).

AskUserQuestion per checkbox (3 separate or 1 multiSelect batch): `Confirm ☑` / `Flip to ☐ — <reason>` / `TBD`.

### 10. Recommendation synthesis (AskUserQuestion — mandatory)

Claude picks one of the 3 approaches from §6 + writes a 3-5 sentence rationale in session memory.

Rationale MUST explicitly cite:
- RICE score from §10.
- Feasibility state from §11.
- ≥1 multi-perspective synthesis matrix cell from §7.

AskUserQuestion for user confirm: `Accept recommendation` / `Pick different approach` / `Mark recommendation as TBD`.

### 10.5. ExitPlanMode handoff (planning → execute)

Everything above is session memory only. Now the skill **calls `ExitPlanMode`** with a plan that contains:

1. Create directory `docs/features/<slug>/` (if absent).
2. Copy template `./templates/idea-brief.md` → `docs/features/<slug>/idea-brief.md`.
3. Apply pending glossary terms (Phase 3 list) via `sdlc:fix-term` to `CONTEXT.md`.
4. Fill 14 sections + Related + DoD self-check in the new file from all of session memory (Phases 1-10).
5. Update frontmatter: `status: Confirmed`, `value_score.{rice,state,confirmed_at}`, `feasibility_state: confirmed`.
6. Run Phase 12 self-check (regex, length, citations).
7. Propose commit + next owner.

If the ExitPlanMode tool is unavailable (skill didn't start in plan mode) — skip this step and run Phase 11 directly. The plan in session memory stays the same.

### 11. Execute: fill expanded idea-brief

After `ExitPlanMode` (or immediately, if plan mode isn't active):

- **mkdir** `docs/features/<slug>/` if absent.
- **Copy** template → `docs/features/<slug>/idea-brief.md`.
- **Apply** pending glossary terms (call `sdlc:fix-term` for each, if any).
- **Edit/Write** all sections 1-14 + Related + DoD self-check from session memory. Update frontmatter:
  - `status: Confirmed`
  - `value_score.rice: <N>`, `value_score.state: confirmed`, `value_score.confirmed_at: <today YYYY-MM-DD>`
  - `feasibility_state: confirmed`
  - `updated_at: <today>`

Parked approaches (the 2 non-recommended ones from §6) — go into §13 with reason + revisit trigger.

### 12. Self-check vs DoD

Run all checks (Read + grep over the file just written):
- **14 sections present.** All of 1-14 + Related + DoD self-check filled.
- **No anti-pattern terms in the body.** Regex check (excluding the DoD self-check meta-line): `\b(Postgres|Redis|Kafka|MySQL|SM-2|FSRS|Leitner|SQLAlchemy|gorm|JSONB)\b` + `p99`. **Word boundary matters**: `chi` as a substring in «architecture» is a false positive; add `\b`.
- **Length ≤ 5 pages** (~2200 words ±10%). If over — compress §6 Approaches paragraphs and §7 Multi-perspective feedback bullets.
- **Rationale citations.** §12 Recommendation cites §7 (1 cell) + §10 (RICE) + §11 (Feasibility).

If any check fails → identify the offending section, re-Edit it, then re-check.

### 13. Propose commit + next owner

Suggest a commit (do not auto-execute):

```
01: idea-brief for <slug>
```

Next owner: PM + Tech Lead → `sdlc:write-prd <slug>` (the gate is now `idea-brief.md status: Confirmed`).

ADR (`sdlc:architecture-design`) is NOT invoked at gate 1 — that's a gate 3 concern (after sad.md (architecture-design) §Trade-offs). If the §12 recommendation looks like a hard-to-reverse technical choice — note that in §14 Open questions, but don't open an ADR thread here.

## Questions for discussion

- What's the slug — kebab-case, short, no date?
- Which user segment suffers most from this problem?
- Why now — what's the trigger (incident / contract / deadline)?
- What metric do we use to measure that this worked?
- Which of the 3 strategic approaches is closer to how the team usually solves similar problems?
- Do you agree with the Claude-proposed RICE numbers — or do they need adjusting?
- Are all 3 Feasibility checkboxes actually closed, or is there an unknown somewhere?

## Definition of Done

- `docs/features/<slug>/idea-brief.md` created and committed.
- All 14 sections filled (no empty H2, `<!-- TBD -->` allowed where honestly missing).
- No anti-pattern tech terms in the body (verified by internal regex check, word-boundaries on).
- Length ≤ 5 pages (~2200 words ±10%).
- Frontmatter `status: Confirmed`, `value_score.state: confirmed`, `feasibility_state: confirmed`, `confirmed_at: <date>`.
- §12 Recommendation rationale cites RICE (§10) + Feasibility (§11) + ≥1 multi-perspective cell (§7).
- **AskUserQuestion checkpoints actually fired** in Phases 1, 2, 8, 9, 10 (verify via the user-message trail). If even one was fabricated → artifact NOT DoD-valid.
- Next-stage owner assigned (PM + Tech Lead → `sdlc:write-prd`).

## Anti-patterns

- **User-input RICE («calculator game»).** Old skill asked user for Reach/Impact/Confidence/Effort — user has no grounding to answer. New flow: Claude proposes from upstream sections (Users → Reach, Multi-perspective feedback → Impact, TBDs → Confidence, Effort signal → Effort). User only confirms or adjusts.
- **Tech terms in idea-brief body** (Postgres, Redis, Kafka, SM-2, FSRS, p99 latency, JSONB). This is a PRODUCT brief. Tech lives in PRD §6 + sad.md (architecture-design) + ADR (gate 3+). Phase 12 self-check enforces this.
- **Single approach in §6.** Strategic approaches MUST be 3 (Simplicity / Differentiation / Balanced). One approach = decision already taken, nothing to evaluate.
- **Skip multi-perspective review.** Engineer-only view → blind to UX risk. UX-only view → blind to implementation cost. Need both perspectives in §7 to balance.
- **Devil's advocate from same session context.** Phase 7 MUST spawn a sub-agent with clean context, otherwise it's biased by all the optimism upstream.
- **Skip Feasibility repo scan.** Phase 9 must do `find`/`ls` over feature dirs and cite adjacent shipped features. «Tech: ☑ — we know how» without citation = guess.
- **Recommendation without rationale citing 3 upstream sections.** Phase 10 rationale MUST cite §7 (multi-perspective cell), §10 (RICE), §11 (Feasibility). Otherwise it's «I feel like A».
- **Propose ADR at end of phase 13.** ADR moves to gate 3 (after sad.md (architecture-design)). Gate 1 is pure product — no tech lock-in.
- **Brainstorm-style transcript dump.** §13 Parked & rejected is structured (table with status / reason / revisit trigger), not a raw chat log.
- **Solution-mode prose in §2 Problem.** «We need to add Redis» → wrong section. §2 is the problem only; solutions live in §6 Approaches.
- **Fabricating user answers under Auto Mode.** The `Auto Mode Active` system-reminder says «work without stopping for clarifying questions» — this applies to pause-to-check moments, not to this skill's in-flow AskUserQuestion checkpoints. Phases 1, 2, 8, 9, 10 MUST fire real AskUserQuestions; generating raw idea / Socratic answers / RICE confirms / Feasibility confirms / recommendation accept without user input = a reconstruction artifact, not an interview. If an AskUserQuestion is rejected via permission denial — stop and tell the user, don't work around it.
- **Writing files inside planning mode.** Phases 0-10 are read-only. If the skill started in plan mode and you try to Write/Edit/mkdir before ExitPlanMode (Phase 10.5) — permission deny. Keep the whole artifact in session memory until Phase 11.

## Template

→ [./templates/idea-brief.md](./templates/idea-brief.md)

## Example invocation

> **User:** «interview a feature: 3 customers complained this week about 429s because of noisy neighbors, need per-user rate limiting»
>
> **Skill behavior (planning-mode-friendly flow):**
>
> **— Plan mode (read-only) —**
> 1. **Phase 0** — suggests slug `rate-limiting-per-user`. User confirms. **Read** template and root `CONTEXT.md`. NO copy yet.
> 2. **Phase 1** — AskUserQuestion: «describe the idea in 1-3 sentences». Captures the raw paragraph verbatim in session memory as the §1 draft.
> 3. **Phase 2** — Socratic batch 1 (AskUserQuestion): «which customer segment?» «how often does this hit?» Batch 2 (AskUserQuestion): «what was tried before?» «what does 'worked' mean — which metric?»
> 4. **Phase 3** — the term «tenant» appeared in the answers → added to `pending_glossary_terms` (fix-term is called in Phase 11, since it writes to CONTEXT.md).
> 5. **Phase 4** — 3 parallel sub-agents (single message):
>    - A (Simplicity): «Per-tenant request quota at edge proxy» — fastest, generic, S effort.
>    - B (Differentiation): «Adaptive per-tenant quota based on plan-tier» — pricing leverage, L effort.
>    - C (Balanced): «Static per-tenant quota with self-serve config» — M effort, customer can tweak.
> 6. **Phase 5** — 2 sub-agents (Engineer / UX) review all 3 in parallel. Engineer stays abstract (no Redis/nginx). Synthesis matrix in session memory.
> 7. **Phase 6** — Claude synthesizes trade-offs + 6 edge cases in session memory.
> 8. **Phase 7** — sub-agent with clean context: «how does this fail?» Returns 7 attack vectors. Top one → reserved for §9 Risks.
> 9. **Phase 8** — Claude proposes RICE: R=200, I=2, C=0.8, E=3 → 107. AskUserQuestion per number; user adjusts Effort to 4 → Score = 80.
> 10. **Phase 9** — Claude scans the repo (read-only): finds adjacent `usage-metering`. Proposes 3 ☑. AskUserQuestion per checkbox; user confirms all 3.
> 11. **Phase 10** — Claude picks **Approach C**. Rationale cites: RICE=80, Feasibility 3/3 ☑, Engineer bullet (multi-perspective cell). AskUserQuestion: user accepts.
>
> **— ExitPlanMode handoff —**
> 12. **Phase 10.5** — `ExitPlanMode` with plan: «create dir, copy template, apply fix-term tenant, fill 14 sections, run self-check, propose commit».
>
> **— Execute (post-plan) —**
> 13. **Phase 11** — `mkdir docs/features/rate-limiting-per-user/`, copy template, `sdlc:fix-term tenant`, Write idea-brief.md with all sections. Frontmatter `status: Confirmed`, `confirmed_at: 2026-05-21`.
> 14. **Phase 12** — self-check: 14 sections ✓, no Postgres/Redis in body ✓, 4.2 pages ✓, citations ✓.
> 15. **Phase 13** — Commit message proposed: `01: idea-brief for rate-limiting-per-user` (user executes). Next: PM + Tech Lead → `sdlc:write-prd rate-limiting-per-user`.
