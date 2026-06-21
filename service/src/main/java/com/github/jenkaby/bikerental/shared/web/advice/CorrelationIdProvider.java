package com.github.jenkaby.bikerental.shared.web.advice;

import com.github.jenkaby.bikerental.shared.infrastructure.port.uuid.UuidGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static com.github.jenkaby.bikerental.shared.web.advice.ProblemDetailField.CORRELATION_ID;

@Component
@RequiredArgsConstructor
public class CorrelationIdProvider {

    private final UuidGenerator uuidGenerator;

    public String resolve() {
        String correlationId = MDC.get(CORRELATION_ID);
        return correlationId != null ? correlationId : uuidGenerator.generate().toString();
    }
}
