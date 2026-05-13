package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.exception.EmailVerificationSendException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmtpEmailVerificationSenderTest {

    private final FakeJavaMailSender mailSender = new FakeJavaMailSender();
    private final SmtpEmailVerificationSender sender =
            new SmtpEmailVerificationSender(mailSender, "no-reply@campost.local");

    @Test
    void sendCreatesVerificationEmailMessage() {
        sender.send("user@example.com", "123456");

        SimpleMailMessage sentMessage = mailSender.sentMessage;

        assertThat(sentMessage).isNotNull();
        assertThat(sentMessage.getFrom()).isEqualTo("no-reply@campost.local");
        assertThat(sentMessage.getTo()).containsExactly("user@example.com");
        assertThat(sentMessage.getSubject()).isNotBlank();
        assertThat(sentMessage.getText()).contains("123456");
        assertThat(sentMessage.getText()).contains(String.valueOf(EmailVerificationPolicy.CODE_TTL_MINUTES));
    }

    @Test
    void sendThrowsBusinessExceptionWhenMailSendFails() {
        mailSender.failToSend = true;

        assertThatThrownBy(() -> sender.send("user@example.com", "123456"))
                .isInstanceOf(EmailVerificationSendException.class)
                .hasCauseInstanceOf(MailSendException.class);
    }

    private static class FakeJavaMailSender implements JavaMailSender {

        private SimpleMailMessage sentMessage;
        private boolean failToSend;

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(Session.getInstance(new Properties()));
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            if (failToSend) {
                throw new MailSendException("SMTP server unavailable.");
            }

            this.sentMessage = simpleMessage;
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            if (simpleMessages.length == 0) {
                throw new IllegalArgumentException("At least one message is required.");
            }

            send(simpleMessages[0]);
        }
    }
}
