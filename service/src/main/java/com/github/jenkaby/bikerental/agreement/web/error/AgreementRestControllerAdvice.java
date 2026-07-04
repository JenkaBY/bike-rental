package com.github.jenkaby.bikerental.agreement.web.error;

import com.github.jenkaby.bikerental.agreement.domain.exception.ActiveAgreementTemplateNotFoundException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotActivatableException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotDeletableException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementPdfRenderingException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotEditableException;
import com.github.jenkaby.bikerental.shared.web.advice.CorrelationIdProvider;
import com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jenkaby.bikerental.agreement")
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class AgreementRestControllerAdvice {

    private static final String CONCURRENT_ACTIVATION_ERROR_CODE = "agreement.template.concurrent_activation";

    private final CorrelationIdProvider correlationIdProvider;

    @ExceptionHandler(AgreementTemplateNotEditableException.class)
    public ResponseEntity<ProblemDetail> handleNotEditable(AgreementTemplateNotEditableException ex) {
        return conflict(ex.getMessage(), ex.getErrorCode(), ex.getDetails());
    }

    @ExceptionHandler(AgreementTemplateNotActivatableException.class)
    public ResponseEntity<ProblemDetail> handleNotActivatable(AgreementTemplateNotActivatableException ex) {
        return conflict(ex.getMessage(), ex.getErrorCode(), ex.getDetails());
    }

    @ExceptionHandler(AgreementTemplateNotDeletableException.class)
    public ResponseEntity<ProblemDetail> handleNotDeletable(AgreementTemplateNotDeletableException ex) {
        return conflict(ex.getMessage(), ex.getErrorCode(), ex.getDetails());
    }

    @ExceptionHandler(ActiveAgreementTemplateNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoActive(ActiveAgreementTemplateNotFoundException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] No active agreement template: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Active agreement template not found");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleConcurrentActivation(DataIntegrityViolationException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Concurrent agreement template activation lost the race: {}", correlationId, ex.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Concurrent activation");
        problem.setDetail("Another agreement template was activated concurrently. Please retry.");
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, CONCURRENT_ACTIVATION_ERROR_CODE);
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(AgreementPdfRenderingException.class)
    public ResponseEntity<ProblemDetail> handlePdfRendering(AgreementPdfRenderingException ex) {
        var correlationId = correlationIdProvider.resolve();
        log.error("[correlationId={}] Agreement PDF rendering failed: {}", correlationId, ex.getMessage(), ex);
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Agreement PDF rendering failed");
        problem.setDetail(ex.getMessage());
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, ex.getErrorCode());
        return ResponseEntity.of(problem).build();
    }

    private ResponseEntity<ProblemDetail> conflict(String detail, String errorCode, Object params) {
        var correlationId = correlationIdProvider.resolve();
        log.warn("[correlationId={}] Agreement template conflict [{}]: {}", correlationId, errorCode, detail);
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Agreement template conflict");
        problem.setDetail(detail);
        problem.setProperty(ProblemDetailField.CORRELATION_ID, correlationId);
        problem.setProperty(ProblemDetailField.ERROR_CODE, errorCode);
        problem.setProperty(ProblemDetailField.PARAMS, params);
        return ResponseEntity.of(problem).build();
    }
}
