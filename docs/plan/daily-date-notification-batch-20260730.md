# Daily date notification email batch (15:00 cron)

## Context

Cần một batch job chạy tự động mỗi ngày lúc 15:00, gửi email cho toàn bộ Administrator đang `ACTIVE`, thông báo hôm nay là ngày bao nhiêu (ví dụ: "Hôm nay là ngày 30/07/2026"). Đây là tính năng nội bộ (không có endpoint gọi thủ công), dùng Spring `@Scheduled`. Sau khi trao đổi với user:
- Đối tượng nhận: tất cả `Administrator` có `status = ACTIVE` (không cần domain User mới).
- Kênh: chỉ email (bỏ push notification — dự án chưa có cơ chế lưu device token, để scope sau).
- Cơ chế lịch: Spring `@Scheduled` cron trong app (chưa có `@EnableScheduling` nào trong codebase — cần thêm mới).

Tính năng tái sử dụng tối đa hạ tầng email đã có từ login-notification feature (`MailSenderAdapter`, `EmailTemplateRenderer`, Thymeleaf template pattern, `@Async` fire-and-forget adapter pattern) — xem `LoginNotificationAdapterImpl`/`LoginNotificationPort` làm tham chiếu.

## Approach

Theo đúng pattern hiện có (`LoginNotificationPort` → `LoginNotificationAdapterImpl` → `MailSenderAdapter`/`EmailTemplateRenderer`):

1. **Domain port** — `domain/src/main/java/vn/thanhnd/demo/domain/adapter/DailyDateNotificationPort.java`
   ```java
   public interface DailyDateNotificationPort {
       void notifyDailyDate(Administrator administrator, LocalDate today);
   }
   ```
   JavaDoc note: fire-and-forget, implementation runs async, không propagate lỗi.

2. **Infrastructure adapter** — `infrastructure/src/main/java/vn/thanhnd/demo/infrastructure/adapter/DailyDateNotificationAdapterImpl.java`
   - `@Adapter @RequiredArgsConstructor @Log4j2`, method `@Async @Override notifyDailyDate(...)`.
   - Format ngày bằng `DateTimeFormatter.ofPattern("dd/MM/yyyy")` (inline, không thêm hằng số mới vì chỉ dùng ở đây).
   - Render template `email/daily-date-notification` với model `Map.of("username", administrator.username(), "todayFormatted", formattedDate)`.
   - Gửi qua `mailSenderAdapter.send(MailMessage.of(administrator.email(), "Thong bao ngay hom nay", "Hom nay la ngay " + formattedDate, null, html))`.
   - Toàn bộ body trong try/catch, chỉ log lỗi — **giống hệt** `LoginNotificationAdapterImpl`, không throw ra ngoài.

3. **Email template** — `infrastructure/src/main/resources/templates/email/daily-date-notification.html`
   - Cùng cấu trúc self-contained HTML như `login-notification.html`, dùng `th:text="${todayFormatted}"` và `th:text="${username}"`.

4. **Scheduling config** — file mới `infrastructure/src/main/java/vn/thanhnd/demo/infrastructure/config/SchedulingConfiguration.java`
   ```java
   @Configuration
   @EnableScheduling
   public class SchedulingConfiguration {
   }
   ```
   Tách riêng khỏi `AsyncConfiguration` (giữ nguyên file đó, không đổi) vì đây là concern khác (`@EnableAsync` cho fire-and-forget email, `@EnableScheduling` cho cron trigger) — theo đúng convention 1-file-1-concern đã thấy ở `EmailTemplateConfiguration`.

