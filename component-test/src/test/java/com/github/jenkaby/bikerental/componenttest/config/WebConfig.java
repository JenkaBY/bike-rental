package com.github.jenkaby.bikerental.componenttest.config;


import com.github.jenkaby.bikerental.componenttest.config.support.BreakdownCostDetailsDeserializer;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;

@Configuration
@AutoConfigureTestRestTemplate
public class WebConfig {

    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper().rebuild()
            .addModule(new SimpleModule().addDeserializer(BreakdownCostDetails.class, new BreakdownCostDetailsDeserializer()))
            .build();

    @Bean
    public JacksonModule customDeserializers() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BreakdownCostDetails.class, new BreakdownCostDetailsDeserializer());
        return module;
    }
}
