package com.github.jenkaby.bikerental.componenttest.transformer;


import com.github.jenkaby.bikerental.componenttest.config.WebConfig;
import com.github.jenkaby.bikerental.componenttest.model.VocabularyType;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;
import io.cucumber.java.ParameterType;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

public class ParameterTypes {

    private final ObjectMapper objectMapper = WebConfig.DEFAULT_OBJECT_MAPPER;

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    public Object transformer(Object fromValue, Type toValueType) {
        return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
    }

    @ParameterType("GET|POST|PUT|PATCH|DELETE|OPTIONS")
    public HttpMethod httpMethod(String methodName) {
        return HttpMethod.valueOf(methodName);
    }

    @ParameterType("equipment status|equipment type")
    public VocabularyType vocabularyType(String type) {
        return VocabularyType.of(type);
    }
}
