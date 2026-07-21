# Application Layer — Detailed Reference

## Read Use Case Example: GetDetailUseCase (member)

**Location**: `vn.thanhnd.demo.application.usecase.member.getdetail.GetDetailUseCase`

**Flow**:

1. Build cache key: `ApplicationConstants.CACHE_MEMBER_DETAIL + memberId`
2. Cache hit → return cached `Member`; cache miss → fetch from DB via `MemberAdapter`, store with TTL, return
3. Map `Member` → `MemberDetailResponse`

**Transaction**: `@Transactional(readOnly = true)`

```java
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDetailUseCase {
    private final MemberAdapter memberAdapter;
    private final CacheAdapter cacheAdapter;
    private final DateTimeParser dateTimeParser;

    public MemberDetailResponse execute(String memberId) {
        String cacheKey = ApplicationConstants.CACHE_MEMBER_DETAIL + memberId;
        Cache cache = cacheAdapter.get(cacheKey, Cache.class);

        Member member;
        if (Objects.nonNull(cache) && cache.getData() instanceof Member cached) {
            member = cached;
        } else {
            member = memberAdapter.findById(memberId);
            LocalDateTime expiredAt = dateTimeParser.getCurrentUTCDateTime()
                .plusSeconds(ApplicationConstants.CACHE_MEMBER_DETAIL_EXPIRE_TIME);
            cacheAdapter.set(cacheKey, Cache.builder().key(cacheKey).expiredAt(expiredAt).data(member).build(),
                ApplicationConstants.CACHE_MEMBER_DETAIL_EXPIRE_TIME);
        }
        return MemberDetailResponse.of(member);
    }
}
```

---

## Write Use Case Example: CreateMemberUseCase

**Flow**:

1. DB-dependent validation (e.g. email uniqueness check)
2. Build new `Member` via `Member.builder()...build()`
3. Persist via `memberAdapter.createMember(member)`
4. Publish `MemberRegisteredEvent` for post-commit notifications

**Transaction**: `@Transactional`

```java
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateMemberUseCase {
    private final MemberAdapter memberAdapter;
    private final ApplicationEventPublisher eventPublisher;
    private final DateTimeParser dateTimeParser;

    @Transactional
    public ResultWrapper<Void> execute(CreateMemberRequest request) {
        return ResultHandler.handle(() -> {
            // DB-dependent validation only
            if (memberAdapter.existsByEmail(request.getEmail())) {
                throw new DomainValidationException("E-01-MEMBER-0001");
            }

            Member member = Member.builder()
                .memberName(request.getMemberName())
                .email(request.getEmail())
                // ... other fields ...
                .build();

            memberAdapter.createMember(member);

            // Publish event — listener fires AFTER_COMMIT
            eventPublisher.publishEvent(new MemberRegisteredEvent(
                member.id(), member.memberName(), dateTimeParser.getCurrentUTCDateTime()));
            return null;
        });
    }
}
```

---

## Export Use Case Pattern

Export use cases are **NOT** annotated with `@Transactional`. The `StreamingResponseBody` lambda runs outside the HTTP thread's transaction boundary, so each page is fetched in its own read-only transaction via `TransactionTemplate`.

### CSV export (`ExportMembersCsvUseCase`)

```java
@UseCase
@RequiredArgsConstructor
@Log4j2
public class ExportMembersCsvUseCase {

    private final MemberAdapter memberAdapter;
    private final DateTimeParser dateTimeParser;

    @Qualifier("readOnlyTransactionTemplate")
    private final TransactionTemplate readOnlyTransactionTemplate;

    private static final char BOM = '\uFEFF';   // UTF-8 BOM for Excel compatibility
    private static final String CSV_HEADER = "Col1,Col2,Col3";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public ResponseEntity<StreamingResponseBody> execute(ExportRequest request) {
        String timestamp = dateTimeParser.getCurrentJapanDateTime().format(TIMESTAMP_FORMATTER);

        StreamingResponseBody body = outputStream -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                writer.write(BOM);
                writer.write(CSV_HEADER + "\n");
                writer.flush();

                int page = 0;
                boolean hasMore = true;
                while (hasMore) {
                    final int currentPage = page;
                    List<Member> rows = readOnlyTransactionTemplate.execute(
                        status -> memberAdapter.exportMembers(request.getFilter(), currentPage, PAGE_SIZE));
                    if (rows == null || rows.isEmpty()) break;
                    for (Member m : rows) writeCsvRow(writer, m);
                    writer.flush();
                    if (rows.size() < PAGE_SIZE) hasMore = false;
                    else page++;
                }
            } catch (IOException e) {
                throw new CoreException("Error streaming CSV", e);
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"export-" + timestamp + ".csv\"");
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
```

