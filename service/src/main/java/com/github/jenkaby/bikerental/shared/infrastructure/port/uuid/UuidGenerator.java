package com.github.jenkaby.bikerental.shared.infrastructure.port.uuid;

import java.util.UUID;

/**
 * Port for generating UUIDs. Implementations should provide desired UUID strategy (v7, ULID, etc.).
 */
public interface UuidGenerator {

    UUID generate();
}
