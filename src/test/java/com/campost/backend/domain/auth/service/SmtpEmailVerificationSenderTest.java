package com.campost.backend.domain.auth.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(sentMessage.getSubject()).isEqualTo("CamPost 이메일 인증번호");
        assertThat(sentMessage.getText()).contains("123456");
        assertThat(sentMessage.getText()).contains("5분");
    }

    private static class FakeJavaMailSender implements JavaMailSender {

        private SimpleMailMessage sentMessage;

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
            this.sentMessage = simpleMessage;
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            this.sentMessage = simpleMessages[0];
        }
    }
}
