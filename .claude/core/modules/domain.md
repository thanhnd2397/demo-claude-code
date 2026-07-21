# Domain Layer — Detailed Reference

## Main Domain Object: Member (reference implementation)

`Member` is the canonical example for main domain objects using `@Builder` + compact constructor + `CacheData`.

Key patterns:

- `@Builder` on the record — callers construct via the generated builder, never via a constructor or static `of()` factory
- Compact constructor runs all validation; throws `DomainValidationException` with an error code on failure
- Validation split into focused private methods: `validateRequiredFields(...)`, `validateDependentCount(...)`, `validateApproval(...)`
- Implements `CacheData` — registered in `@JsonSubTypes` on the `CacheData` interface with type name `"member"`
- Field merging on updates is handled by the **MapStruct mapper** in the infrastructure layer (`toUpdateEntity`), not by the domain object

---

## Display Object Examples

### OrganizationMemberListRow

One row in a list screen. Fields: memberId, memberName, position, email, joinDate, status flags.

### OrganizationMemberSearchResult

Paginated search result. Fields: `List<OrganizationMemberListRow> items`, `long totalElements`, `int totalPages`, `int currentPage`, `int pageSize`.

### OrganizationListEntry

Wraps `Organization` domain object + display-only fields: `directChildCount`, `managerName`.

### MemberOrgContext

Join-result context object that replaces multiple adapter calls when a use case needs member + org data together. Annotated with `@Builder` (no validation — populated by infrastructure, not user input).

```java
@Builder
public record MemberOrgContext(
    String memberId,
    String memberName,
    String email,
    String phoneNumber,
    String organizationId,
    String organizationName) {
}
```

Use this pattern when a screen pre-fill or form-load requires a JOIN across two or more entities and returning them separately would create N+1 calls.

### WorkflowTicketTypeOption

One dropdown option on a screen (e.g. ticket-type selector). `@Builder`, no validation.

```java
@Builder
public record WorkflowTicketTypeOption(String workflowId, String displayLabel) {
}
```

---

## Value / Message Objects

### MailMessage

Email payload for `MailSenderAdapter`. No validation — infrastructure validates at send time. Two factory methods:

```java
// Simple (text-only, default sender)
MailMessage msg = MailMessage.of(to, subject, textBody);

// Full (multipart with HTML and explicit sender)
MailMessage msg = MailMessage.of(to, subject, textBody, from, htmlBody);
```

`from` and `htmlBody` may be null. When `from` is null, the adapter uses `spring.mail.from`.

---

## Cache / CacheData

### Cache

```java
// vn.thanhnd.demo.domain.cache.Cache
public class Cache {
    String key;
    LocalDateTime expiredAt;
    CacheData data;
}
```

Mutable class (Lombok). Used as the envelope stored in Redis.

