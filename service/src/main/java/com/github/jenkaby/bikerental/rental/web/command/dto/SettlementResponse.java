package com.github.jenkaby.bikerental.rental.web.command.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Settlement response containing capture transaction references, optional release transaction reference and the timestamp when the settlement was recorded")
public record SettlementResponse(
        @NonNull
        @NotNull
        @ArraySchema(schema = @Schema(type = "string", format = "uuid", description = "Capture transaction reference (UUID)"),
                arraySchema = @Schema(description = "List of capture transaction UUIDs"))
        List<UUID> captureTransactionRefs,

        @Nullable
        @Schema(description = "Release transaction reference (UUID). Present when a release transaction was created.", nullable = true, type = "string", format = "uuid")
        UUID releaseTransactionRef,

        @NonNull
        @NotNull
        @Schema(description = "Timestamp when the settlement was recorded", example = "2023-08-01T12:34:56Z")
        Instant recordedAt) {
}
