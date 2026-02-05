package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentStatusUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentStatusRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentStatusCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentStatusMapper;
import com.github.jenkaby.bikerental.shared.web.support.Slug;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/equipment-statuses")
public class EquipmentStatusCommandController {

    private final CreateEquipmentStatusUseCase createUseCase;
    private final UpdateEquipmentStatusUseCase updateUseCase;
    private final EquipmentStatusCommandMapper commandMapper;
    private final EquipmentStatusMapper queryMapper;

    EquipmentStatusCommandController(CreateEquipmentStatusUseCase createUseCase,
                                     UpdateEquipmentStatusUseCase updateUseCase,
                                     EquipmentStatusCommandMapper commandMapper,
                                     EquipmentStatusMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    public ResponseEntity<EquipmentStatusResponse> create(@RequestBody @Valid EquipmentStatusRequest request) {
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{slug}")
    public ResponseEntity<EquipmentStatusResponse> update(@PathVariable("slug") @Slug String slug,
                                                          @RequestBody @Valid EquipmentStatusRequest request) {
        var command = commandMapper.toUpdateCommand(slug, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }
}
