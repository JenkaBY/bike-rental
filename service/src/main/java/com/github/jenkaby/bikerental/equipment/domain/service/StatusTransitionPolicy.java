package com.github.jenkaby.bikerental.equipment.domain.service;

import org.jspecify.annotations.NonNull;

public interface StatusTransitionPolicy {

    void validateTransition(@NonNull String fromStatusSlug, @NonNull String toStatusSlug);
}
