package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationRequest;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.BatchCalculationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/tariffs")
@Tag(name = OpenApiConfig.Tags.TARIFFS, description = "Tariff V2 API")
public class TariffV2CalculationController {

    private final TariffV2Facade tariffV2Facade;
    private final BatchCalculationMapper requestMapper;

    TariffV2CalculationController(TariffV2Facade tariffV2Facade, BatchCalculationMapper requestMapper) {
        this.tariffV2Facade = tariffV2Facade;
        this.requestMapper = requestMapper;
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate rental cost for multiple equipment items",
            description = "Supports normal mode (auto-select tariffs, apply discount) and SPECIAL mode (fixed group price)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cost calculation result",
                    content = @Content(schema = @Schema(implementation = CostCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No suitable tariff found for an equipment type",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CostCalculationResponse> calculateCost(@Valid @RequestBody CostCalculationRequest request) {
        log.info("[POST] Batch cost calculation for {} equipment item(s)", request.equipments().size());
        var command = requestMapper.toCommand(request);
        var result = tariffV2Facade.calculateRentalCost(command);
        return ResponseEntity.ok(requestMapper.toResponse(result));
    }
}
