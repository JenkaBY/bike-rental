package com.github.jenkaby.bikerental.shared.web.advice;

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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class CoreExceptionHandlerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleError(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(","));
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        log.warn("Bad request for MethodArgumentNotValidException: {}", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> handleError(MethodArgumentTypeMismatchException ex) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        log.warn("Bad request for MethodArgumentTypeMismatchException: {}", body);
        return ResponseEntity.of(body)
                .build();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ProblemDetail> handleError(HandlerMethodValidationException ex) {
        var errors = ex.getValueResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .map(message -> String.join(" ",
                                result.getMethodParameter().getParameterName(),
                                message)))
                .collect(Collectors.joining(","));
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        log.warn("Bad request for HandlerMethodValidationException: {}", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleError(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
                .map(violation -> "%s: %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                .collect(Collectors.joining(","));
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        log.warn("Bad request for ConstraintViolationException: {}", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleError(HttpMessageNotReadableException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "Malformed or missing request body";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        log.warn("Bad request for HttpMessageNotReadableException: {}", message);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidApiVersionException.class, MissingApiVersionException.class})
    ResponseEntity<ProblemDetail> handleError(ResponseStatusException ex) {
        log.warn("Invalid api version requested: ", ex);
        return new ResponseEntity<>(ex.getBody(), ex.getStatusCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleError(HttpRequestMethodNotSupportedException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "HTTP method not supported";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, message);
        log.warn("Method not allowed for HttpRequestMethodNotSupportedException: {}", message);
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleError(HttpMediaTypeNotSupportedException ex) {
        var message = ex.getMessage() != null ? ex.getMessage() : "Media type not supported";
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        log.warn("Unsupported media type for HttpMediaTypeNotSupportedException: {}", message);
        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleError(Exception ex) {
        var message = ex.getMessage();
        log.error("Unexpected {} was thrown: {}", ex.getClass(), message);
        log.debug("Unexpected error", ex);
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.of(body).build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("The resource '{}' not found by reason: {}", ex.getResourcePath(), ex.getMessage());
        return ResponseEntity.of(ex.getBody()).build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("The resource '{}[{}]' not found in DB", ex.getResourceName(), ex.getIdentifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReferenceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ReferenceNotFoundException ex) {
        log.warn("The referenced resource '{}[{}]' not found in DB", ex.getResourceName(), ex.getIdentifier());
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ResourceConflictException ex) {
        log.warn("The resource '{}[{}]' conflicts with other entity in DB", ex.getResourceName(), ex.getIdentifier());
        HttpStatus status = HttpStatus.CONFLICT;
        var body = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        return new ResponseEntity<>(body, status);
    }
}