### CacheData — full registry

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "dataType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Member.class,                   name = "member"),
    @JsonSubTypes.Type(value = ItemMaster.class,               name = "itemMaster"),
    @JsonSubTypes.Type(value = ItemMasterList.class,           name = "itemMasterList"),
    @JsonSubTypes.Type(value = LoginOtpData.class,             name = "loginOtp"),
    @JsonSubTypes.Type(value = RequestPasswordTokenData.class, name = "requestPasswordToken"),
    @JsonSubTypes.Type(value = OrganizationListEntryList.class,name = "organizationListEntryList"),
    @JsonSubTypes.Type(value = PermissionList.class,           name = "permissionList"),
    @JsonSubTypes.Type(value = RoleList.class,                 name = "roleList"),
})
public interface CacheData {}
```

**Note:** The JSON discriminator property is `"dataType"` (not `"type"`). Match this when adding new subtypes.

To make a new domain object cacheable:

1. Implement `CacheData` on the record
2. Annotate with `@JsonTypeName("uniqueName")` on the class, OR add a `@JsonSubTypes.Type` entry in `CacheData`

**Existing concrete implementations:**

| Class | Type name | Stored value |
| ----- | --------- | ------------ |
| `Member` | `"member"` | Full member profile |
| `ItemMaster` | `"itemMaster"` | Single item master entry |
| `ItemMasterList` | `"itemMasterList"` | List wrapper |
| `LoginOtpData` | `"loginOtp"` | `String code` — 2FA OTP |
| `RequestPasswordTokenData` | `"requestPasswordToken"` | `String requestPasswordToken` — reset token |
| `OrganizationListEntryList` | `"organizationListEntryList"` | List wrapper |
| `PermissionList` | `"permissionList"` | List wrapper |
| `RoleList` | `"roleList"` | List wrapper |

---

## DomainUtils

```java
// vn.thanhnd.demo.domain.utils.DomainUtils — final, all static
<T> T coalesce(T value, T defaultValue)                          // first non-null
<T> List<T> coalesceList(List<T> value, List<T> defaultValue)   // first non-null, non-empty list
boolean isNullOrEmpty(String value)                              // null or ""
```

---

## DomainValidationException and DomainError

```java
// DomainValidationException — extends RuntimeException
throw new DomainValidationException("E-01-MEMBER-0002");           // message only
throw new DomainValidationException("E-01-MEMBER-0002", cause);    // with cause
```

Error code format: `E-{layer}-{ENTITY}-{sequence}` (e.g. `E-01-MEMBER-0002`, `E-02-ADMINISTRATOR-0005`).
Caught by `ResultHandler` → converted to `DomainError` in `ResultWrapper.failure(...)`.

```java
// DomainError — record in domain.exception
public record DomainError(String field, String errorCode, Object[] args) {
    public static DomainError of(String field, String code) { ... }
    public static DomainError of(String field, String code, Object[] args) { ... }
}
```

`field` is the property name that failed (null for global/object-level errors). `errorCode` is the i18n key.

---

## Adapter Interface Examples

### MemberAdapter (reference)

```java
public interface MemberAdapter {
    Member findById(String id);
    Member findByEmail(String email);
    Member createMember(Member member);
    Member updateMember(Member member);
}
```

### CacheAdapter (service adapter)

```java
public interface CacheAdapter {
    <T> T get(String key, Class<T> type);
    void set(String key, CacheData data, LocalDateTime expiredAt);
    void delete(String key);
    void deleteByPattern(String pattern);
    boolean exists(String key);
}
```

### ObjectStorageAdapter (service adapter)

```java
public interface ObjectStorageAdapter {
    void put(String bucket, String objectKey, InputStream inputStream, long contentLength, String contentType);
    InputStream get(String bucket, String objectKey);
    void delete(String bucket, String objectKey);
    boolean exists(String bucket, String objectKey);
    List<String> listObjectKeys(String bucket, String prefix);
    List<ObjectKeyWithLastModified> listObjectKeysWithLastModified(String bucket, String prefix);
    String getPresignedUrl(String bucket, String objectKey, int expirySeconds);
}
```

Null/empty `bucket` → implementation uses its configured default bucket. `prefix` must not contain `..`.

### EmailTemplateRenderer (service adapter)

```java
public interface EmailTemplateRenderer {
    String render(String templateName, Map<String, Object> model);
}
```

`templateName` is a logical name (e.g. `"email/register-notification"`); infrastructure resolves it to `classpath:/templates/{name}.html`.

### MailSenderAdapter (service adapter)

```java
public interface MailSenderAdapter {
    void send(MailMessage message);
}
```

---

## Enum Structure (standard template)

```java
public enum GenderEnum {
    MALE(1), FEMALE(2), OTHER(0);

    private final int value;
    GenderEnum(int value) { this.value = value; }
    public int getValue() { return value; }

    public static GenderEnum fromValue(Integer value) {
        if (value == null) return OTHER;  // safe default
        return Arrays.stream(values())
            .filter(e -> e.value == value)
            .findFirst()
            .orElse(OTHER);
    }
}
```
