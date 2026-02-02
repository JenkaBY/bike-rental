package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentStatusesUseCase;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/equipment-statuses")
public class EquipmentStatusQueryController {

    private final GetEquipmentStatusesUseCase useCase;
    private final EquipmentStatusMapper mapper;

    EquipmentStatusQueryController(GetEquipmentStatusesUseCase useCase, EquipmentStatusMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentStatusResponse>> getAllEquipmentStatuses() {
        log.info("[GET] Get all equipment statuses");
        var statuses = useCase.findAll();
        var responses = statuses.stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }
}
