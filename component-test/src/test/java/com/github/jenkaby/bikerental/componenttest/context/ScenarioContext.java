package com.github.jenkaby.bikerental.componenttest.context;


import com.github.jenkaby.bikerental.componenttest.config.WebConfig;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;

import java.util.*;

@Getter
@Setter
@ScenarioScope
@Component
public class ScenarioContext {

    private final ObjectMapper mapper = WebConfig.DEFAULT_OBJECT_MAPPER;
    private final Map<String, List<String>> requestHeaders = new HashMap<>();
    private ResponseEntity<String> response;
    private Object requestBody;
    private String modifiedObjectId;
    private Set<UUID> persistedIds = new HashSet<>();

    @SneakyThrows
    public <T> T getResponseBody(Class<T> clazz) {
        return mapper.readValue(response.getBody(), clazz);
    }

    @SneakyThrows
    public <T> List<T> getResponseAsList(Class<T> clazz) {
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return mapper.readValue(response.getBody(), collectionType);
    }

    @SneakyThrows
    public <T> Page<T> getResponseAsPage(Class<T> clazz) {
        JavaType type = mapper.getTypeFactory().constructParametricType(Page.class, clazz);
        return mapper.readValue(response.getBody(), type);
    }

    public String getStringResponseBody() {
        return response.getBody();
    }

    public void addHeader(String headerKey, String headerValue) {
        List<String> values = this.requestHeaders.getOrDefault(headerKey, new ArrayList<>());
        values.add(headerValue);
        this.requestHeaders.put(headerKey, values);
    }

    public void replaceHeader(String headerKey, String headerValue) {
        List<String> values = new ArrayList<>();
        values.add(headerValue);
        this.requestHeaders.put(headerKey, values);
    }

    public void removeHeader(String headerKey) {
        this.requestHeaders.remove(headerKey);
    }

    public void clearHeaders() {
        this.requestHeaders.clear();
    }

    public MultiValueMap<String, String> getRequestHeaders() {
        return CollectionUtils.toMultiValueMap(requestHeaders);
    }

    public void addPersistedId(UUID id) {
        this.persistedIds.add(id);
    }
}
