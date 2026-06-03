package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.exception.EmailVerificationSendException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Sends email verification codes through the Resend HTTP API.
 *
 * <p>Render Free 웹 서비스는 SMTP 포트(25/465/587) outbound를 차단하므로, SMTP 대신
 * HTTPS(443) 기반 Resend API로 메일을 발송한다. 발신 주소(app.mail.from)는 Resend에서
 * 검증한 도메인 주소이거나, 테스트용 onboarding@resend.dev 여야 한다.
 */
@Component
@ConditionalOnProperty(
        name = "app.mail.verification-sender",
        havingValue = "resend"
)
public class ResendEmailVerificationSender implements EmailVerificationSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailVerificationSender.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 8_000;

    private final RestClient restClient;
    private final String from;

    public ResendEmailVerificationSender(
            @Value("${app.mail.resend.api-key}") String apiKey,
            @Value("${app.mail.from}") String from
    ) {
        this.from = from;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);

        this.restClient = RestClient.builder()
                .baseUrl(RESEND_API_URL)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void send(String email, String code) {
        Map<String, Object> payload = Map.of(
                "from", from,
                "to", List.of(email),
                "subject", "CamPost 이메일 인증번호",
                "text", buildText(code)
        );

        try {
            restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn(
                    "Failed to send email verification code via Resend. email={}, reason={}",
                    maskEmail(email),
                    exception.getMessage()
            );
            throw new EmailVerificationSendException(exception);
        }
    }

    private String buildText(String code) {
        return """
                CamPost 회원가입 이메일 인증번호입니다.

                인증번호: %s

                인증번호는 %d분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code, EmailVerificationPolicy.CODE_TTL_MINUTES);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(Math.max(atIndex, 0));
        }

        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
