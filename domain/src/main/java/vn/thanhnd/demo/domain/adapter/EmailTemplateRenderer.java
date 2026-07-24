package vn.thanhnd.demo.domain.adapter;

import java.util.Map;

/**
 * Port for rendering an email template (backed by Thymeleaf in infrastructure).
 * {@code templateName} is a logical name (e.g. {@code "email/register-notification"});
 * infrastructure resolves it to {@code classpath:/templates/{name}.html}.
 */
public interface EmailTemplateRenderer {

    /**
     * Render a template to an HTML string.
     *
     * @param templateName The logical template name
     * @param model The variables available to the template
     * @return The rendered HTML
     */
    String render(String templateName, Map<String, Object> model);
}
