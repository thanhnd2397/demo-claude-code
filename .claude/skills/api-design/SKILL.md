---
name: api-design
description: Produces an API design document (REST endpoints, request/response shapes, main logic) from a feature/requirement spec. Use when the user wants to design REST endpoints for a feature, or when asked to produce an api-design document. This project is REST-API-only (no server-side rendering, no frontend) — output is API design only.
---

# API Design

Take a feature/requirement spec and produce **one output file**:

- **API Design** — REST endpoints, request/response shapes, main business logic per endpoint.

The output is a business-logic document: no implementation code, no framework annotations.

---

## When to Use This Skill

Use this skill when the user asks to:

- Design REST endpoints for a feature.
- Produce an API design document from a feature/requirement spec.
- Create an `api-design` file.

---

## Core Principle: All Logic Lives in the API

This project exposes REST endpoints only — there is no server-side rendered view and no frontend to design. All business rules, validation, authorization, and data formatting are enforced by the API and returned in the response body.

| Concern | API responsibility |
|---------|--------------------|
| Validation — field-level (required, max length, format, pattern) | API validates and returns 400 with field errors |
| Validation — business rules (uniqueness, existence, cross-entity, state checks) | API validates exclusively and returns the specific error case |
| Data formatting | API formats or returns raw values per the response shape defined below — state which |
| Business rules | API enforces all rules (authority, state checks, uniqueness) |
| Authorization | API checks authority and returns 403 when denied |

---

## Input Modes

### Mode A — Standard (preferred)

The user provides a **feature/requirement spec `.md` file** (e.g. in `docs/spec/`). This is the primary input.

- Do NOT re-derive information that is already in the spec. Transform and restructure it.

### Mode B — Adhoc

The user describes the feature directly in the prompt — no spec doc exists yet.

- Ask the user for what cannot be inferred: feature name, authority rules, main business logic per action.
- Apply the same information-gathering rules as Mode A.

---

## Document Language (MANDATORY)

- The document body (section headings, descriptions, requests, logic, response behavior, and notes) MUST be written in **English**.
- User-facing messages MUST be written in all supported product UI languages.
- Message format MUST be explicit by language:
  - **Error message (Vietnamese):** "..."
  - **Error message (English):** "..."
- If the product supports only one UI language, state that language explicitly and provide that one message.

---

## Wording: Specific and Precise (MANDATORY)

The generated design document is the source of truth for implementation. Ambiguous wording confuses developers and leads to inconsistent builds. **All content in the generated document MUST be specific and precise.**

**Do not use in the generated document:**

- **"e.g." / "for example"** when listing options — state the actual options.
  - Bad: "Search by name or email (e.g. by name or email)."
  - Good: "Search by name or email."
- **"e.g." for quantities or limits** — state the exact value.
  - Bad: "Return a bounded list (e.g. top 20)."
  - Good: "Return a bounded list (top 20)."
- **"or equivalent"** — state the exact behavior or message.
  - Bad: "Return 403 or equivalent."
  - Good: "Return 403 Forbidden."
- **"per application policy" / "per product"** without specifying the behavior.
  - Bad: "On failure, handle per application policy."
  - Good: "On failure, return 403 Forbidden; return forbidden message (same as existing endpoint X)."
- **Vague error messages** — every validation rule must have the exact user-facing message(s).
  - Bad: "Error message: (or equivalent: 'Name is required.')"
  - Good: "Error message (Vietnamese): 'Tên là bắt buộc.' / Error message (English): 'Name is required.'"
- **Vague main logic steps** — "process the data" is not acceptable.
  - Bad: "Validate and process the request."
  - Good: "1. Validate organizationName is not blank. 2. Check parentOrganizationId exists and is not deleted. 3. Insert a new record into m_organizations."
- **Unspecified numbers** — use exact limits (max length 255, limit 20, 10 digits).

**Rule:** Prefer definitive lists and exact values. When referencing existing behavior, name the reference (e.g. "same as existing organization-list endpoint"). Implementors must be able to act from the spec without guessing.

---

## Workflow

1. **Determine input mode** — Does the user provide a feature/requirement spec `.md` file? → Mode A. Otherwise → Mode B.
2. **Read existing project conventions** — Follow the "Reading Existing Project Conventions" section below. Do NOT assume conventions; derive them from code.
3. **Gather required information** — See "Information Gathering" section. Ask only for what is missing; provide concrete suggestions when asking.
4. **Extract source data** — In Mode A: parse the spec (actions, data mapping, auth). In Mode B: parse user answers.
5. **Generate API design file** — Use the template at `assets/api-design-template.md`. One endpoint section per action.
6. **Run self-review** — See "Self-Review (MANDATORY)" section. Fix any failing items before saving.
7. **Save the file** — See "Save Location & Naming" below.

