

Plan a product plan and domain specification in a way it could be stored in the project as a living documentation and
reused later by other LLM chat. The functionality plan implementation should start from product evaluation and
assessment: at this stage you should interview me with questions to clarify and crystallize my intent and my problem. The
next stage is definition of bounded context (existing or a new one) and ubiquitous language for it. Based on these two
points we should create specification or other appropriate file that will be stored in docs folder (there is adr, we
should probably create another folder) and describe the functionality on this high level principle. You should use [ ]
points and other structured output to track your progress and record questions and answers and also the meanings and
definitions that we identified together.
before you make any change I want you to first present it to me one section at a time for my review and approve. Each section has to have a one sentence description why it's needed and what problems solve. The structure is : What solves and why
body of the section

- [ ] Stage 1 — Product discovery (interview you; record Q&A verbatim)
- [ ] Stage 2 — Bounded context + ubiquitous language (name the context, define every term, mark invariants)
- [ ] Stage 3 — Write docs/product/ + docs/domain/ (spec + glossary, LLM-reusable, cross-linked to ADRs)

Stage 1 — Discovery.** Interview the product owner before writing
anything. Ask about the problem, never about the solution.

- Ask in small rounds of 3–4 questions, each round shaped by the last.
- Offer concrete options with their consequences attached, not blank
  prompts. Reacting to a wrong option is easier than answering an open
  question, and the consequences are what make the trade-off visible.
- Keep going until the problem fits in one sentence the owner recognises.
  "Existing tools don't fit how I record and analyse" is a finished answer.
  "I want to track my expenses" is not — it is a solution wearing a
  problem's clothes.
- Record every question and answer verbatim in the Discovery Log.

**Stage 2 — Bounded context.** Decide whether the capability belongs to an
existing context in `docs/domain/` or needs a new one. Define every term
before any code names anything. For each term also write its **trap** — the
bigger, more familiar concept it will be mistaken for.

**Stage 3 — Specification.** Write the spec. No-goals carry the reason each
was excluded. Acceptance criteria must be capable of failing: "entry is
fast" protects nothing, "three inputs and one confirmation" does.

**Stage 4 — Review.** Present each section for approval before writing it,
prefaced by one sentence saying what it solves and why it is needed. A
section that cannot state its own problem does not belong in the document.