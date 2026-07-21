package vn.thanhnd.demo.domain.model;

/**
 * Email payload for {@link vn.thanhnd.demo.domain.adapter.MailSenderAdapter}.
 * No validation — infrastructure validates at send time.
 * {@code from} and {@code htmlBody} may be null; when {@code from} is null the adapter
 * uses the configured default sender (e.g. {@code spring.mail.from}).
 */
public record MailMessage(String to, String subject, String textBody, String from, String htmlBody) {

    public static MailMessage of(String to, String subject, String textBody) {
        return new MailMessage(to, subject, textBody, null, null);
    }

    public static MailMessage of(String to, String subject, String textBody, String from, String htmlBody) {
        return new MailMessage(to, subject, textBody, from, htmlBody);
    }
}