### Excel export (`ExportOrganizationMembersExcelUseCase`)

Same shape as CSV but uses Apache POI `SXSSFWorkbook` for memory-efficient streaming:

```java
StreamingResponseBody body = outputStream ->
    writeExcelToStream(outputStream, organizationIds, onlyApprovers);

private void writeExcelToStream(OutputStream out, List<String> orgIds, boolean onlyApprovers) {
    // SXSSFWorkbook keeps only ROW_ACCESS_WINDOW_SIZE rows in memory at a time
    try (SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE)) {
        Sheet sheet = workbook.createSheet("Data");
        int rowIndex = writeHeaderRow(sheet);   // returns 1

        int page = 0;
        boolean hasMore = true;
        while (hasMore) {
            if (rowIndex > MAX_EXPORT_ROWS) {
                // write warning row and stop
                hasMore = false;
            } else {
                final int currentPage = page;
                SearchResult result = readOnlyTransactionTemplate.execute(
                    status -> adapter.findPage(orgIds, onlyApprovers, currentPage, PAGE_SIZE));
                if (result.items().isEmpty()) {
                    hasMore = false;
                } else {
                    rowIndex = writeDataRows(sheet, result.items(), rowIndex);
                    if (result.items().size() < PAGE_SIZE) hasMore = false;
                    else page++;
                }
            }
        }
        workbook.write(out);
        out.flush();
    } catch (IOException e) {
        throw new CoreException("Error streaming Excel", e);
    }
}
```

**Excel filename — Unicode support:**

```java
String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
headers.set(HttpHeaders.CONTENT_DISPOSITION,
    "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);
```

**Key rules for all export use cases:**

- No `@Transactional` on the class or method — use `TransactionTemplate` inside the lambda
- Always inject `@Qualifier("readOnlyTransactionTemplate") TransactionTemplate`
- Filename timestamp: `dateTimeParser.getCurrentJapanDateTime()` (Japan time)
- CSV: write UTF-8 BOM first for Excel compatibility
- Excel: `SXSSFWorkbook` with window size = page size; enforce `MAX_EXPORT_ROWS` hard limit

---

## Application Helper Pattern

For shared cache-aside logic reused across multiple use cases. Prevents use-case-to-use-case coupling.

**Interface** (application layer — no framework annotations):

```java
public interface ItemMasterCacheHelper {
    List<ItemMasterByTypeResponse> getByType(String type);
}
```

**Implementation** — `@Component`, NOT `@UseCase`, NOT `@Transactional`:

```java
@Component
@RequiredArgsConstructor
public class ItemMasterCacheHelperImpl implements ItemMasterCacheHelper {
    private final ItemMasterAdapter itemMasterAdapter;
    private final CacheAdapter cacheAdapter;
    private final DateTimeParser dateTimeParser;

    @Override
    public List<ItemMasterByTypeResponse> getByType(String type) {
        String cacheKey = ApplicationConstants.CACHE_ITEM_MASTER_BY_TYPE + type;
        Cache cache = cacheAdapter.get(cacheKey, Cache.class);

        List<ItemMaster> itemMasters;
        if (cache != null && cache.getData() instanceof ItemMasterList(List<ItemMaster> cached)) {
            itemMasters = cached;
        } else {
            itemMasters = itemMasterAdapter.findAllActiveByType(type);
            LocalDateTime expiredAt = dateTimeParser.getCurrentUTCDateTime()
                .plusSeconds(ApplicationConstants.CACHE_ITEM_MASTER_BY_TYPE_EXPIRE_TIME);
            cacheAdapter.set(cacheKey,
                Cache.builder().key(cacheKey).expiredAt(expiredAt).data(ItemMasterList.of(itemMasters)).build(),
                ApplicationConstants.CACHE_ITEM_MASTER_BY_TYPE_EXPIRE_TIME);
        }
        return itemMasters.stream().map(ItemMasterByTypeResponse::of).toList();
    }
}
```

Rules:

- `@Component` — stateless helper, not a transactional boundary
- Transaction belongs to the calling use case
- Inject the interface, not the implementation
- Location: `usecase/{domain}/helper/` + `usecase/{domain}/helper/impl/`

---

## Event Pattern (post-commit side-effects)

Used when a side-effect (e.g. email notification) must run only after the business transaction commits.

**Event** — plain POJO, no Spring/framework dependencies:

```java
@Getter
@RequiredArgsConstructor
public class AdministratorRegisteredEvent {
    private final String registeredAdministratorId;
    private final String registeredUserName;
    private final LocalDateTime registeredAt;
}
```

**Listener** — `@Component`, fires `AFTER_COMMIT`:

