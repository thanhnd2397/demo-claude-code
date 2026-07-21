package vn.thanhnd.demo.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.thanhnd.demo.domain.adapter.EmailTemplateRenderer;
import vn.thanhnd.demo.util.annotation.Adapter;

import java.util.Map;

@Adapter
@RequiredArgsConstructor
public class EmailTemplateRendererImpl implements EmailTemplateRenderer {

    @Qualifier("emailTemplateEngine")
    private final TemplateEngine emailTemplateEngine;

    @Override
    public String render(String templateName, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return emailTemplateEngine.process(templateName, context);
    }
}
