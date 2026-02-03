package com.github.jenkaby.bikerental.shared.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class SpringApplicationEventPublisher implements MessagePublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(@NonNull String destination, @NonNull Object message) {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(destination, "destination must not be null");

        log.info("Destination: '{}', publishing message: {}", destination, message);

        eventPublisher.publishEvent(message);
    }
}
