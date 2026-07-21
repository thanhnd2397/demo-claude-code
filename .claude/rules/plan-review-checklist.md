---
description: Checklist for reviewing plan documents before execution
globs: ["**/.cursor/plans/**/*.md"]
alwaysApply: false
---

# Plan Review Checklist

Applies when writing or reviewing a plan document. A plan is not ready until every item below is checked.

## Structure

- [ ] Plan is triggered for the right scope — used for any non-trivial task (3+ steps or an architectural decision), not for simple one-line fixes
- [ ] Each step has an explicit **verify condition** — how you'll know the step succeeded before moving to the next one
- [ ] Assumptions are stated explicitly, not implied
- [ ] Where multiple valid approaches exist, they are presented as options — not silently decided

## Architecture fit

- [ ] Dependency Rule respected: no step makes domain depend on infrastructure or presentation
- [ ] Domain model changes stay immutable records with no framework annotations
- [ ] Command use cases route through `ResultWrapper<T>` / `ResultHandler.handle(...)` — no throwing for validation
- [ ] Cache invalidation is covered for any step that changes cached data

## Scope discipline

- [ ] No steps introduce abstractions, config flags, or flexibility beyond what was asked
- [ ] No steps refactor or reformat code unrelated to the task
- [ ] Every step traces back to the user's actual request

## Before marking a plan approved

- [ ] Ask: "Would a staff engineer approve this?"
- [ ] If any step feels hacky, ask: "Knowing everything I know now, what's the elegant version?"
