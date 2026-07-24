# Investigation: Files missing from working directory

## Document metadata

| Field | Value |
|-------|-------|
| Created date | 2026/07/23 17:10 |
| Created by | Claude Code |
| Spec ref | Adhoc — no spec doc |
| Version | 1.0 |

## Summary

While cleaning up Cursor-related references in this repo, `git status` showed several
tracked files as **deleted from the working directory**, even though no tool call in this
session touched them. This doc records the finding so it isn't lost, and so a human can
decide whether to restore them.

## Observed state

Last commit on `develop` before this investigation:

```
02f0dcf8f99caf2acc2b7afab03d0e842b49b965
Author: thanhnd
Date:   2026-07-23 16:56:17 +0700
Subject: security
```

`git status --porcelain` (relative to that commit) showed these files deleted on disk:

- `plan/administrator-auth-authorization.md`
- `plan/authorization-mechanism-explained.md`
- `plan/flyway-migration-switch.md`
- `infrastructure/src/main/resources/db/migration/V4__seed_test_administrators.sql`
- `infrastructure/target/classes/db/migration/V4__seed_test_administrators.sql` (build artifact, regenerated on next build — not a concern)

The `plan/` directory itself still exists on disk but is empty.

## What these files were

- **`plan/administrator-auth-authorization.md`** and **`plan/authorization-mechanism-explained.md`** — explanatory/planning docs for the administrator authentication and role/permission-authorization work done earlier in this project.
- **`plan/flyway-migration-switch.md`** — plan doc for a Flyway migration approach.
- **`V4__seed_test_administrators.sql`** — the Flyway migration that seeds RBAC test data: role `USER`, permissions `ADMINISTRATOR_READ`/`ADMINISTRATOR_MANAGE` granted to role `ADMIN`, and two test accounts (`admin_test`/`Admin@123` on role `ADMIN`, `user_test`/`User@123` on role `USER`). **This migration is load-bearing** — the currently-documented test credentials and permission-authorization test endpoints (`AdministratorController`) depend on it having been applied to the database. If the file is gone but the migration already ran against the local MySQL instance, the database still has the data — only the source file (and Flyway's ability to replay/verify it) is missing.

## Why this matters

- If this deletion was intentional (e.g. manual cleanup by the user outside this session), no action is needed.
- If it was **not** intentional, these files are recoverable from git history since they were committed in `02f0dcf`:
  ```
  git restore plan/administrator-auth-authorization.md plan/authorization-mechanism-explained.md plan/flyway-migration-switch.md infrastructure/src/main/resources/db/migration/V4__seed_test_administrators.sql
  ```
- No destructive action was taken by Claude Code to cause this — it was observed as pre-existing working-directory state at the start of the Cursor-cleanup task, before any file in this list was touched.

## Recommendation

Confirm with the repository owner whether these deletions were intentional. If not, restore via `git restore` as shown above before doing further work that depends on the RBAC test data or the referenced plan docs.
