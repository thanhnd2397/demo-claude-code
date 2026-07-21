package vn.thanhnd.demo.domain.adapter;

import java.util.Map;

/**
 * Port for rendering an email template (backed by Thymeleaf in infrastructure).
 * {@code templateName} is a logical name (e.g. {@code "email/register-notification"});
 * infrastructure resolves it to {@code classpath:/templates/{name}.html}.
 */
public interface EmailTemplateRenderer {

    String render(String templateName, Map<String, Object> model);
}
