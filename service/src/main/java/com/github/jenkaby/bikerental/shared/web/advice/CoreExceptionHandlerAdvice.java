package com.github.jenkaby.bikerental.shared.web.advice;

import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class CoreExceptionHandlerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleError(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()));

        var globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(error -> {
                    var objectName = error.getObjectName();
                    return "%s: %s".formatted(objectName, error.getDefaultMessage());
                });

        var errors = java.util.stream.Stream.concat(fieldErrors, globalErrors)
                .collect(Collectors.joining(","));

        if (errors.isBlank()) {
            errors = ex.getMessage();
        }

        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        var errorId = UUID.randomUUID();
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] Bad request for MethodArgumentNotValidException: {}", errorId, errors);
        return ResponseEntity.of(body)
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> handleError(MethodArgumentTypeMismatchException ex) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        var errorId = UUID.randomUUID();
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] Bad request for MethodArgumentTypeMismatchException: {}", errorId, ex.getMessage());
        return ResponseEntity.of(body)
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ProblemDetail> handleError(MissingServletRequestParameterException ex) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        var errorId = UUID.randomUUID();
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] Bad request for MissingServletRequestParameterException: {}", errorId, ex.getMessage());
        return ResponseEntity.of(body)
                .build();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ProblemDetail> handleError(HandlerMethodValidationException ex) {
        var errors = ex.getValueResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .map(message -> {
                            String paramName = result.getMethodParameter().getParameterName();
                            if (paramName == null) {
                                // try to read from @PathVariable or fallback to arg index
                                PathVariable pv = result.getMethodParameter().getParameterAnnotation(PathVariable.class);
                                if (pv != null && !pv.name().isBlank()) {
                                    paramName = pv.name();
                                } else if (pv != null && !pv.value().isBlank()) {
                                    paramName = pv.value();
                                } else {
                                    paramName = "arg" + result.getMethodParameter().getParameterIndex();
                                }
                            }
                            return String.join(" ", paramName, message);
                        }))
                .collect(Collectors.joining(","));
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        var errorId = UUID.randomUUID();
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] Bad request for HandlerMethodValidationException: {}", errorId, errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleError(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
                .map(violation -> "%s: %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                .collect(Collectors.joining(","));
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        var errorId = UUID.randomUUID();
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] Bad request for ConstraintViolationException: {}", errorId, errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleError(HttpMessageNotReadableException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "Malformed or missing request body";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        var errorId = UUID.randomUUID();
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] Bad request for HttpMessageNotReadableException: {}", errorId, message);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidApiVersionException.class, MissingApiVersionException.class})
    ResponseEntity<ProblemDetail> handleError(ResponseStatusException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Invalid api version requested", errorId, ex);
        var body = ex.getBody();
        if (body != null) {
            body.setProperty("errorId", errorId);
            return new ResponseEntity<>(body, ex.getStatusCode());
        }
        var pd = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
        pd.setProperty("errorId", errorId);
        return new ResponseEntity<>(pd, ex.getStatusCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleError(HttpRequestMethodNotSupportedException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "HTTP method not supported";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, message);
        var errorId = UUID.randomUUID();
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] Method not allowed for HttpRequestMethodNotSupportedException: {}", errorId, message);
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleError(HttpMediaTypeNotSupportedException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "Media type not supported";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        var errorId = UUID.randomUUID();
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] Unsupported media type for HttpMediaTypeNotSupportedException: {}", errorId, message);
        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleError(Exception ex) {
        var message = ex.getMessage();
        var errorId = UUID.randomUUID();
        log.error("[errorId={}] Unexpected {} was thrown: {}", errorId, ex.getClass(), message);
        log.debug("[errorId={}] Unexpected error", errorId, ex);
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        body.setProperty("errorId", errorId);
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        var errorId = UUID.randomUUID();
        var body = ex.getBody();
        if (body == null) {
            body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        body.setProperty("errorId", errorId);
        log.warn("[errorId={}] The resource '{}' not found by reason: {}", errorId, ex.getResourcePath(), ex.getMessage());
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] The resource '{}[{}]' not found in DB", errorId, ex.getResourceName(), ex.getIdentifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        body.setProperty("errorId", errorId);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReferenceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ReferenceNotFoundException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] The referenced resource '{}[{}]' not found in DB", errorId, ex.getResourceName(), ex.getIdentifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        body.setProperty("errorId", errorId);
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ResourceConflictException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] The resource '{}[{}]' conflicts with other entity in DB", errorId, ex.getResourceName(), ex.getIdentifier());
        HttpStatus status = HttpStatus.CONFLICT;
        var body = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        body.setProperty("errorId", errorId);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(EquipmentNotAvailableException.class)
    public ResponseEntity<ProblemDetail> handleEquipmentNotAvailableException(EquipmentNotAvailableException ex) {
        var errorId = UUID.randomUUID();
        log.warn("[errorId={}] Equipment {} is not available for operation. Current status: {}", errorId, ex.getEquipmentId(), ex.getCurrentStatus());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        body.setProperty("errorId", errorId);
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
