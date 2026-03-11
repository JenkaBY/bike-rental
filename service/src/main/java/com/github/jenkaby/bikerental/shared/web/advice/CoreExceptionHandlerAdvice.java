package com.github.jenkaby.bikerental.shared.web.advice;

import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

import java.util.Map;
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
                .map(error -> "%s: %s".formatted(error.getObjectName(), error.getDefaultMessage()));

        var logDetail = java.util.stream.Stream.concat(fieldErrors, globalErrors)
                .collect(Collectors.joining(","));

        if (logDetail.isBlank()) {
            logDetail = ex.getMessage();
        }

        var errorsList = ex.getBindingResult().getFieldErrors().stream()
                .<Map<String, String>>map(error -> Map.of(
                        error.getField(),
                        error.getDefaultMessage() != null ? error.getDefaultMessage() : ""))
                .toList();

        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation error");
        var correlationId = resolveCorrelationId();
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.request.validation_failed");
        body.setProperty("errors", errorsList);
        log.warn("[correlationId={}] Bad request for MethodArgumentNotValidException: {}", correlationId, logDetail);
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> handleError(MethodArgumentTypeMismatchException ex) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        var correlationId = resolveCorrelationId();
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.request.type_mismatch");
        log.warn("[correlationId={}] Bad request for MethodArgumentTypeMismatchException: {}", correlationId, ex.getMessage());
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ProblemDetail> handleError(MissingServletRequestParameterException ex) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        var correlationId = resolveCorrelationId();
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.request.param_missing");
        log.warn("[correlationId={}] Bad request for MissingServletRequestParameterException: {}", correlationId, ex.getMessage());
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ProblemDetail> handleError(HandlerMethodValidationException ex) {
        var results = ex.getValueResults();
        var detail = results.stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .map(message -> {
                            String paramName = result.getMethodParameter().getParameterName();
                            if (paramName == null) {
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

        var errorsList = results.stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .map(message -> {
                            String paramName = result.getMethodParameter().getParameterName();
                            if (paramName == null) {
                                PathVariable pv = result.getMethodParameter().getParameterAnnotation(PathVariable.class);
                                paramName = (pv != null && !pv.name().isBlank()) ? pv.name()
                                        : (pv != null && !pv.value().isBlank()) ? pv.value()
                                        : "arg" + result.getMethodParameter().getParameterIndex();
                            }
                            return Map.<String, String>of(paramName, message != null ? message : "");
                        }))
                .toList();

        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation error");
        var correlationId = resolveCorrelationId();
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.request.validation_failed");
        body.setProperty("errors", errorsList);
        log.warn("[correlationId={}] Bad request for HandlerMethodValidationException: {}", correlationId, detail);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleError(ConstraintViolationException ex) {
        var logDetail = ex.getConstraintViolations().stream()
                .map(violation -> "%s: %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                .collect(Collectors.joining(","));

        var errorsList = ex.getConstraintViolations().stream()
                .<Map<String, String>>map(violation -> Map.of(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();

        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation error");
        var correlationId = resolveCorrelationId();
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.request.constraint_violation");
        body.setProperty("errors", errorsList);
        log.warn("[correlationId={}] Bad request for ConstraintViolationException: {}", correlationId, logDetail);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleError(HttpMessageNotReadableException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "Malformed or missing request body";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        var correlationId = resolveCorrelationId();
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.request.not_readable");
        log.warn("[correlationId={}] Bad request for HttpMessageNotReadableException: {}", correlationId, message);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidApiVersionException.class, MissingApiVersionException.class})
    ResponseEntity<ProblemDetail> handleError(ResponseStatusException ex) {
        var correlationId = resolveCorrelationId();
        var errorCode = ex instanceof MissingApiVersionException
                ? "shared.api.version_missing"
                : "shared.api.version_invalid";
        log.warn("[correlationId={}] Invalid api version requested", correlationId, ex);
        var body = ex.getBody();
        if (body != null) {
            body.setProperty("correlationId", correlationId);
            body.setProperty("errorCode", errorCode);
            return new ResponseEntity<>(body, ex.getStatusCode());
        }
        var pd = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
        pd.setProperty("correlationId", correlationId);
        pd.setProperty("errorCode", errorCode);
        return new ResponseEntity<>(pd, ex.getStatusCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleError(HttpRequestMethodNotSupportedException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "HTTP method not supported";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, message);
        var correlationId = resolveCorrelationId();
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.request.method_not_allowed");
        log.warn("[correlationId={}] Method not allowed for HttpRequestMethodNotSupportedException: {}", correlationId, message);
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleError(HttpMediaTypeNotSupportedException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "Media type not supported";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        var correlationId = resolveCorrelationId();
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.request.media_type_not_supported");
        log.warn("[correlationId={}] Unsupported media type for HttpMediaTypeNotSupportedException: {}", correlationId, message);
        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleError(Exception ex) {
        var correlationId = resolveCorrelationId();
        log.error("[correlationId={}] Unexpected {} was thrown: {}", correlationId, ex.getClass(), ex.getMessage());
        log.debug("[correlationId={}] Unexpected error", correlationId, ex);
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.server.internal_error");
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        var correlationId = resolveCorrelationId();
        var body = ex.getBody();
        if (body == null) {
            body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", "shared.resource.not_found");
        log.warn("[correlationId={}] The resource '{}' not found by reason: {}", correlationId, ex.getResourcePath(), ex.getMessage());
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] The resource '{}[{}]' not found in DB", correlationId, ex.getResourceName(), ex.getIdentifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", ex.getErrorCode());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReferenceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleReferenceNotFoundException(ReferenceNotFoundException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] The referenced resource '{}[{}]' not found in DB", correlationId, ex.getResourceName(), ex.getIdentifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", ex.getErrorCode());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ResourceConflictException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] The resource '{}[{}]' conflicts with other entity in DB", correlationId, ex.getResourceName(), ex.getIdentifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", ex.getErrorCode());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EquipmentNotAvailableException.class)
    public ResponseEntity<ProblemDetail> handleEquipmentNotAvailableException(EquipmentNotAvailableException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] Equipment {} is not available for operation. Current status: {}", correlationId, ex.getEquipmentId(), ex.getCurrentStatus());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        body.setProperty("correlationId", correlationId);
        body.setProperty("errorCode", ex.getErrorCode());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}
