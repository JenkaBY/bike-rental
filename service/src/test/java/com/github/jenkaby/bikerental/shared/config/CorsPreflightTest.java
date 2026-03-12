package com.github.jenkaby.bikerental.shared.config;

import com.github.jenkaby.bikerental.support.web.TestingAppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CorsPreflightTest.StubController.class)
@Import({CorsConfig.class, TestingAppConfig.class})
@EnableConfigurationProperties(CorsProperties.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:3000",
        "app.cors.allowed-methods=GET,POST,PUT,PATCH,DELETE,OPTIONS",
        "app.cors.allowed-headers=*",
        "app.cors.allow-credentials=true",
        "app.cors.max-age=3600"
})
class CorsPreflightTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightRequest_shouldReturnCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/stub")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    void preflightRequest_fromDisallowedOrigin_shouldNotReturnAllowOriginHeader() throws Exception {
        mockMvc.perform(options("/api/stub")
                        .header(HttpHeaders.ORIGIN, "http://evil.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @RestController
    @RequestMapping("/api/stub")
    static class StubController {

        @GetMapping
        String get() {
            return "ok";
        }
    }
}