5. **Use case (nơi đặt `@Scheduled`)** — `application/src/main/java/vn/thanhnd/demo/application/usecase/administrator/SendDailyDateNotificationUseCase.java`
   - `@UseCase`, constructor injection `AdministratorRepositoryPort` + `DailyDateNotificationPort` (theo đúng style `LoginUseCase`, không dùng Lombok).
   - `@Scheduled(cron = "${app.notification.daily-date-cron:0 0 15 * * *}")`
   - `@Transactional(readOnly = true)`
   - Method `void sendDailyDateNotifications()`:
     - `administratorRepositoryPort.findAll()` rồi filter `status() == AdministratorStatus.ACTIVE` trong use case (không thêm `findByStatus` mới vào port/JPA — bảng admin nhỏ, filter in-memory đơn giản hơn, đúng nguyên tắc "không thêm abstraction chưa cần").
     - Loop từng admin ACTIVE, gọi `dailyDateNotificationPort.notifyDailyDate(admin, LocalDate.now())` (mỗi call async, fire-and-forget).
   - **Không** dùng `ResultWrapper`/`ResultHandler` — pattern đó dành cho command use case có HTTP caller đọc `isSuccess()`/`getErrors()`; job scheduled không có caller nào tiêu thụ kết quả. Lỗi từng admin đã được nuốt/log ở tầng adapter (bước 2); nếu `findAll()` (DB) lỗi, để exception nổi lên — Spring's default `@Scheduled` error handling sẽ log, không cần try/catch thủ công thêm.

6. **Cron cấu hình** — thêm 1 dòng vào `web/src/main/resources/application.properties` (base, áp dụng mọi profile trừ khi override):
   ```properties
   app.notification.daily-date-cron=0 0 15 * * *
   ```
   Đặt property (không hardcode literal trong code) để có thể đổi giờ chạy mà không cần build lại — nhất quán với các config khác (`app.jwt.*`, `datasource.replica.*`).

## Files touched

**New:**
- `domain/.../adapter/DailyDateNotificationPort.java`
- `infrastructure/.../adapter/DailyDateNotificationAdapterImpl.java`
- `infrastructure/.../config/SchedulingConfiguration.java`
- `infrastructure/src/main/resources/templates/email/daily-date-notification.html`
- `application/.../usecase/administrator/SendDailyDateNotificationUseCase.java`

**Modified:**
- `web/src/main/resources/application.properties` — add `app.notification.daily-date-cron`
- `docs/architecture/project-overview.md` — §3 add new use case row (no HTTP endpoint, trigger = cron), §4 add `SendDailyDateNotificationUseCase → AdministratorRepositoryPort` + `-.async.-> DailyDateNotificationPort → DailyDateNotificationAdapterImpl → MailSenderAdapter/EmailTemplateRenderer` edges, §8 known gaps unaffected (no new gap introduced)
- `docs/architecture/architecture-map.html` — mirror the above (`modules`, `graphNodes`/`graphEdges` with `newItem: true`)

## Not doing (explicitly out of scope, per user's answers)
- No push notification (FCM/WebPush) — no port, no device-token storage.
- No new `User`/`Customer` domain — recipients are existing `Administrator` records.
- No manual-trigger HTTP endpoint.
- No i18n for the email content (matches existing `login-notification.html` which hardcodes Vietnamese/ASCII text directly, no `MessageSource` involvement).

## Verification
1. `./mvnw clean compile -P=local` — must pass with no errors after each file is added (per `implement-mode.md` build-after-every-change rule).
2. Unit-level sanity: temporarily point `app.notification.daily-date-cron` to a near-future cron (e.g. next minute) while running locally with `local` profile + MailHog/mailcatcher on `localhost:1025` (already configured in `application-local.properties`), start the app, wait for the trigger, confirm an email arrives in the mail catcher UI with correct date text — then revert the cron value back to `0 0 15 * * *`.
3. Confirm only `ACTIVE` administrators receive the email: seed one `ACTIVE` and one `INACTIVE`/`LOCKED` admin in local DB, run the trigger, verify only the `ACTIVE` one gets an email (via mail catcher log).
4. Confirm a single admin's email failure (e.g. temporarily give one admin an invalid email format bypassing validation, or stop mail server mid-loop) doesn't stop the whole batch — other admins still receive their email (validates the per-admin try/catch-and-log in the adapter).
5. Re-read `docs/architecture/project-overview.md` and `architecture-map.html` side by side after edits to confirm they still describe the same state (per `implement-mode.md` guardrail).
