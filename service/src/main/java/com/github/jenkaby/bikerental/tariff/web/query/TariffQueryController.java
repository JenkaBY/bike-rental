package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import com.github.jenkaby.bikerental.tariff.application.mapper.TariffToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetAllTariffsUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffForRentalUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffResponse;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffSelectionResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffQueryMapper;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffSelectionMapper;
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
@Tag(name = OpenApiConfig.Tags.TARIFFS)
public class TariffQueryController {

    private final GetTariffByIdUseCase getByIdUseCase;
    private final GetAllTariffsUseCase getAllUseCase;
    private final GetActiveTariffsByEquipmentTypeUseCase getActiveByTypeUseCase;
    private final SelectTariffForRentalUseCase selectTariffForRentalUseCase;
    private final TariffPeriodSelector tariffPeriodSelector;
    private final TariffToInfoMapper tariffToInfoMapper;
    private final TariffQueryMapper mapper;
    private final TariffSelectionMapper selectionMapper;

    TariffQueryController(GetTariffByIdUseCase getByIdUseCase,
                          GetAllTariffsUseCase getAllUseCase,
                          GetActiveTariffsByEquipmentTypeUseCase getActiveByTypeUseCase,
                          SelectTariffForRentalUseCase selectTariffForRentalUseCase,
                          TariffPeriodSelector tariffPeriodSelector,
                          TariffToInfoMapper tariffToInfoMapper,
                          TariffQueryMapper mapper,
                          TariffSelectionMapper selectionMapper) {
        this.getByIdUseCase = getByIdUseCase;
        this.getAllUseCase = getAllUseCase;
        this.getActiveByTypeUseCase = getActiveByTypeUseCase;
        this.selectTariffForRentalUseCase = selectTariffForRentalUseCase;
        this.tariffPeriodSelector = tariffPeriodSelector;
        this.tariffToInfoMapper = tariffToInfoMapper;
        this.mapper = mapper;
        this.selectionMapper = selectionMapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tariff by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff found",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tariff not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffResponse> getTariffById(
            @Parameter(description = "Tariff ID", example = "1") @PathVariable("id") @Id Long id) {
        log.info("[GET] Get tariff by id {}", id);
        Tariff tariff = getByIdUseCase.get(id);
        return ResponseEntity.ok(mapper.toResponse(tariff));
    }

    @GetMapping
    @Operation(summary = "Get all tariffs", description = "Returns a paginated list of all tariffs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff page returned")
    })
    public ResponseEntity<Page<TariffResponse>> getAllTariffs(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("[GET] Get all tariffs with pagination {}", pageable);

        PageRequest pageRequest = new PageRequest(
                pageable.getPageSize(),
                pageable.getPageNumber(),
                pageable.getSort().toString()
        );
        Page<Tariff> tariffs = getAllUseCase.execute(pageRequest);
        Page<TariffResponse> response = tariffs.map(mapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active tariffs by equipment type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active tariffs returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TariffResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid equipment type",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<TariffResponse>> getActiveTariffsByEquipmentType(
            @Parameter(description = "Equipment type slug", example = "bike") @RequestParam("equipmentType") String equipmentType) {
        log.info("[GET] Get active tariffs for equipment type {}", equipmentType);

        List<Tariff> tariffs = getActiveByTypeUseCase.execute(equipmentType);
        List<TariffResponse> response = tariffs.stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/selection")
    @Operation(summary = "Select best tariff for rental",
            description = "Returns the most cost-effective tariff for the given equipment type, duration and date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff selected",
                    content = @Content(schema = @Schema(implementation = TariffSelectionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No suitable tariff found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffSelectionResponse> selectTariff(
            @Parameter(description = "Equipment type slug", example = "bike") @RequestParam("equipmentType") @NotBlank String equipmentType,
            @Parameter(description = "Rental duration in minutes", example = "120") @RequestParam("durationMinutes") @Positive int durationMinutes,
            @Parameter(description = "Rental date (ISO date, defaults to today)", example = "2026-06-01")
            @RequestParam(value = "rentalDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentalDate) {
        log.info("[GET] Select tariff for equipmentType={}, durationMinutes={}, rentalDate={}",
                equipmentType, durationMinutes, rentalDate);

        var command = new SelectTariffForRentalUseCase.SelectTariffCommand(equipmentType, durationMinutes, rentalDate);
        Tariff selectedTariff = selectTariffForRentalUseCase.execute(command);

        Duration duration = Duration.ofMinutes(durationMinutes);
        TariffPeriod period = tariffPeriodSelector.selectPeriod(duration);

        TariffSelectionResponse response = selectionMapper.toSelectionResponse(
                tariffToInfoMapper.toTariffInfo(selectedTariff),
                period
        );
        
        return ResponseEntity.ok(response);
    }
}
