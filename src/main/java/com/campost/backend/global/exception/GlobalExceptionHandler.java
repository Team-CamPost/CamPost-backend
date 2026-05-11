package com.campost.backend.global.exception;

import com.campost.backend.domain.auth.exception.DuplicatedEmailException;
import com.campost.backend.domain.auth.exception.DuplicatedUsernameException;
import com.campost.backend.global.api.ApiCode;
import com.campost.backend.global.api.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.NoSuchElementException;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        return toResponse(ApiCode.COMMON400);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return toResponse(ApiCode.COMMON400);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        return toResponse(ApiCode.COMMON400);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpiredToken(TokenExpiredException ex) {
        return toResponse(ApiCode.TOKEN401);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        return toResponse(ApiCode.TOKEN402);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(SecurityException ex) {
        return toResponse(ApiCode.AUTH403);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        return toResponse(ApiCode.COMMON404);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex) {
        if (containsMessage(ex, "ux_users_username")) {
            return toResponse(ApiCode.AUTH409_USERNAME);
        }

        return toResponse(ApiCode.COMMON409);
    }

    @ExceptionHandler(DuplicatedEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedEmail(DuplicatedEmailException ex) {
        return toResponse(ApiCode.AUTH409);
    }

    @ExceptionHandler(DuplicatedUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedUsername(DuplicatedUsernameException ex) {
        return toResponse(ApiCode.AUTH409_USERNAME);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return toResponse(ApiCode.SERVER500);
    }

    private ResponseEntity<ErrorResponse> toResponse(ApiCode code) {
        return ResponseEntity.status(Objects.requireNonNull(code.httpStatus()))
                .body(ErrorResponse.of(code));
    }

    private boolean containsMessage(Throwable throwable, String value) {
        Throwable current = throwable;

        while (current != null) {
            String message = current.getMessage();

            if (message != null && message.contains(value)) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
