package com.github.jenkaby.bikerental.shared.web.advice;

import com.github.jenkaby.bikerental.shared.web.response.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Map;
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

        var field = fieldError.getField();
        var code = extractConstraint(fieldError.getCodes());

        var params = extractParams(fieldError);

        return new ValidationError(field, code, params);
    }

    @Override
    public ValidationError mapObjectError(ObjectError objectError) {
        var code = extractConstraint(objectError.getCodes());
        return new ValidationError(null, code, null);
    }

    @Override
    public ValidationError mapResolvableError(MessageSourceResolvable resolvable) {
        var code = extractConstraint(resolvable.getCodes());
        var params = extractParams(resolvable.getArguments());
        return new ValidationError(null, prefixCode(code), params);
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

    private Map<String, Object> extractParams(FieldError fieldError) {
        var args = fieldError.getArguments();
        if (args == null) {
            return null;
        }
        return Arrays.stream(args)
                .filter(arg -> arg instanceof ConstraintDescriptor<?>)
                .map(arg -> (ConstraintDescriptor<?>) arg)
                .findFirst()
                .map(this::extractConstraintParams)
                .orElse(null);
    }

    private Map<String, Object> extractConstraintParams(ConstraintDescriptor<?> descriptor) {
        Map<String, Object> attributes = descriptor.getAttributes();
        return attributes.entrySet()
                .stream()
                .filter(e -> !isInternalAttribute(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Object> extractParams(Object[] arguments) {
        if (arguments == null) {
            return null;
        }
        for (Object arg : arguments) {
            if (arg instanceof ConstraintDescriptor<?> descriptor) {
                return this.extractConstraintParams(descriptor);
            }
        }
        return null;
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
