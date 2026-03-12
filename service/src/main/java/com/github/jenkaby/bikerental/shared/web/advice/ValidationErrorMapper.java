package com.github.jenkaby.bikerental.shared.web.advice;

import com.github.jenkaby.bikerental.shared.web.response.ValidationError;
import jakarta.validation.ConstraintViolation;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public interface ValidationErrorMapper {

    ValidationError toValidationError(ConstraintViolation<?> violation);

    ValidationError mapFieldError(FieldError fieldError);

    ValidationError mapObjectError(ObjectError objectError);

    ValidationError mapResolvableError(MessageSourceResolvable resolvable);
}
