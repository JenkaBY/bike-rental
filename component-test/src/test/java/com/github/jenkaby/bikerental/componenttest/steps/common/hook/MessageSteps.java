package com.github.jenkaby.bikerental.componenttest.steps.common.hook;

import com.github.jenkaby.bikerental.componenttest.context.MessageStore;
import io.cucumber.java.After;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MessageSteps {

    private final MessageStore messageStore;

    @After
    public void clearMessages() {
        messageStore.clear();
    }
}
