package vn.thanhnd.demo.domain.adapter;

import vn.thanhnd.demo.domain.model.MailMessage;

/**
 * Port for sending email (backed by SMTP / JavaMailSender in infrastructure).
 */
public interface MailSenderAdapter {

    void send(MailMessage message);
}
