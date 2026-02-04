package com.github.jenkaby.bikerental.shared.infrastructure.messaging;


public interface EventPublisher {

    void publish(String destination, Object message);
}
