---
description: Conventions for writing main Java source code (non-test)
globs: ["**/src/main/**/*.java"]
alwaysApply: false
---

# Implement Mode

Applies whenever editing production code under `src/main/`.

## Layer rules

- **domain**: immutable records, no framework annotations, no Lombok. Factory methods / compact constructors validate; throw `DomainValidationException("E-{layer}-{ENTITY}-{seq}")` on failure. Adapter interfaces (ports) live here, not implementations.
- **application**: one use case = one business operation, single public method, annotated `@UseCase`. `@Transactional(readOnly = true)` for reads, `@Transactional` for writes. Command use cases return `ResultWrapper<T>` via `ResultHandler.handle(Supplier<T>)` — never throw for validation.
- **infrastructure**: implements domain ports. JPA entities, MapStruct mappers, cache/adapters live here. Lombok allowed (e.g. `@RequiredArgsConstructor`, `@Log4j2`) — never on domain models.
- **presentation**: controllers, filters, exception handlers. `@PreAuthorize` for authorization.
- **util**: shared constants, helpers, base exceptions, response models — no business logic.

## Style rules

- No `var` keyword — explicit types always.
- JavaDoc on all public/protected methods; inline comments only for non-obvious logic (hidden constraints, workarounds, invariants).
- DTO/Bean Validation (`@NotBlank`, `@Size`, `@Pattern`) for format/length/required checks — DB-dependent checks only inside the use case.
- Cache pattern: check cache first on reads, invalidate on writes. Cache keys: `CACHE_<ENTITY>_<OPERATION>_{id}`.

## Guardrails (Karpathy principles)

- State assumptions before coding; if multiple interpretations exist, ask — don't pick silently.
- No abstractions for single-use code, no speculative configurability, no error handling for impossible scenarios.
- Don't refactor or reformat adjacent code that isn't part of the request. Clean up only the orphans your own change creates (unused imports/vars).
- Build after every change: `./mvnw.cmd clean compile -P=local` (Windows). Fix all errors before marking the task complete.

## Dependency diagram maintenance

Before adding a new use case, port, adapter, controller endpoint, cache key, or error code, check `docs/architecture/project-overview.md` (§3 feature catalog, §4 dependency diagram) to see what already touches the same domain model/port/cache key — avoid duplicating or breaking existing invalidation/coupling.

After adding, changing, or removing any of the above, update `docs/architecture/project-overview.md`:

- New/changed use case, port, adapter, or endpoint → update its table (§3, §5, §6, §7).
- New cross-feature coupling (a use case now touches a port/cache key it didn't before) → update the Mermaid diagram in §4.
- New business domain (not just a new operation on an existing model) → add a subsection under §3 and extend §2's diagram if a new module-level dependency appears.