---

## Reading Existing Project Conventions

Before generating the document, read the existing codebase to avoid inventing conventions that already exist:

- **URL / path conventions** — Browse the presentation layer's existing API controllers (e.g. `presentation/src/main/java/.../api/`) to understand the URL prefix, path parameter naming, and HTTP method conventions in use.
- **Error response shape** — Check the util module (e.g. `util/src/main/java/.../response/`) for the project's standard error response class. Use the actual field names; do NOT invent a new shape.
- **Auth scheme** — This project uses stateless JWT for API auth (see root `CLAUDE.md`). Do not add CSRF or session notes to the design.

If the codebase is not accessible, note that conventions were not verified and instruct the user to confirm.

---

## Information Gathering (MANDATORY before generating)

You must be able to answer, from the spec (Mode A) or the user's answers (Mode B):

- Which actions require an endpoint (POST, GET, PUT, PATCH, DELETE)?
- What are the request fields and response fields for each?
- What is the authority per action?
- What does the main logic do (business steps, SQL intent)?

If any of the above is missing or unclear, ask the user before generating. **When asking, provide concrete suggestions** (e.g. options to choose from, examples of what to provide). **After the user replies, confirm you have enough before starting to generate; if something is still unclear, ask a short follow-up with one or two concrete options. Do NOT generate partial output with invented information.**

---

## Save Location & Naming

Save the file in the **current project's** `docs/spec/` directory.

- **API design:** `api-{feature-name}-yyyyMMddHHmmssSSS.md`

Where:

- `{feature-name}` = lowercase, hyphen-separated feature identifier (same as the spec doc base name, e.g. `organization-add`).
- `yyyyMMddHHmmssSSS` = current datetime to milliseconds.
- Example: `api-organization-add-20260328100000123.md`.

Create `docs/spec/` if it does not exist.

---

## API Design Structure

Use the template at `assets/api-design-template.md` as the structure. Key sections per endpoint:

