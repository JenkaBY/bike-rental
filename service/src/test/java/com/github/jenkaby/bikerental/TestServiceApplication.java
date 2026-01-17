package com.github.jenkaby.bikerental;

import com.github.jenkaby.bikerental.support.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestServiceApplication {

    static void main(String[] args) {
        SpringApplication.from(BikeRentalApplication::main)
                .with(TestcontainersConfiguration.class).run(args);
    }

}
