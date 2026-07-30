# Plan: Login bằng username hoặc email + Gửi mail thông báo khi login thành công

## Document metadata

| Field | Value |
|-------|-------|
| Created date | 2026/07/29 |
| Created by | Claude Code |
| Approved date | 2026/07/29 |
| Approved by | thanhnd |
| Spec ref | Adhoc — no spec doc |
| Version | 1.0 |

## Context

Hiện tại `LoginUseCase` chỉ tìm administrator bằng `findByUsername(request.username())`. Yêu cầu:
1. Cho phép đăng nhập bằng **username hoặc email** (cùng một field `username` trong `LoginRequest`, giữ nguyên tên field để không breaking change API).
2. Gửi **mail thông báo bất đồng bộ** mỗi khi đăng nhập **thành công** (không gửi khi thất bại — theo quyết định của user).

Codebase có sẵn nhưng **chưa dùng đến**: `MailSenderAdapter` (port) / `MailSenderAdapterImpl`, `EmailTemplateRenderer` / `EmailTemplateRendererImpl` (Thymeleaf, engine bean `emailTemplateEngine`), `MailMessage` domain model. Chưa có file template `.html` nào, chưa có `@EnableAsync`/hạ tầng async nào trong project — đây sẽ là lần đầu bổ sung.

---

## Phần 1 — Login bằng username hoặc email

### 1.1 Domain port: `AdministratorRepositoryPort`
File: `domain/src/main/java/vn/thanhnd/demo/domain/adapter/AdministratorRepositoryPort.java`

Thêm method mới (giữ nguyên `findByUsername` vì `RegisterAdministratorUseCase`/chỗ khác có thể vẫn cần):

```java
Optional<Administrator> findByUsernameOrEmail(String identifier);
```

### 1.2 Infra: JPA repository
File: `infrastructure/src/main/java/vn/thanhnd/demo/infrastructure/persistence/repository/AdministratorJpaRepository.java`

Thêm:
```java
Optional<AdministratorEntity> findByUsernameOrEmail(String username, String email);
```

### 1.3 Infra: `AdministratorRepositoryAdapterImpl`
Implement port mới, truyền cùng 1 giá trị cho cả 2 tham số.

### 1.4 `LoginUseCase`
Đổi lookup từ `findByUsername` sang `findByUsernameOrEmail`. Giữ nguyên `LoginRequest` (field `username`), giữ nguyên cache key `cacheKeyAdministratorLoginFailures(request.username())`.

**Verify**: `POST /auth/login` bằng username thật → OK; bằng email thật (cùng tài khoản) → OK; giá trị không tồn tại → vẫn lỗi `E-01-ADMINISTRATOR-0003`.

---

## Phần 2 — Gửi mail thông báo khi login thành công (bất đồng bộ)

### 2.1 Domain port mới: `LoginNotificationPort`
File mới: `domain/src/main/java/vn/thanhnd/demo/domain/adapter/LoginNotificationPort.java` — `void notifyLogin(Administrator administrator)`.

### 2.2 Infra adapter: `LoginNotificationAdapterImpl`
File mới: `infrastructure/src/main/java/vn/thanhnd/demo/infrastructure/adapter/LoginNotificationAdapterImpl.java` — `@Async`, dùng `MailSenderAdapter` + `EmailTemplateRenderer`, bắt toàn bộ exception nội bộ và chỉ log (không được propagate ra ngoài vì chạy trên thread riêng).

### 2.3 Bật Async
File mới: `infrastructure/src/main/java/vn/thanhnd/demo/infrastructure/config/AsyncConfiguration.java` — `@Configuration @EnableAsync`, dùng executor mặc định của Spring.

Lưu ý: `@Async` chỉ hoạt động qua Spring proxy — không được self-invocation, nên `LoginUseCase` phải gọi qua interface `LoginNotificationPort` (bean riêng).

### 2.4 Template email
File mới: `infrastructure/src/main/resources/templates/email/login-notification.html` — Thymeleaf tối giản, biến `username`, `loginTime`.

### 2.5 `LoginUseCase` — gọi notification sau khi login thành công
Thêm `LoginNotificationPort` vào constructor, gọi `loginNotificationPort.notifyLogin(administrator)` trước khi `return` `LoginResponse`. Không cần try/catch ở `LoginUseCase` vì lời gọi `@Async` không throw ra ngoài.

**Verify**: login thành công → response trả ngay, email đến sau; login thất bại → không gửi mail; SMTP down → login vẫn 200 OK, chỉ log ERROR.

---

## Phần 3 — Cập nhật tài liệu

File: `docs/architecture/project-overview.md` — §1 (ghi nhận `@EnableAsync` lần đầu), §3 (LoginUseCase depends thêm `LoginNotificationPort`, purpose nêu "by username or email"), §4 (thêm node `LoginNotificationPort`/`LoginNotificationAdapterImpl` vào diagram).

---

## Danh sách file thay đổi/tạo mới

| Loại | File |
| --- | --- |
| Sửa | `domain/.../adapter/AdministratorRepositoryPort.java` |
| Sửa | `infrastructure/.../repository/AdministratorJpaRepository.java` |
| Sửa | `infrastructure/.../adapter/AdministratorRepositoryAdapterImpl.java` |
| Sửa | `application/.../usecase/administrator/LoginUseCase.java` |
| Mới | `domain/.../adapter/LoginNotificationPort.java` |
| Mới | `infrastructure/.../adapter/LoginNotificationAdapterImpl.java` |
| Mới | `infrastructure/.../config/AsyncConfiguration.java` |
| Mới | `infrastructure/src/main/resources/templates/email/login-notification.html` |
| Sửa | `docs/architecture/project-overview.md` (§1, §3, §4) |

## Build & Verify

```bash
./mvnw clean compile -P=local
```
Test end-to-end qua `docker compose up`: login bằng username, rồi bằng email của cùng tài khoản — cả hai thành công và nhận 1 email mỗi lần.
