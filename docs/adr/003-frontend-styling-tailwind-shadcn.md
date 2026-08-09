# ADR 003: Frontend styling — Tailwind CSS v4 + shadcn/ui

- **Status:** Accepted
- **Date:** 2026-08-09

## Context

By the time expense logging and income logging (the first two real features)
existed, the frontend had zero styling infrastructure: every component was
plain semantic JSX with no CSS classes anywhere (`docs/plans/001-expense-logging-implementation.md`
called this out explicitly as the state to match). That was a reasonable
starting point for a technical scaffold, but with two working features now
rendering as unstyled forms and tables, the UI was unusably ugly — enough to
block actually using the app, not just a cosmetic complaint.

The ask was a "modern but not flashy" look, added without a rewrite of the
already-working feature components. Two shapes of solution exist for React:

1. A batteries-included component library (Mantine, Chakra) — fast to wire
   up, consistent look out of the box, but you inherit its component API and
   are more locked into its visual language.
2. A utility-CSS layer (Tailwind) plus a copy-in component set (shadcn/ui) —
   components are generated into the repo as plain, editable TSX rather than
   pulled in as an opaque dependency, styled with Tailwind utility classes.
   More setup, more initial code in the repo, but full control and no
   library upgrade treadmill for visual changes.

Given this project's `CLAUDE.md` conventions already favor feature-first,
inspectable code over hidden framework magic (see ADR 002's reasoning against
default ports-and-adapters indirection), option 2 fits the codebase's existing
bias better.

## Decision

**Tailwind CSS v4 + shadcn/ui**, installed via the official `shadcn` CLI
(`npx shadcn@latest init`, Radix base library, "Nova" preset — neutral base
color, Geist font, CSS-variable theming including unused-but-available dark
mode tokens) rather than configured by hand. Components added so far:
`Button`, `Card`, `Input`, `Label`, `Table`, `Textarea`.

Two deviations from the CLI's defaults, both deliberate:

- **Component location.** The CLI defaults to `src/components/ui` and
  `src/lib/utils`. `CLAUDE.md` already defines `src/shared/` as the home for
  cross-feature building blocks, so the generated files were moved to
  `src/shared/components/ui/` and `src/shared/utils/cn.ts`, and
  `components.json`'s aliases were repointed there so future
  `shadcn add <component>` runs land in the right place without a manual
  move.
- **Category/source pickers stayed native `<select>`, not shadcn's `Select`.**
  shadcn's `Select` wraps Radix's primitive, which renders as a `button`
  with `role="combobox"`, not a real `<select>`. The existing
  `ExpenseForm`/`IncomeForm` tests drive the picker with
  `fireEvent.change(screen.getByLabelText(...), { target: { value } })`,
  which only works against a native form control. Rather than rewrite those
  tests around pointer-driven interaction for a plain picklist with no need
  for search/multi-select/async loading, the native `<select>` was kept and
  styled by hand with the same Tailwind classes shadcn's other inputs use, so
  it's visually consistent without the behavioral change. The generated
  `select.tsx` (and the now-unused `lucide-react` dependency it pulled in)
  were deleted rather than left as dead code.

## Consequences

- New feature UI should default to composing `src/shared/components/ui/*`
  and Tailwind utility classes directly in JSX, not hand-rolled CSS files or
  a new component library.
- Dark mode CSS variables exist (via the Nova preset) but there's no
  light/dark toggle wired up yet — theming is a future concern, not
  something this change addresses.
- Adding a shadcn component that needs icons (e.g. a future `Select` used
  somewhere search/async-driven enough to justify Radix) will need
  `lucide-react` reinstalled — the CLI does this automatically via
  `shadcn add`.
- `radix-ui` and `class-variance-authority` are now direct dependencies (used
  by `Button`'s `asChild` support and `Label`); this is expected shadcn/ui
  plumbing, not an accidental transitive dependency.
- Verified end to end: `npm run typecheck`, `npm run lint`, and `npm test`
  all pass unchanged (no test rewrites needed given the native-`<select>`
  decision above); `npm run build` produces a working bundle with the
  Tailwind CSS and Geist font assets included; the full stack was run via
  Docker Compose + local Vite dev server and the expense create/edit/delete
  flow was exercised in a real browser against the styled UI.
