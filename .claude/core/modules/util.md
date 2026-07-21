# Util Layer — Detailed Reference

## DateTimeParserImpl

`@Component` implementing `DateTimeParser`. Inject the interface, not the implementation.

Key implementation details:

- Japan timezone: `ZoneId.of("Asia/Tokyo")` (GMT+9)
- Date format pattern: `ApplicationConstants.DATE_FORMAT`
- `getStartOfDay(date)` → `date.atTime(LocalTime.MIN)`
- `getEndOfDay(date)` → `date.atTime(LocalTime.MAX)`
- Null-safe: returns empty string for null `LocalDateTime` input

---

## ResponseMakerImpl

`@Component` implementing `ResponseMaker`. Inject the interface, not the implementation.

Uses Jackson `JsonMapper` for serialization. Sets response headers:

- `Content-Type: application/json`
- `charset=UTF-8`

`writeResponse(HttpServletResponse, RestResponse)` used in filters (before the controller chain).
Logs `IOException` on write failure rather than rethrowing.

---

## RestResponse Full Structure

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "service_name", "success", "errors", "data" })
public class RestResponse<T> {
    @JsonProperty("service_name") String serviceName;
    @JsonProperty("success")      Boolean success;
    @JsonProperty("errors")       List<ErrorModel> error;
    @JsonProperty("data")         T data;
}
```

```java
public class ErrorModel implements Serializable {
    @JsonProperty("error_code")    String errorCode;
    @JsonProperty("error_message") String errorMessage;
}
```

Usage:

```java
// In API controllers
RestResponse<MyData> ok  = responseMaker.createSuccessResponse("my-service", data);
RestResponse<Void>   err = responseMaker.createFailResponse("my-service", "E-01-X-0001", localizedMsg);

// In filters (write directly to HttpServletResponse)
responseMaker.writeResponse(httpServletResponse,
    responseMaker.createFailResponse("auth-service", "E-00-AUTH-0001", "Unauthorized"));
```

---

## ResponseHelper and RequestHelper implementations

Both are `@Component` beans implementing their respective interfaces.

**RequestHelperImpl**: checks `request.getHeader("HX-Request")` equals `"true"`.

**ResponseHelperImpl** decision table:

| Method | HTMX? | Returns |
| ------ | ----- | ------- |
| `resolveView(req, full, fragment)` | yes | `fragment` (String) |
| `resolveView(req, full, fragment)` | no | `full` (String) |
| `createRedirectResponse(req, url)` | yes | `ResponseEntity.ok().header("HX-Redirect", url).build()` |
| `createRedirectResponse(req, url)` | no | `null` |
| `handleRedirect(req, url, attrs)` | yes | calls `createRedirectResponse` |
| `handleRedirect(req, url, attrs)` | no | sets `RedirectAttributes` and returns `"redirect:" + url` |
| `handleRedirectWithSessionMessage(req, url, msg, attrs)` | yes | stores `msg` in session, returns `ResponseEntity` with `HX-Redirect` |
| `handleRedirectWithSessionMessage(req, url, msg, attrs)` | no | stores `msg` in flash, returns `"redirect:" + url` |

Session messages are read-once (cleared after first read) to avoid showing on page refresh.

---

## ApplicationConstants Key Categories

| Prefix | Examples |
| ------ | -------- |
| `CACHE_*` | `CACHE_ADMINISTRATOR_DETAIL`, `CACHE_ADMINISTRATOR_DETAIL_EXPIRE_TIME` |
| `DATE_FORMAT*` | date/time format strings used by `DateTimeParser` |
| `CONTENT_TYPE_*` | HTTP content type constants |

All fields are `public static final` in a final class with private constructor.

---

## CoreException

**Location:** `vn.thanhnd.demo.util.exception.CoreException`

Base **`RuntimeException`** for application-wide errors. Subclasses include infrastructure types such as `EntityNotFoundException`, `RedisException`, `StorageException`, etc.

```java
public class EntityNotFoundException extends CoreException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
```

Use a **stable message or code** suitable for mapping to i18n keys in higher layers.
