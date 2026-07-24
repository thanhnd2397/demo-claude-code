---
description: Always-on gateway — routes work to the matching mode rule and enforces baseline invariants
alwaysApply: true
---

# Skill Orchestrator Gateway

Before editing any file, identify which mode applies and defer to that rule file:

| If the file matches... | Apply |
| ----------------------- | ----- |
| `**/src/main/**/*.java` | `implement-mode.md` |
| `docs/plan/**/*.md` | `plan-review-checklist.md` |

If none match (docs, config, build files), fall back to the baseline invariants below plus the root `CLAUDE.md`.

## Baseline invariants (apply regardless of mode)

- Dependencies point inward: `presentation → application → domain`, `infrastructure → domain` (implements ports). Domain never depends on infrastructure or presentation.
- Domain models are immutable Java records — no Lombok, no framework annotations.
- Command use cases return `ResultWrapper<T>` via `ResultHandler.handle(...)` — never throw for validation failures.
- `@Transactional(readOnly = true)` for reads, `@Transactional` for writes.
- Every changed line must trace to the user's request — no unrelated refactors (see Karpathy Coding Principles in `CLAUDE.md`).
- When unsure which mode applies or requirements conflict, stop and ask rather than guessing.
