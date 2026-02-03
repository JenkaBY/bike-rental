package com.github.jenkaby.bikerental.shared.infrastructure.messaging;


public interface MessagePublisher {

    void publish(String destination, Object message);
}
