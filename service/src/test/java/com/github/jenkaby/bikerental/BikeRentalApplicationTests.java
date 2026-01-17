package com.github.jenkaby.bikerental;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BikeRentalApplicationTests {

    @Test
    void contextLoads() {
        assertThat(true)
                .as("Spring context must be initialized and loaded successfully")
                .isTrue();
    }

}
