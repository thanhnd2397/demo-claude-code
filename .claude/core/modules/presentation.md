# Presentation Layer — Detailed Reference

## Full Controller Example: AdministratorManagementController

Demonstrates the complete view controller pattern with HTMX, ResultWrapper, and redirect handling.

```java
@Controller
@RequiredArgsConstructor
@RequestMapping("/administrator-management")
public class AdministratorManagementController {

    private final SearchAdministratorUseCase searchUseCase;
    private final UpdateAdministratorUseCase updateUseCase;
    private final ResponseHelper responseHelper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin-view', 'super-admin')")
    public String list(@ModelAttribute SearchRequest request, Model model) {
        model.addAttribute("results", searchUseCase.execute(request));
        return "administrator/list";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('admin-edit', 'super-admin')")
    public Object update(
            @PathVariable String id,
            @Valid @ModelAttribute UpdateRequest request,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return responseHelper.resolveView(httpRequest,
                "administrator/update", "administrator/update :: general");
        }

        ResultWrapper<Void> result = updateUseCase.execute(request);
        if (!result.isSuccess()) {
            List<String> messages = result.getErrors().stream()
                .map(e -> messageSource.getMessage(e.errorCode(), null, Locale.getDefault()))
                .toList();
            model.addAttribute("errorMessages", messages);
            return responseHelper.resolveView(httpRequest,
                "administrator/update", "administrator/update :: general");
        }

        return responseHelper.handleRedirectWithSessionMessage(
            httpRequest, "/administrator-management", "Updated successfully", redirectAttributes);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('admin-delete', 'super-admin')")
    public Object delete(@PathVariable String id, HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {
        deleteUseCase.execute(id);
        return responseHelper.handleRedirectWithSessionMessage(
            httpRequest, "/administrator-management", "Deleted successfully", redirectAttributes);
    }
}
```

---

## Exception Handlers

### ApiExceptionHandler

```java
@RestControllerAdvice(basePackages = "vn.thanhnd.demo.presentation.api")
public class ApiExceptionHandler {
    // Returns ResponseEntity<RestResponse<T>> with appropriate HTTP status
    // Uses MessageSource for i18n error messages
}
```

### WebExceptionHandler

```java
@ControllerAdvice(basePackages = "vn.thanhnd.demo.presentation.view")
public class WebExceptionHandler {
    // HTMX requests: ResponseEntity<Void> with HX-Redirect header
    // Non-HTMX requests: ModelAndView for error pages
    // Uses ResponseHelper for HTMX detection
}
```

---

## Filter Details

### JwtAuthenticationFilter

- Processes only `/api/**` requests
- Extracts Bearer token from `Authorization` header
- Validates JWT, loads authorities via `GetAuthorityUseCase`
- Sets `SecurityContextHolder` with user authorities

### ApiLoginFilter (`POST /api/v1/auth/login`)

- Extends `AbstractAuthenticationProcessingFilter`
- On success: generates JWT + refresh token, updates login info, returns JSON
- On failure: returns 401 JSON response

### SessionRefreshFilter

- Extends session expiration on every authenticated web request

---

## Security Configuration Details

### API Filter Chain (`@Order(1)`)

- Stateless (`SessionCreationPolicy.STATELESS`)
- JWT via `JwtAuthenticationFilter`
- CSRF disabled
- Security headers: `X-Frame-Options: DENY`, `nosniff`, `HSTS`, CSP
- Public: `/api/v1/auth/login`, `/api/v1/auth/refresh-token`

### Web Filter Chain (`@Order(2)`)

- Session-based authentication stored in Redis
- CSRF enabled
- Public: `/`, `/login`, `/register`, `/css/**`, `/js/**`, `/images/**`

---

## Transaction Routing (DatabaseConfiguration)

`@Transactional(readOnly = true)` → `TransactionSynchronizationManager.isCurrentTransactionReadOnly()` → replica.
`@Transactional` → primary.

Multiple replicas use round-robin. If all replicas are unhealthy, falls back to primary.
`ReplicaHealthChecker` periodically validates replicas; failed replicas enter cooldown.

Key properties:

- `datasource.replica.health-check-interval-ms`
- `datasource.replica.failover-cooldown-seconds`
- `datasource.replica.max-consecutive-failures`

---

## Async Configuration (AsyncConfiguration)

- Virtual threads enabled via `spring.threads.virtual.enabled=true`
- Automatic fallback to traditional thread pool if virtual threads fail
- Tomcat configured to use virtual threads for request handling
- Task decorator captures tracing + security context for async operations
- Metrics via Micrometer; health indicator included
