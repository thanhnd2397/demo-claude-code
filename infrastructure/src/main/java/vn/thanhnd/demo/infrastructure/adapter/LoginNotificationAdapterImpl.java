package vn.thanhnd.demo.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import vn.thanhnd.demo.domain.adapter.EmailTemplateRenderer;
import vn.thanhnd.demo.domain.adapter.LoginNotificationPort;
import vn.thanhnd.demo.domain.adapter.MailSenderAdapter;
import vn.thanhnd.demo.domain.model.Administrator;
import vn.thanhnd.demo.domain.model.MailMessage;
import vn.thanhnd.demo.util.annotation.Adapter;

import java.time.LocalDateTime;
import java.util.Map;

@Adapter
@RequiredArgsConstructor
@Log4j2
public class LoginNotificationAdapterImpl implements LoginNotificationPort {

    private final MailSenderAdapter mailSenderAdapter;
    private final EmailTemplateRenderer emailTemplateRenderer;

    @Override
    @Async
    public void notifyLogin(Administrator administrator) {
        try {
            LocalDateTime loginTime = LocalDateTime.now();
            Map<String, Object> model = Map.of("username", administrator.username(), "loginTime", loginTime);
            String html = emailTemplateRenderer.render("email/login-notification", model);
            mailSenderAdapter.send(MailMessage.of(
                    administrator.email(),
                    "Dang nhap thanh cong",
                    "Tai khoan " + administrator.username() + " vua dang nhap luc " + loginTime,
                    null,
                    html));
        } catch (Exception e) {
            log.error("Failed to send login notification email for administrator {}", administrator.id(), e);
        }
    }
}
