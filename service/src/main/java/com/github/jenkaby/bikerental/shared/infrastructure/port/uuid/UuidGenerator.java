package com.github.jenkaby.bikerental.shared.infrastructure.port.uuid;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

/**
 * Port for generating UUIDs. Implementations should provide desired UUID strategy (v7, ULID, etc.).
 */
public interface UuidGenerator {

    UUID generate();

    default UUID generateNameBased(String name) {
        return UuidCreator.getNameBasedMd5(name);
    }
}
