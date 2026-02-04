package com.github.jenkaby.bikerental.componenttest.config.infra.messaging;

import com.github.jenkaby.bikerental.componenttest.context.MessageStore;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.BikeRentalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TestMessageListener {

    private final MessageStore messageStore;

    @ApplicationModuleListener
    public void handleEvent(BikeRentalEvent event) {
        log.info("[TEST] Event received: {}", event);
        messageStore.handleEvent(event);
    }
}
