package com.github.jenkaby.bikerental.tariff.web.command;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import com.github.jenkaby.bikerental.tariff.application.usecase.ActivateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.DeactivateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffRequest;
import com.github.jenkaby.bikerental.tariff.web.command.mapper.TariffCommandMapper;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffQueryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tariffs")
@Validated
@Tag(name = OpenApiConfig.Tags.TARIFFS)
public class TariffCommandController {

    private final CreateTariffUseCase createUseCase;
    private final UpdateTariffUseCase updateUseCase;
    private final ActivateTariffUseCase activateUseCase;
    private final DeactivateTariffUseCase deactivateUseCase;
    private final TariffCommandMapper commandMapper;
    private final TariffQueryMapper queryMapper;

    TariffCommandController(CreateTariffUseCase createUseCase,
                            UpdateTariffUseCase updateUseCase,
                            ActivateTariffUseCase activateUseCase,
                            DeactivateTariffUseCase deactivateUseCase,
                            TariffCommandMapper commandMapper,
                            TariffQueryMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.activateUseCase = activateUseCase;
        this.deactivateUseCase = deactivateUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    @Operation(summary = "Create tariff")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tariff created",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Tariff conflict",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffResponse> createTariff(@Valid @RequestBody TariffRequest request) {
        log.info("[POST] Create tariff with name {}", request.name());
        log.debug("[POST] Create tariff {}", request);
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tariff", description = "Replaces all fields of an existing tariff")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff updated",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Tariff not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffResponse> updateTariff(
            @Parameter(description = "Tariff ID", example = "1") @PathVariable("id") @Id Long id,
            @Valid @RequestBody TariffRequest request) {
        log.info("[PUT] Update tariff with id {}", id);
        log.debug("[PUT] Update tariff {} with data {}", id, request);
        var command = commandMapper.toUpdateCommand(id, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate tariff", description = "Marks a tariff as active so it can be used for rental pricing")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff activated",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tariff not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffResponse> activateTariff(
            @Parameter(description = "Tariff ID", example = "1") @PathVariable("id") @Id Long id) {
        log.info("[PATCH] Activate tariff with id {}", id);
        var activated = activateUseCase.execute(id);
        return ResponseEntity.ok(queryMapper.toResponse(activated));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate tariff", description = "Marks a tariff as inactive; it will no longer be selected for new rentals")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff deactivated",
                    content = @Content(schema = @Schema(implementation = TariffResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tariff not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffResponse> deactivateTariff(
            @Parameter(description = "Tariff ID", example = "1") @PathVariable("id") @Id Long id) {
        log.info("[PATCH] Deactivate tariff with id {}", id);
        var deactivated = deactivateUseCase.execute(id);
        return ResponseEntity.ok(queryMapper.toResponse(deactivated));
    }
}
