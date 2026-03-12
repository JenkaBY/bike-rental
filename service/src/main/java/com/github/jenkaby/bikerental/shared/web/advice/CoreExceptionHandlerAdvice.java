package com.github.jenkaby.bikerental.shared.web.advice;

import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceConflictException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes.*;
import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CoreExceptionHandlerAdvice {

    private final UuidGenerator uuidGenerator;
    private final ValidationErrorMapper validationErrorMapper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleError(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(validationErrorMapper::mapFieldError)
                .toList();

        var globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(validationErrorMapper::mapObjectError)
                .toList();

        var errors = new ArrayList<>();
        errors.addAll(fieldErrors);
        errors.addAll(globalErrors);

        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation error");
        var correlationId = resolveCorrelationId();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, METHOD_ARGUMENTS_VALIDATION_FAILED);
        body.setProperty(ERRORS, errors);
        log.warn("[correlationId={}] Bad request for MethodArgumentNotValidException", correlationId, ex);
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> handleError(MethodArgumentTypeMismatchException ex) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        var correlationId = resolveCorrelationId();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, METHOD_ARGUMENT_TYPE_MISMATCH);
        log.warn("[correlationId={}] Bad request for MethodArgumentTypeMismatchException: {}", correlationId, ex.getMessage());
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ProblemDetail> handleError(MissingServletRequestParameterException ex) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        var correlationId = resolveCorrelationId();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, REQUEST_PARAMS_MISSING);
        log.warn("[correlationId={}] Bad request for MissingServletRequestParameterException: {}", correlationId, ex.getMessage());
        return ResponseEntity.of(body).build();
    }

