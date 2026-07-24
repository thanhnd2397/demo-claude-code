# Monolithic — Spring Boot Clean Architecture

Java 25 · Spring Boot 4.0.1 · MySQL + Redis · REST API

@.claude/core/architecture.md
@.claude/core/modules.md

> Full docs: `.claude/core/` (tech-stack.md, development.md available on demand)

---

## Review Accountability

> **MANDATORY NOTICE:** Every output produced by this agent — code, plans, designs, test cases, and documentation — will be reviewed by an experienced **Senior Technical Lead**, **Test Lead**, and **Project Manager**. All work must meet production-quality standards and withstand expert scrutiny before it is accepted.

---

## Working Mode

### Plan Mode Default

- Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
- Use plan mode for verification steps, not just building
- Write detailed specs upfront to reduce ambiguity
- Multi-step plans must have explicit verify conditions per step

### Self-Improvement Loop

- After ANY correction from the user: update `tasks/lessons.md` with the pattern
- Write rules for yourself that prevent the same mistake
- Ruthlessly iterate on these lessons until the mistake rate drops
- Review lessons at session start for this project

### Verification Before Done

- Never mark a task complete without proving it works
- Diff behavior between main and your changes when relevant
- Check logs, demonstrate correctness
- Ask yourself: "Would a staff engineer approve this?"

### Demand Elegance (Balanced)

- For non-trivial changes: pause and ask "is there a more elegant way?"
- If a fix feels hacky: "Knowing everything I know now, implement the elegant solution"
- Skip this for simple, obvious fixes — don't overengineer
- Challenge your own work before presenting it

---

## Karpathy Coding Principles

Four guardrails against the most common LLM coding failures.

### 1. Think Before Coding

- State assumptions explicitly before writing code
- When multiple interpretations exist, present them — never pick silently
- Push back if a simpler approach exists
- If something is unclear, stop and ask before proceeding

### 2. Simplicity First

- Make every change as simple as possible; impact minimal code
- No features beyond what was explicitly asked
- No abstractions for single-use code
- No "flexibility" or "configurability" not requested
- No error handling for impossible scenarios
- Self-test: "Would a senior engineer say this is overcomplicated?" → If yes, rewrite
- If 200 lines could be 50, rewrite it

### 3. Surgical Changes

- Do not improve adjacent code, comments, or formatting
- Do not refactor things that aren't broken
- Match existing style even if you'd do it differently
- If you notice unrelated dead code: **mention it, don't delete it**
- When YOUR changes create orphans (unused imports/vars/funcs): clean those up
- Litmus test: every changed line must trace directly to the user's request

### 4. Goal-Driven Execution

- Transform tasks into verifiable goals with success criteria
- "Add validation" → "Confirm invalid inputs are rejected with a clear error"
- "Fix the bug" → "Confirm the reported scenario now behaves correctly"
- "Refactor X" → "Confirm behavior is unchanged before and after"
- No laziness: find root causes — no temporary fixes; senior developer standards

---

## Build & Verify

```bash
./mvnw clean compile -P=local
```

> Machine-specific `JAVA_HOME` override is in `CLAUDE.local.md`.

**Run after every code generation step. Fix all errors before marking complete.**

---

## Module Quick Reference

| Module | Location | Purpose |
| ------ | -------- | ------- |
| web | `web/` | Application entry point |
| presentation | `presentation/` | HTTP layer — controllers, filters, security |
| application | `application/` | Use cases, DTOs |
| domain | `domain/` | Business logic, domain models, adapter interfaces (ports) |
| infrastructure | `infrastructure/` | Adapters, JPA entities, repositories, DB migrations |
| util | `util/` | Constants, helpers, response models, base exceptions |

Each module has its own `CLAUDE.md` — read it before implementing in that module.

---

## Agent Rules

The `.claude/rules/` directory contains rule files loaded automatically by Claude Code.

| Rule File | Scope |
| --------- | ----- |
| `skill-orchestrator-gateway.md` | Always loaded |
| `implement-mode.md` | Path-scoped: `**/src/main/**/*.java` |
| `plan-review-checklist.md` | Path-scoped: `docs/plan/**/*.md` |

---

## Document Output Locations

Whenever a plan or spec document is created (explicitly requested, or produced by a skill), save it under `docs/`:

| Document type | Save to |
| -------------- | ------- |
| Plan (implementation plan, explanatory/analysis doc) | `docs/plan/` |
| Spec (API design or other spec document) | `docs/spec/` |

Create the target folder if it does not exist. Do not save these documents anywhere else (e.g. repo root, `.claude/`).

---

## Project Conventions

- Maven for dependency management; the Maven artifact name must match the parent directory name
- Group ID and base Java package: `vn.thanhnd.demo`
- Use dependency versions declared in the parent POM — upgrade only when explicitly requested
- Lombok is allowed for boilerplate reduction (except domain models — those stay plain immutable records)
- Keep a Docker Compose file that runs all components used by the application
- Minimize the amount of generated code
- Only when explicitly requested: CI pipeline changes (`.circleci/`), version bumps (semantic versioning, bump PATCH), README updates

---

## Best Practices

1. Dependencies point **inward** — never let domain depend on infrastructure or presentation
2. Domain models are **immutable Java records** — no framework annotations
3. One use case = one business operation, single public method
4. Use `@Transactional(readOnly = true)` for reads — routes to replica automatically
5. Command use cases return `ResultWrapper<T>` via `ResultHandler.handle(...)` — never throw for validation
6. DTO/Bean Validation (`@NotBlank`, `@Size`, `@Pattern`) for format/length/required — DB-dependent checks only in use case
7. Cache: check first on reads, invalidate on writes
8. No `var` keyword — explicit types always
9. JavaDoc on all public/protected methods; inline comments for non-obvious logic
