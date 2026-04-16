package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsV2ByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetAllTariffsV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffV2ByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffSelectionV2Response;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffV2Response;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffV2QueryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/tariffs")
@Tag(name = OpenApiConfig.Tags.TARIFFS, description = "Tariff V2 API")
public class TariffV2QueryController {

    private final GetTariffV2ByIdUseCase getByIdUseCase;
    private final GetAllTariffsV2UseCase getAllUseCase;
    private final GetActiveTariffsV2ByEquipmentTypeUseCase getActiveByTypeUseCase;
    private final SelectTariffV2UseCase selectTariffUseCase;
    private final TariffV2QueryMapper mapper;

    TariffV2QueryController(GetTariffV2ByIdUseCase getByIdUseCase,
                            GetAllTariffsV2UseCase getAllUseCase,
                            GetActiveTariffsV2ByEquipmentTypeUseCase getActiveByTypeUseCase,
                            SelectTariffV2UseCase selectTariffUseCase,
                            TariffV2QueryMapper mapper) {
        this.getByIdUseCase = getByIdUseCase;
        this.getAllUseCase = getAllUseCase;
        this.getActiveByTypeUseCase = getActiveByTypeUseCase;
        this.selectTariffUseCase = selectTariffUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get V2 tariff by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff found",
                    content = @Content(schema = @Schema(implementation = TariffV2Response.class))),
            @ApiResponse(responseCode = "404", description = "Tariff not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffV2Response> getTariffById(
            @Parameter(description = "Tariff ID") @PathVariable("id") @Id Long id) {
        log.info("[GET] Get V2 tariff by id {}", id);
        TariffV2 tariff = getByIdUseCase.get(id);
        return ResponseEntity.ok(mapper.toResponse(tariff));
    }

    @GetMapping
    @Operation(summary = "Get all V2 tariffs (paginated)")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Tariff page returned"))
    public ResponseEntity<Page<TariffV2Response>> getAllTariffs(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("[GET] Get all V2 tariffs with pagination");
        PageRequest pageRequest = new PageRequest(
                pageable.getPageSize(),
                pageable.getPageNumber(),
                pageable.getSort().toString()
        );
        Page<TariffV2> tariffs = getAllUseCase.execute(pageRequest);
        Page<TariffV2Response> response = tariffs.map(mapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active V2 tariffs by equipment type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active tariffs returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TariffV2Response.class))))
    })
    public ResponseEntity<List<TariffV2Response>> getActiveTariffs(
            @Parameter(description = "Equipment type slug") @RequestParam(value = "equipmentType") String equipmentType) {
        log.info("[GET] Get active V2 tariffs for equipment type {}", equipmentType);
        List<TariffV2> tariffs = getActiveByTypeUseCase.execute(equipmentType);
        return ResponseEntity.ok(tariffs.stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/selection")
    @Operation(summary = "Select cheapest V2 tariff for equipment type and duration. For debug purpose")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff selected",
                    content = @Content(schema = @Schema(implementation = TariffSelectionV2Response.class))),
            @ApiResponse(responseCode = "404", description = "No suitable tariff found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffSelectionV2Response> selectTariff(
            @Parameter(description = "Equipment type") @RequestParam("equipmentType") @NotBlank String equipmentType,
            @Parameter(description = "Duration in minutes") @RequestParam("durationMinutes") @Positive int durationMinutes,
            @Parameter(description = "Rental date (default: today)")
            @RequestParam(value = "rentalDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentalDate) {
        log.info("[GET] Select V2 tariff for equipmentType={}, durationMinutes={}", equipmentType, durationMinutes);
        var duration = Duration.ofMinutes(durationMinutes);
        var tariff = selectTariffUseCase.execute(new SelectTariffV2UseCase.SelectTariffCommand(equipmentType, duration, rentalDate));
        var cost = tariff.calculateCost(duration);
        var response = new TariffSelectionV2Response(
                mapper.toResponse(tariff),
                cost.totalCost().amount(),
                cost.calculationBreakdown()
        );
        return ResponseEntity.ok(response);
    }
}