//    @ExceptionHandler(HandlerMethodValidationException.class)
//    ResponseEntity<ProblemDetail> handleError(HandlerMethodValidationException ex) {
//        var results = ex.getValueResults();
//        var detail = results.stream()
//                .flatMap(result -> result.getResolvableErrors().stream()
//                        .map(MessageSourceResolvable::getDefaultMessage)
//                        .map(message -> {
//                            String paramName = result.getMethodParameter().getParameterName();
//                            if (paramName == null) {
//                                PathVariable pv = result.getMethodParameter().getParameterAnnotation(PathVariable.class);
//                                if (pv != null && !pv.name().isBlank()) {
//                                    paramName = pv.name();
//                                } else if (pv != null && !pv.value().isBlank()) {
//                                    paramName = pv.value();
//                                } else {
//                                    paramName = "arg" + result.getMethodParameter().getParameterIndex();
//                                }
//                            }
//                            return String.join(" ", paramName, message);
//                        }))
//                .collect(Collectors.joining(","));
//
//        var errorsList = results.stream()
//                .flatMap(result -> result.getResolvableErrors().stream()
//                        .map(resolvable -> {
//                            String paramName = result.getMethodParameter().getParameterName();
//                            if (paramName == null) {
//                                PathVariable pv = result.getMethodParameter().getParameterAnnotation(PathVariable.class);
//                                paramName = (pv != null && !pv.name().isBlank()) ? pv.name()
//                                        : (pv != null && !pv.value().isBlank()) ? pv.value()
//                                        : "arg" + result.getMethodParameter().getParameterIndex();
//                            }
//                            String[] codes = resolvable.getCodes();
//                            String code = (codes != null && codes.length > 0) ? toValidationCode(codes[codes.length - 1]) : "";
//                            return Map.<String, String>of("field", paramName, "code", code);
//                        }))
//                .toList();
//
//        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation error");
//        var correlationId = resolveCorrelationId();
//        body.setProperty(CORRELATION_ID, correlationId);
//        body.setProperty(ERROR_CODE, VALIDATION_ERROR);
//        body.setProperty(ERRORS, errorsList);
//        log.warn("[correlationId={}] Bad request for HandlerMethodValidationException: {}", correlationId, detail);
//        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
//    }

    //  Occurs when validation failed in annotated @RequestParam, @PathVariable, @RequestHeader, @CookieValue, @ModelAttribute
    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ProblemDetail> handleError(HandlerMethodValidationException ex) {
        var results = ex.getValueResults();
        var errors = results.stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(this.validationErrorMapper::mapResolvableError)
                .toList();

        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation error");
        var correlationId = resolveCorrelationId();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, HANDLER_METHOD_ERROR);
        body.setProperty(ERRORS, errors);
        log.warn("[correlationId={}] Bad request for HandlerMethodValidationException", correlationId, ex);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleError(ConstraintViolationException ex) {
        var logDetail = ex.getConstraintViolations().stream()
                .map(violation -> "%s: %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                .collect(Collectors.joining(","));

        var errors = ex.getConstraintViolations().stream()
                .map(this.validationErrorMapper::toValidationError)
                .toList();

        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
        var correlationId = resolveCorrelationId();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, CONSTRAINT_VIOLATION);
        body.setProperty(ERRORS, errors);
        log.warn("[correlationId={}] Bad request for ConstraintViolationException: {}", correlationId, logDetail);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleError(HttpMessageNotReadableException ex) {
        var safeMessage = "Malformed or missing request body";
        var message = ex.getMessage() != null ? ex.getMessage() : safeMessage;
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, safeMessage);
        var correlationId = resolveCorrelationId();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, "shared.request.not_readable");
        log.warn("[correlationId={}] Bad request for HttpMessageNotReadableException: {}", correlationId, message);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidApiVersionException.class, MissingApiVersionException.class})
    ResponseEntity<ProblemDetail> handleError(ResponseStatusException ex) {
        var correlationId = resolveCorrelationId();
        var errorCode = ex instanceof MissingApiVersionException ? API_VERSION_MISSING : API_VERSION_INVALID;
        log.warn("[correlationId={}] Invalid api version requested", correlationId, ex);
        var body = ex.getBody();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, errorCode);
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleError(HttpRequestMethodNotSupportedException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "HTTP method not supported";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, message);
        var correlationId = resolveCorrelationId();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, "shared.request.method_not_allowed");
        log.warn("[correlationId={}] Method not allowed for HttpRequestMethodNotSupportedException: {}", correlationId, message);
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleError(HttpMediaTypeNotSupportedException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "Media type not supported";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        var correlationId = resolveCorrelationId();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, "shared.request.media_type_not_supported");
        log.warn("[correlationId={}] Unsupported media type for HttpMediaTypeNotSupportedException: {}", correlationId, message);
        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleError(Exception ex) {
        var correlationId = resolveCorrelationId();
        log.error("[correlationId={}] Unexpected {} was thrown: {}", correlationId, ex.getClass(), ex.getMessage());
        log.debug("[correlationId={}] Unexpected error", correlationId, ex);
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, INTERNAL_SERVER_ERROR);
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        var correlationId = resolveCorrelationId();
        var body = ex.getBody();
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, RESOURCE_NOT_FOUND);
        log.warn("[correlationId={}] The resource '{}' not found by reason: {}", correlationId, ex.getResourcePath(), ex.getMessage());
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex) {
        var correlationId = resolveCorrelationId();
        log.warn("[correlationId={}] The resource '{}[{}]' not found in DB", correlationId, ex.getResourceName(), ex.getIdentifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, ex.getErrorCode());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReferenceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleReferenceNotFoundException(ReferenceNotFoundException ex) {
        var correlationId = resolveCorrelationId();
        var details = ex.getDetails();
        log.warn("[correlationId={}] The referenced resource '{}[{}]' not found in DB", correlationId, details.resourceName(), details.identifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, ex.getErrorCode());
        body.setProperty(PARAMS, details);
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ResourceConflictException ex) {
        var correlationId = resolveCorrelationId();
        var details = ex.getDetails();
        log.warn("[correlationId={}] The resource '{}[{}]' conflicts with other entity in DB", correlationId, details.resourceName(), details.identifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, ex.getErrorCode());
        body.setProperty(PARAMS, details);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EquipmentNotAvailableException.class)
    public ResponseEntity<ProblemDetail> handleEquipmentNotAvailableException(EquipmentNotAvailableException ex) {
        var correlationId = resolveCorrelationId();
        var details = ex.getDetails();
        log.warn("[correlationId={}] Equipment {} is not available for operation. Current status: {}", correlationId, details.identifier(), details.status());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        body.setProperty(CORRELATION_ID, correlationId);
        body.setProperty(ERROR_CODE, ex.getErrorCode());
        body.setProperty(PARAMS, details);
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID);
        return correlationId != null ? correlationId : uuidGenerator.generate().toString();
    }
}
