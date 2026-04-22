package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetPricingTypesUseCase;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingTypeResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffV2QueryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/api/tariffs", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = OpenApiConfig.Tags.TARIFFS, description = "Tariff V2 API")
public class PricingTypeQueryController {

    private final GetPricingTypesUseCase getPricingTypesUseCase;
    private final TariffV2QueryMapper mapper;

    PricingTypeQueryController(GetPricingTypesUseCase getPricingTypesUseCase,
                               TariffV2QueryMapper mapper) {
        this.getPricingTypesUseCase = getPricingTypesUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/pricing-types")
    @Operation(summary = "Get all pricing types with localized title and description")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Pricing types list"))
    public ResponseEntity<List<PricingTypeResponse>> getPricingTypes() {
        log.info("[GET] Get pricing types");
        var list = getPricingTypesUseCase.execute().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }
}

