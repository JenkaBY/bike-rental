package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.domain.QuoteRef;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostQuoteUseCase;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationRequest;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostQuoteResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.BatchCalculationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/api/tariffs", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = OpenApiConfig.Tags.TARIFFS, description = "Tariff V2 API")
public class TariffV2CalculationController {

    private final TariffV2Facade tariffV2Facade;
    private final RentalCostQuoteUseCase rentalCostQuoteUseCase;
    private final BatchCalculationMapper requestMapper;

    TariffV2CalculationController(TariffV2Facade tariffV2Facade, RentalCostQuoteUseCase rentalCostQuoteUseCase,
                                  BatchCalculationMapper requestMapper) {
        this.tariffV2Facade = tariffV2Facade;
        this.rentalCostQuoteUseCase = rentalCostQuoteUseCase;
        this.requestMapper = requestMapper;
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate rental cost for multiple equipment items",
            deprecated = true,
            description = "Supports normal mode (auto-select tariffs, apply discount) and SPECIAL mode (fixed group price)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cost calculation result",
                    content = @Content(schema = @Schema(implementation = CostCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No suitable tariff found for an equipment type",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @Deprecated(forRemoval = true)
    public ResponseEntity<CostCalculationResponse> calculateCost(@Valid @RequestBody CostCalculationRequest request) {
        log.info("[POST] Batch cost calculation for {} equipment item(s)", request.equipments().size());
        var command = requestMapper.toCommand(request);
        var result = tariffV2Facade.calculateRentalCost(command);
        return ResponseEntity.ok(requestMapper.toResponse(result));
    }

    @PutMapping("/calculations")
    @Operation(summary = "Calculate V2 rental cost with per-equipment return timestamps",
            description = "Supports normal mode (per-equipment billing from global startAt) and SPECIAL mode (fixed group price)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cost calculation result",
                    content = @Content(schema = @Schema(implementation = CostCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No suitable tariff found for an equipment type",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CostCalculationResponse> costCalculations(@Valid @RequestBody CostCalculationV2Request request) {
        log.info("[PUT] V2 batch cost calculation for {} equipment item(s)", request.equipments().size());
        var command = requestMapper.toV2Command(request);
        var result = tariffV2Facade.calculateRentalCostV2(command);
        return ResponseEntity.ok(requestMapper.toResponse(result));
    }

    @PostMapping("/quotes")
    @Operation(summary = "Create a rental cost quote",
            description = "Calculates the V2 rental cost and persists it as an immutable, single-use snapshot with a " +
                    "validity window, so the exact figure can be reused at return confirmation instead of being recalculated")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cost quote created",
                    content = @Content(schema = @Schema(implementation = CostQuoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No suitable tariff found for an equipment type",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CostQuoteResponse> createQuote(@Valid @RequestBody CostCalculationV2Request request) {
        log.info("[POST] Creating cost quote for {} equipment item(s)", request.equipments().size());
        var command = requestMapper.toV2Command(request);
        var quote = rentalCostQuoteUseCase.createQuote(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestMapper.toQuoteResponse(quote));
    }

    @DeleteMapping("/quotes/{id}")
    @Operation(summary = "Delete a rental cost quote",
            description = "Removes a cost quote before it is consumed or expires")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cost quote deleted"),
            @ApiResponse(responseCode = "404", description = "Cost quote not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> deleteQuote(@PathVariable("id") UUID id) {
        log.info("[DELETE] Deleting cost quote {}", id);
        rentalCostQuoteUseCase.deleteQuote(new QuoteRef(id));
        return ResponseEntity.noContent().build();
    }
}
