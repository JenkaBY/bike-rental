package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentQueryMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipments")
@Validated
public class EquipmentCommandController {

    private final CreateEquipmentUseCase createUseCase;
    private final UpdateEquipmentUseCase updateUseCase;
    private final EquipmentCommandMapper commandMapper;
    private final EquipmentQueryMapper queryMapper;

    EquipmentCommandController(CreateEquipmentUseCase createUseCase,
                               UpdateEquipmentUseCase updateUseCase,
                               EquipmentCommandMapper commandMapper,
                               EquipmentQueryMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable("id") Long id,
            @Valid @RequestBody EquipmentRequest request) {
        var command = commandMapper.toUpdateCommand(id, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }
}
