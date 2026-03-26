package com.github.jenkaby.bikerental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@EnableJpaAuditing
@Modulithic(sharedModules = "shared")
@ConfigurationPropertiesScan
public class BikeRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(BikeRentalApplication.class, args);
    }

}