```java
@Component
@RequiredArgsConstructor
@Log4j2
public class AdministratorRegisteredEventListener {

    private final NotifySuperAdminsOfRegistrationUseCase notifyUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdministratorRegistered(AdministratorRegisteredEvent event) {
        try {
            notifyUseCase.execute(
                event.getRegisteredAdministratorId(),
                event.getRegisteredUserName(),
                event.getRegisteredAt());
        } catch (Exception e) {
            // Best-effort: notification failure must NOT affect the committed registration
            log.warn("Failed to send registration notification for user: {}",
                event.getRegisteredUserName(), e);
        }
    }
}
```

**Publishing** (inside a `@Transactional` use case):

```java
eventPublisher.publishEvent(new AdministratorRegisteredEvent(id, name, registeredAt));
```

Rules:

- Always catch all exceptions in the listener body — never let them propagate
- `AFTER_COMMIT` guarantees the event fires only on successful commit
- Location: `usecase/{domain}/event/`

---

## Domain Dependencies

| Type | Name | Purpose |
| ---- | ---- | ------- |
| Adapter | `MemberAdapter`, `MemberOrganizationAdapter` | Member CRUD and membership queries |
| Adapter | `OrganizationAdapter` | Organization CRUD + hierarchy |
| Adapter | `ApprovalWorkflowAdapter`, `ApprovalWorkflowStepAdapter` | Workflow + step CRUD |
| Adapter | `TicketAdapter` | Ticket + file + approval step persistence |
| Adapter | `ItemMasterAdapter` | Item master / lookup table queries |
| Adapter | `AuthenticationAdapter`, `CurrentUserAdapter` | Auth token and session context |
| Adapter | `CacheAdapter` | `get` / `set` / `delete` / `deleteByPattern` / `exists` |
| Adapter | `ObjectStorageAdapter` | MinIO file `put` / `get` / `delete` / presigned URL |
| Adapter | `MailSenderAdapter` | SMTP email sending |
| Adapter | `EmailTemplateRenderer` | Thymeleaf template → HTML string |
| Model | `Member`, `Administrator` | Core user entities |
| Model | `Organization`, `ApprovalWorkflow` | Org structure and approval workflows |
| Model | `Ticket`, `TicketFile`, `TicketApprovalStep` | Ticket entities |
| Model | `MailMessage` | Email payload |
| Enum | `GenderEnum`, `MarriedFlag`, `HasDependentFlag` etc. | Domain value objects |
| Exception | `DomainValidationException` | Caught by `ResultHandler` → `ResultWrapper.failure` |
| Record | `DomainError` | Error item inside `ResultWrapper.errors` |

## Infrastructure and util dependencies

| Type | Name | Purpose |
| ---- | ---- | ------- |
| Annotation | `@UseCase` | marks class as Spring component |
| Helper | `DateTimeParser` | date/time parsing and timezone conversion |
| Constant | `ApplicationConstants` | cache keys, expiration times |
| Spring | `ApplicationEventPublisher` | publish domain events |
| Spring | `TransactionTemplate` (`readOnlyTransactionTemplate`) | manual transaction in export lambdas |

---

## ResultWrapper and ResultHandler

Command-style write use cases that must return validation errors to the caller (without relying only on global exception handlers) return **`ResultWrapper<T>`** and wrap core logic in **`ResultHandler.handle(Supplier<T>)`**.

**`ResultWrapper<T>`** holds:

- `data` (`T`) — success payload (often `Void` for commands)
- `errors` (`List<DomainError>`) — validation / domain errors
- `isSuccess()` — checked by controllers

**`ResultHandler`** ( `vn.thanhnd.demo.application.base` ):

1. Runs the supplied block.
2. Catches **`DomainValidationException`** only → maps to `ResultWrapper.failure(...)` with `DomainError` entries (message = error code key, e.g. `E-02-ADMINISTRATOR-0005`; optional field on exception → `DomainError.field()`).
3. Any other exception propagates to presentation global handlers (e.g. `WebExceptionHandler`).

**Example shape** (see use cases such as `CreateAdministratorUseCase`, `UpdateAdministratorGeneralUseCase`, `Toggle2FAUseCase`):

```java
public ResultWrapper<Void> execute(SomeRequest request) {
  return ResultHandler.handle(() -> {
    // DB-dependent validation — throw DomainValidationException("E-02-ADMINISTRATOR-0005") on failure
    // persist
    return null;
  });
}
```

---

## Application-layer DTO mappers

Some responses use static `*.of(domain)` factories; occasional helpers live in **`vn.thanhnd.demo.application.mapper`** (e.g. `AdministratorMapper.toAuthorityResponse(Administrator)`). Prefer response DTO factories on the DTO class unless a shared mapper is clearly justified.
