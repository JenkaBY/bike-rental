package com.github.jenkaby.bikerental.componenttest.config;


import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
@AutoConfigureTestRestTemplate
public class WebConfig {

    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
}
