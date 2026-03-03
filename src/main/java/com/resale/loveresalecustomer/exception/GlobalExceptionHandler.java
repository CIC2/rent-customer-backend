package com.resale.loveresalecustomer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.resale.loveresalecustomer.utils.ReturnObject;

import java.sql.SQLIntegrityConstraintViolationException;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ReturnObject<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldErrors().get(0);

        ReturnObject<Object> result = new ReturnObject<>();
        result.setStatus(false);
        result.setMessage(error.getDefaultMessage());
        result.setData(null);

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            SQLIntegrityConstraintViolationException.class,
            DataIntegrityViolationException.class,
            TransactionSystemException.class,
            JpaSystemException.class
    })
    public ResponseEntity<ReturnObject<?>> handleDatabaseConstraint(Exception ex) {
        log.error("Database constraint or integrity violation", ex);

        // Unwrap the real cause
        Throwable rootCause = ex.getCause();
        while (rootCause != null && rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        String rootMessage = rootCause != null && rootCause.getMessage() != null
                ? rootCause.getMessage().toLowerCase()
                : "";

        String message = "A database constraint was violated. Please check your data and try again.";

        if (rootMessage.contains("data too long")) {
            message = "One or more fields exceed the allowed length. Please review your input.";
        } else if (rootMessage.contains("cannot be null") || rootMessage.contains("null value")) {
            message = "Some required fields are missing. Please make sure all mandatory fields are provided.";
        } else if (rootMessage.contains("duplicate") || rootMessage.contains("unique constraint")) {
            message = "A record with the same value already exists. Please use a unique value.";
        } else if (rootMessage.contains("foreign key")) {
            message = "Invalid reference. Please make sure related data (like project or customer) exists.";
        } else if (rootMessage.contains("constraint") && rootMessage.contains("project")) {
            message = "Project information is missing or invalid. Please include a valid project ID.";
        }

        ReturnObject<?> response = new ReturnObject<>(message, false, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ReturnObject<Object>> handleAuthenticationException(AuthenticationException ex) {
        ReturnObject<Object> result = new ReturnObject<>();
        result.setStatus(false);
        result.setMessage(ex.getMessage());
        result.setData(null);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<ReturnObject<?>> handleAccountNotVerified(AccountNotVerifiedException ex) {
        ReturnObject<?> response = new ReturnObject<>(
                ex.getMessage(),
                false,
                null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
