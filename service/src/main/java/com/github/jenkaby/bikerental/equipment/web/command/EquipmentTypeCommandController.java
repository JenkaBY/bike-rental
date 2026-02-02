package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.application.usecase.CreateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentTypeRequest;
import com.github.jenkaby.bikerental.equipment.web.command.mapper.EquipmentTypeCommandMapper;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentTypeResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentTypeMapper;
import com.github.jenkaby.bikerental.shared.web.support.Slug;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/equipment-types")
public class EquipmentTypeCommandController {

    private final CreateEquipmentTypeUseCase createUseCase;
    private final UpdateEquipmentTypeUseCase updateUseCase;
    private final EquipmentTypeCommandMapper commandMapper;
    private final EquipmentTypeMapper queryMapper;

    EquipmentTypeCommandController(CreateEquipmentTypeUseCase createUseCase,
                                   UpdateEquipmentTypeUseCase updateUseCase,
                                   EquipmentTypeCommandMapper commandMapper,
                                   EquipmentTypeMapper queryMapper) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    public ResponseEntity<EquipmentTypeResponse> create(@RequestBody @Valid EquipmentTypeRequest request) {
        var command = commandMapper.toCreateCommand(request);
        var created = createUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(queryMapper.toResponse(created));
    }

    @PutMapping("/{slug}")
    public ResponseEntity<EquipmentTypeResponse> update(@PathVariable("slug") @Slug String slug,
                                                        @RequestBody @Valid EquipmentTypeRequest request) {
        var command = commandMapper.toUpdateCommand(slug, request);
        var updated = updateUseCase.execute(command);
        return ResponseEntity.ok(queryMapper.toResponse(updated));
    }
}
