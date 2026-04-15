package com.github.jenkaby.bikerental.tariff.application.strategy;

import org.jspecify.annotations.NonNull;


public interface ForgivenessStrategy {

    boolean shouldForgive(int overtimeMinutes);

    int getForgivenMinutes(int overtimeMinutes);

    @NonNull
    String getForgivenessMessage(int overtimeMinutes);
}
