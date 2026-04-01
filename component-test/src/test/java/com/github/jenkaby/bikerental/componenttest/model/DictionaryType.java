package com.github.jenkaby.bikerental.componenttest.model;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DictionaryType {
    EQUIPMENT_TYPE("equipment type");

    @Getter
    private final String humanReadableName;
    private static final Map<String, DictionaryType> CONTEXT;

    static {
        CONTEXT = Stream.of(DictionaryType.values())
                .collect(Collectors.toMap(DictionaryType::getHumanReadableName, Function.identity()));
    }

    DictionaryType(String humanReadableName) {
        this.humanReadableName = humanReadableName;
    }

    public static DictionaryType of(String name) {
        return Optional.ofNullable(name).map(String::toLowerCase).map(CONTEXT::get)
                .orElseThrow(() -> new IllegalArgumentException("No VocabularyType with name: " + name));

    }
}