- **HTTP method + path** — derived from action type and resource. Use the derivation table below. If the project has an existing URL convention visible in the presentation layer controllers, follow it.
- **Authority** — who can call this endpoint (from the spec's auth section).
- **Request** — path params, query params, body fields (name, type, required/optional, description).
- **Main logic** — numbered business steps (plain language). Include SQL intent where the spec provides it. If the source action has non-functional notes (cache invalidation, idempotency guard, double-submit prevention, concurrency handling), include them as explicit numbered steps (e.g. "Invalidate CACHE_ORGANIZATION_LIST after insert.").
- **Response** — success shape (fields, types), success HTTP status, error cases (status + message).
- **Error response shape** — at the end of the endpoints section, define the standard error body used across all endpoints in this document, using the **actual project error response class** (verified from the util module); or reference the existing project convention by name (e.g. "Same as organization-list endpoint error shape.").

**One endpoint section per action.**

**HTTP method and path derivation:**

| Action type | HTTP method | Path pattern |
|------------|-------------|--------------|
| Read list | `GET` | `/resource` |
| Read detail | `GET` | `/resource/{id}` |
| Search / typeahead | `GET` | `/resource/search` |
| Create | `POST` | `/resource` |
| Update (full replace) | `PUT` | `/resource/{id}` |
| Update (partial) | `PATCH` | `/resource/{id}` |
| Delete | `DELETE` | `/resource/{id}` |

---

## Non-Functional Requirements

Non-functional requirements (NFRs) from the spec must be represented explicitly in the API design — not left as implicit notes.

**How to surface NFRs per endpoint:**

| NFR type | How to represent in main logic |
|----------|-------------------------------|
| Cache invalidation | Add an explicit numbered step: "Invalidate `CACHE_ORGANIZATION_LIST` after insert." |
| Idempotency guard | Add an explicit numbered step: "Check for duplicate submission using idempotency key `X`; return existing result if found." |
| Double-submit prevention | Add an explicit numbered step: "Reject duplicate requests within [N] seconds based on [key field]." |
| Concurrency / optimistic locking | Add an explicit numbered step: "Check version field matches current DB value; return 409 Conflict if mismatched." |
| Rate limiting | Add a note under the endpoint: "Rate limit: [N] requests per [time window] per member." If no rate limit is defined, write: "No rate limiting defined for this endpoint." |
| Performance SLA | Add a note: "Expected response time: [N ms] at [Xth percentile]." If not defined, write: "No formal SLA defined." |

If an action has no NFR notes, write "No NFR notes for this action." in the endpoint section rather than omitting the topic silently.

---

## Document Metadata

The file MUST start with a **Document metadata** section.

| Field | Value |
|-------|-------|
| Created date | yyyy/MM/dd HH:mm |
| Created by | |
| Approved date | yyyy/MM/dd HH:mm |
| Approved by | |
| Spec ref | (path to source spec doc, or "Adhoc — no spec doc") |
| Version | 1.0 |

The file MUST also include a **Revision history** table immediately after the metadata:

| Version | Date | Author | Summary of changes |
|---------|------|--------|--------------------|
| 1.0 | yyyy/MM/dd | | Initial draft |

---

## Self-Review (MANDATORY)

Before saving the file, run this checklist item-by-item. Fix any item that fails **before** saving.

- [ ] Document metadata is present with all fields (Created date, Created by, Approved date, Approved by, Spec ref, Version).
- [ ] Revision history table is present.
- [ ] Document body is in English; user-facing messages use language-labeled format for all supported UI languages.
- [ ] No ambiguous wording: no "e.g.", "or equivalent", "per policy", "as needed", "consider", "may", "might"; all values are exact.
- [ ] One endpoint section per action.
- [ ] Each endpoint has: HTTP method + path, authority, request shape, main logic steps, response shape + status codes + error cases.
- [ ] URL/path conventions match the existing project conventions (verified from controllers).
- [ ] SQL intent is described as intent/state, not syntax.
- [ ] Error messages match what the spec specifies; bilingual messages include both language variants.
- [ ] NFR notes (cache invalidation, idempotency, double-submit guard, concurrency, rate limiting, performance SLA) are reflected as explicit numbered steps or endpoint-level notes in the relevant main logic; or "No NFR notes" is stated.
- [ ] Error response shape section uses the actual project error response class (verified from util module).
- [ ] No framework annotations or implementation code.
- [ ] No CSRF/session notes (project uses stateless JWT).
- [ ] File saved as `api-{feature-name}-yyyyMMddHHmmssSSS.md` in `docs/spec/`.

---

## JavaDoc Comment Templates (for implementation from this design)

When the endpoints in the generated design are implemented, every controller method and every use case execute method MUST carry a JavaDoc comment following the two templates below. Include this section verbatim (with the templates) in the generated design document so implementors apply it without guessing.

### Controller method template

```java
/**
 * List admin users by company.
 * GET /api/admin/list
 * <p>
 * Retrieves a paginated list of admin users belonging to the same company as the authenticated user.
 * Supports filtering by status, sorting, and pagination.
 * Only accessible from customer site (requires Bearer token).
 * Super user accounts (company_id = null) are excluded from results.
 *
 * @param authenticationInfo The authenticated user's information
 * @param statusFilter Status filter: "all", "active", or "inactive" (optional, default: "all")
 * @param sortBy Sort field: "created_at", "full_name", or "email" (optional, default: "created_at")
 * @param sortOrder Sort order: "asc" or "desc" (optional, default: "desc")
 * @param page Page number (1-based, optional, default: 1)
 * @param limit Number of records per page (optional, default: 10, max: 100)
 * @return RestResponse with ListAdminsResponse containing paginated admin list
 */
```

Structure: (1) one-line summary of the action, (2) HTTP method + path, (3) `<p>` followed by behavior description — filtering/sorting/pagination support, access constraints, exclusion rules, (4) one `@param` per parameter with allowed values and defaults stated exactly, (5) `@return` naming the response wrapper and payload type.

### Use case execute method template

```java
/**
 * Execute list admins operation.
 * <p>
 * Orchestrates the following steps:
 * 1. Find authenticated user by ID
 * 2. Validate user exists and is active
 * 3. Verify user is not a super_user (has valid company_id)
 * 4. Extract company ID from user
 * 5. Apply filters and retrieve admin list with pagination
 *
 * @param request ListAdminsRequest containing authenticated user ID, filters, sorting, and pagination
 * @return ListAdminsResponse with paginated admin list
 * @throws CoreException if authentication fails or user is unauthorized
 */
```

Structure: (1) one-line summary "Execute {operation} operation.", (2) `<p>` followed by "Orchestrates the following steps:" and a numbered list mirroring the endpoint's main logic steps from this design document, (3) `@param` describing the request object and what it carries, (4) `@return` naming the response type, (5) `@throws` for the failure cases.

The numbered steps in the use case JavaDoc MUST match the "Main logic" steps defined for that endpoint in the design document — same order, same content.

---

## Anti-Patterns to Avoid

- **Duplicating the spec content verbatim** — transform and restructure; do not copy-paste.
- **Inventing endpoints** — only create endpoint sections for actions that exist in the source spec.
- **Vague main logic** — "process the data" is not acceptable. State the steps: validate → check → persist → return.
- **Missing error cases** — every endpoint can fail. Always define at least one error response.
- **Inventing URL conventions** — always check existing controllers before defining paths.
- **Inventing error response shapes** — always check the util module for the project's standard error class.
- **Silent NFR omission** — if an action has no NFR notes, say so explicitly; do not leave the topic blank.
</content>
