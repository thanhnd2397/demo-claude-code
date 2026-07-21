package vn.thanhnd.demo.infrastructure.adapter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import vn.thanhnd.demo.domain.adapter.MailSenderAdapter;
import vn.thanhnd.demo.domain.model.MailMessage;
import vn.thanhnd.demo.infrastructure.exception.MailSendException;
import vn.thanhnd.demo.util.annotation.Adapter;

@Adapter
@RequiredArgsConstructor
@Log4j2
public class MailSenderAdapterImpl implements MailSenderAdapter {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.from:}")
    private String defaultFrom;

    @Override
    public void send(MailMessage message) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(message.to());
            helper.setSubject(message.subject());

            String from = StringUtils.isNotBlank(message.from()) ? message.from() : defaultFrom;
            if (StringUtils.isNotBlank(from)) {
                helper.setFrom(from);
            }

            if (StringUtils.isNotBlank(message.htmlBody())) {
                helper.setText(message.textBody(), message.htmlBody());
            } else {
                helper.setText(message.textBody(), false);
            }

            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            throw new MailSendException("E-03-MAIL-0001", e);
        } catch (MessagingException e) {
            throw new MailSendException("E-03-MAIL-0002", e);
        }
    }
}
