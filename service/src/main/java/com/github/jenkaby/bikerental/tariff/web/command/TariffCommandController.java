package com.github.jenkaby.bikerental.tariff.web.command;

import com.github.jenkaby.bikerental.shared.web.support.Id;
import com.github.jenkaby.bikerental.tariff.application.usecase.ActivateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.DeactivateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffRequest;
import com.github.jenkaby.bikerental.tariff.web.command.mapper.TariffCommandMapper;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffQueryMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tariffs")
@Validated
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
    public ResponseEntity<TariffResponse> createTariff(@Valid @RequestBody TariffRequest request) {
        log.info("[POST] Create tariff with name {}", request.name());
        log.debug("[POST] Create tariff {}", request);
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TariffResponse> updateTariff(
            @PathVariable("id") @Id Long id,
            @Valid @RequestBody TariffRequest request) {
        log.info("[PUT] Update tariff with id {}", id);
        log.debug("[PUT] Update tariff {} with data {}", id, request);
        var command = commandMapper.toUpdateCommand(id, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<TariffResponse> activateTariff(@PathVariable("id") @Id Long id) {
        log.info("[PATCH] Activate tariff with id {}", id);
        var activated = activateUseCase.execute(id);
        return ResponseEntity.ok(queryMapper.toResponse(activated));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<TariffResponse> deactivateTariff(@PathVariable("id") @Id Long id) {
        log.info("[PATCH] Deactivate tariff with id {}", id);
        var deactivated = deactivateUseCase.execute(id);
        return ResponseEntity.ok(queryMapper.toResponse(deactivated));
    }
}
