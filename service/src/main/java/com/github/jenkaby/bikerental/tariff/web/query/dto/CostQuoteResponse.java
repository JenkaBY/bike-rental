package com.github.jenkaby.bikerental.tariff.web.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A persisted rental cost quote: the frozen calculation plus its identifier and validity window")
public record CostQuoteResponse(
        @NotNull UUID quoteId,
        @NotNull Instant quotedAt,
        @NotNull Instant expiresAt,
        @NotNull CostCalculationResponse calculation
) {
}
