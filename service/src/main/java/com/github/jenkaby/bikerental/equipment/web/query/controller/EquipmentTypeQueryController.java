package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentTypesUseCase;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentTypeResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentTypeMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-types")
public class EquipmentTypeQueryController {

    private final GetEquipmentTypesUseCase useCase;
    private final EquipmentTypeMapper mapper;

    EquipmentTypeQueryController(GetEquipmentTypesUseCase useCase, EquipmentTypeMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentTypeResponse>> getAllEquipmentTypes() {
        var types = useCase.findAll();
        var responses = types.stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }
}
