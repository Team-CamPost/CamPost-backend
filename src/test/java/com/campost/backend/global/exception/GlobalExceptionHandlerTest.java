package com.campost.backend.global.exception;

import com.campost.backend.domain.auth.exception.InvalidEmailVerificationCodeException;
import com.campost.backend.domain.auth.exception.UnverifiedEmailException;
import com.campost.backend.global.api.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleConflictReturnsUsernameDuplicateResponseForUsernameConstraint() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"ux_users_username\""
        );

        ResponseEntity<ErrorResponse> response = handler.handleConflict(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("AUTH409_USERNAME");
        assertThat(response.getBody().message()).isEqualTo("이미 사용 중인 아이디입니다.");
    }

    @Test
    void handleConflictReturnsCommonConflictResponseForOtherConstraint() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"other_constraint\""
        );

        ResponseEntity<ErrorResponse> response = handler.handleConflict(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("COMMON409");
    }

    @Test
    void handleConflictReturnsEmailDuplicateResponseForEmailConstraint() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"ux_users_email\""
        );

        ResponseEntity<ErrorResponse> response = handler.handleConflict(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("AUTH409");
    }

    @Test
    void handleInvalidEmailVerificationCodeReturnsAuthBadRequestResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidEmailVerificationCode(
                new InvalidEmailVerificationCodeException()
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("AUTH400_EMAIL_VERIFICATION");
    }

    @Test
    void handleUnverifiedEmailReturnsAuthBadRequestResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleUnverifiedEmail(
                new UnverifiedEmailException()
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("AUTH400_EMAIL_VERIFICATION");
    }
}
