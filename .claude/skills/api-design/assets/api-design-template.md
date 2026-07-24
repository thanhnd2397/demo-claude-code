# API Design: [Feature Name]

## Document metadata
<!-- MANDATORY: DO NOT remove this section -->

| Field | Value |
| --- | --- |
| Created date | yyyy/MM/dd HH:mm |
| Created by | |
| Approved date | yyyy/MM/dd HH:mm |
| Approved by | |
| Spec ref | [path to source .md or "Adhoc"] |
| Version | 1.0 |

### Revision history

| Version | Date | Author | Summary of changes |
| --- | --- | --- | --- |
| 1.0 | yyyy/MM/dd | | Initial draft |

---

## Overview

- **Base path:** [e.g. /organizations]
- **Auth requirement:** [Login required: Yes | No]
- **Auth scheme:** JWT (stateless)

---

## Endpoints

<!--
  One section per server-side event.
  Client-only events (close modal, toggle UI) do NOT produce an endpoint.
-->

---

### [1] [METHOD] /path — [Event Name]

> [One-line description of what this endpoint does.]

- **Authority:** [Who can call this. E.g. "Member with authority `super-admin` OR `edit-organization`." or "Any authenticated member."]

#### Request

- **Trigger:** [When this is called — e.g. "User clicks Submit in the Add Sub-Organization modal."]
- **Path params:**
  - `paramName` (type): description — [Required | Optional]
- **Query params:**
  - `paramName` (type): description — [Required | Optional]
- **Body fields:**
  - `fieldName` (type, Required | Optional): description. Constraints: [e.g. max 255 chars, 10-digit number]
  - `fieldName` (type, Required | Optional): description.

> If no path params / query params / body fields apply, write "None."

#### Main logic

1. [Step 1 in business terms — e.g. "Validate that parentOrganizationId exists and is not deleted."]
2. [Step 2 — e.g. "Resolve manager email to member_id by looking up m_members.mail_address."]
3. [Step 3 — e.g. "Insert a new record into m_organizations with the provided fields."]
4. [NFR step if applicable — e.g. "Invalidate CACHE_ORGANIZATION_LIST after insert." or "No NFR notes for this event."]

- **SQL intent:** [Describe what data is read/written. E.g. "Insert one row into m_organizations with organization_name, phone, manager_id (resolved from email), address, description, parent_id = parentOrganizationId, created_at = now."]

#### Response

- **Success:**
  - HTTP status: `[200 | 201 | 204]`
  - Body: `{ fieldName: type, ... }` — [description of what is returned, or "Empty body (204)."]
- **Error cases:**
  - `400 Bad Request` — [Condition. E.g. "organizationName is blank."] → Error message (Vietnamese): "[exact]" / Error message (English): "[exact]"
  - `403 Forbidden` — [Condition. E.g. "Caller does not have required authority."] → [Redirect or response body behavior.]
  - `404 Not Found` — [Condition. E.g. "parentOrganizationId does not exist."] → Error message (English): "[exact]"
  - `[other codes as applicable]`

---

### [2] [METHOD] /path — [Event Name]

> [Repeat the structure above for each server-side event.]

---

## Error Response Shape

Define the standard error response body used across all endpoints on this screen, using the **actual project error response class** (verified from the util module):

```json
{
  "errors": [
    { "field": "fieldName", "message": "exact error message" }
  ]
}
```

> If the project uses a different standard error shape, describe it here using the actual field names from the project's error response class. Reference the existing convention by name if it has already been documented (e.g. "Same as organization-list screen error shape.").
