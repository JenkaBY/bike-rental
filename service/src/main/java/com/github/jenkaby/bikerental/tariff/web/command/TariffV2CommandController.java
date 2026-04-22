package com.github.jenkaby.bikerental.tariff.web.command;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import com.github.jenkaby.bikerental.tariff.application.usecase.ActivateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.DeactivateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffV2Request;
import com.github.jenkaby.bikerental.tariff.web.command.mapper.TariffV2CommandMapper;
import com.github.jenkaby.bikerental.tariff.web.command.validation.ValidTariffV2Pricing;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffV2Response;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffV2QueryMapper;
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
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/tariffs", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
@Tag(name = OpenApiConfig.Tags.TARIFFS, description = "Tariff V2 API")
public class TariffV2CommandController {

    private final CreateTariffV2UseCase createUseCase;
    private final UpdateTariffV2UseCase updateUseCase;
    private final ActivateTariffV2UseCase activateUseCase;
    private final DeactivateTariffV2UseCase deactivateUseCase;
    private final TariffV2CommandMapper commandMapper;
    private final TariffV2QueryMapper queryMapper;

    TariffV2CommandController(CreateTariffV2UseCase createUseCase,
                              UpdateTariffV2UseCase updateUseCase,
                              ActivateTariffV2UseCase activateUseCase,
                              DeactivateTariffV2UseCase deactivateUseCase,
                              TariffV2CommandMapper commandMapper,
                              TariffV2QueryMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.activateUseCase = activateUseCase;
        this.deactivateUseCase = deactivateUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    @Operation(summary = "Create V2 tariff")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tariff created",
                    content = @Content(schema = @Schema(implementation = TariffV2Response.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffV2Response> createTariff(@Valid @RequestBody @ValidTariffV2Pricing TariffV2Request request) {
        log.info("[POST] Create V2 tariff with name {}", request.name());
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update V2 tariff")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff updated",
                    content = @Content(schema = @Schema(implementation = TariffV2Response.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Tariff not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffV2Response> updateTariff(
            @Parameter(description = "Tariff ID") @PathVariable("id") @Id Long id,
            @Valid @RequestBody @ValidTariffV2Pricing TariffV2Request request) {
        log.info("[PUT] Update V2 tariff with id {}", id);
        var command = commandMapper.toUpdateCommand(id, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate V2 tariff")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff activated",
                    content = @Content(schema = @Schema(implementation = TariffV2Response.class))),
            @ApiResponse(responseCode = "404", description = "Tariff not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffV2Response> activateTariff(@Parameter(description = "Tariff ID") @PathVariable("id") @Id Long id) {
        log.info("[PATCH] Activate V2 tariff with id {}", id);
        var activated = activateUseCase.execute(id);
        return ResponseEntity.ok(queryMapper.toResponse(activated));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate V2 tariff")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tariff deactivated",
                    content = @Content(schema = @Schema(implementation = TariffV2Response.class))),
            @ApiResponse(responseCode = "404", description = "Tariff not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TariffV2Response> deactivateTariff(@Parameter(description = "Tariff ID") @PathVariable("id") @Id Long id) {
        log.info("[PATCH] Deactivate V2 tariff with id {}", id);
        var deactivated = deactivateUseCase.execute(id);
        return ResponseEntity.ok(queryMapper.toResponse(deactivated));
    }
}
