package com.github.jenkaby.bikerental.componenttest.model;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum VocabularyType {
    EQUIPMENT_STATUS("equipment status"),
    EQUIPMENT_TYPE("equipment type");

    @Getter
    private final String humanReadableName;
    private static final Map<String, VocabularyType> CONTEXT;

    static {
        CONTEXT = Stream.of(VocabularyType.values())
                .collect(Collectors.toMap(VocabularyType::getHumanReadableName, Function.identity()));
    }

    VocabularyType(String humanReadableName) {
        this.humanReadableName = humanReadableName;
    }

    public static VocabularyType of(String name) {
        return Optional.ofNullable(name).map(String::toLowerCase).map(CONTEXT::get)
                .orElseThrow(() -> new IllegalArgumentException("No VocabularyType with name: " + name));

    }
}
