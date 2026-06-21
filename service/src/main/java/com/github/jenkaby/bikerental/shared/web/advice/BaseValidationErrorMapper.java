package com.github.jenkaby.bikerental.shared.web.advice;

import com.github.jenkaby.bikerental.shared.web.response.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.jenkaby.bikerental.shared.web.advice.ErrorCodes.VALIDATION_ERROR;

@Component
public class BaseValidationErrorMapper implements ValidationErrorMapper {

    private static final Set<String> INTERNAL_ATTRIBUTES = Set.of("message", "groups", "payload");
    private final static Pattern CODE_PATTERN = Pattern.compile("([a-z])([A-Z])");

    @Override
    public ValidationError toValidationError(ConstraintViolation<?> violation) {
        String field = extractFieldName(violation);
        String code = mapConstraintCode(violation);
        Map<String, Object> params = extractConstraintParams(violation.getConstraintDescriptor());
        return new ValidationError(field, code, params);
    }

    @Override
    public ValidationError mapFieldError(FieldError fieldError) {
        return unwrapViolation(fieldError)
                .map(violation -> new ValidationError(
                        fieldError.getField(),
                        mapConstraintCode(violation),
                        extractConstraintParams(violation.getConstraintDescriptor())))
                .orElseGet(() -> new ValidationError(
                        fieldError.getField(),
                        extractConstraint(fieldError.getCodes()),
                        null));
    }

    @Override
    public ValidationError mapObjectError(ObjectError objectError) {
        return unwrapViolation(objectError)
                .map(violation -> new ValidationError(
                        null,
                        mapConstraintCode(violation),
                        extractConstraintParams(violation.getConstraintDescriptor())))
                .orElseGet(() -> new ValidationError(
                        null,
                        extractConstraint(objectError.getCodes()),
                        null));
    }

    @Override
    public ValidationError mapResolvableError(MessageSourceResolvable resolvable) {
        if (resolvable instanceof FieldError fieldError) {
            return mapFieldError(fieldError);
        }
        if (resolvable instanceof ObjectError objectError) {
            return mapObjectError(objectError);
        }
        return new ValidationError(null, extractConstraint(resolvable.getCodes()), null);
    }

    @Override
    public ValidationError mapParameterError(ParameterValidationResult result, MessageSourceResolvable error) {
        return unwrapViolation(result, error)
                .map(violation -> new ValidationError(
                        result.getMethodParameter().getParameterName(),
                        mapConstraintCode(violation),
                        extractConstraintParams(violation.getConstraintDescriptor())))
                .orElseGet(() -> mapResolvableError(error));
    }

    private Optional<ConstraintViolation<?>> unwrapViolation(ObjectError error) {
        if (error.contains(ConstraintViolation.class)) {
            ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);
            return Optional.of(violation);
        }
        return Optional.empty();
    }

    private Optional<ConstraintViolation<?>> unwrapViolation(ParameterValidationResult result, MessageSourceResolvable error) {
        try {
            ConstraintViolation<?> violation = result.unwrap(error, ConstraintViolation.class);
            return Optional.ofNullable(violation);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String mapConstraintCode(ConstraintViolation<?> violation) {
        var simpleConstraintName = violation.getConstraintDescriptor()
                .getAnnotation()
                .annotationType()
                .getSimpleName();

        return prefixCode(normalizeCode(simpleConstraintName));
    }

    private String normalizeCode(String rawCode) {
        return CODE_PATTERN.matcher(rawCode).replaceAll("$1_$2").toLowerCase();
    }

    private String prefixCode(String code) {
        return "validation." + code;
    }

    private Map<String, Object> extractConstraintParams(ConstraintDescriptor<?> descriptor) {
        Map<String, Object> attributes = descriptor.getAttributes();
        return attributes.entrySet()
                .stream()
                .filter(e -> !isInternalAttribute(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean isInternalAttribute(String key) {
        return INTERNAL_ATTRIBUTES.contains(key);
    }

    private String extractFieldName(ConstraintViolation<?> violation) {
        StringBuilder path = new StringBuilder();
        for (Path.Node node : violation.getPropertyPath()) {
            if (node.getName() != null) {
                if (!path.isEmpty()) {
                    path.append(".");
                }
                path.append(node.getName());
            }
            if (node.getIndex() != null) {
                path.append("[").append(node.getIndex()).append("]");
            }
            if (node.getKey() != null) {
                path.append("[").append(node.getKey()).append("]");
            }
        }
        return path.toString();
    }

    private String extractConstraint(String[] codes) {
        if (codes == null || codes.length == 0) {
            return VALIDATION_ERROR;
        }
        return prefixCode(normalizeCode(codes[codes.length - 1]));
    }
}
