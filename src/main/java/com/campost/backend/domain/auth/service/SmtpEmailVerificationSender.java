package com.campost.backend.domain.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.mail.verification-sender",
        havingValue = "smtp"
)
public class SmtpEmailVerificationSender implements EmailVerificationSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailVerificationSender(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("CamPost 이메일 인증번호");
        message.setText("""
                CamPost 회원가입 이메일 인증번호입니다.

                인증번호: %s

                인증번호는 5분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code));

        mailSender.send(message);
    }
}
