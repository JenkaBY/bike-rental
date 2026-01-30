package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentQueryController {

    private final GetEquipmentByIdUseCase getById;
    private final SearchEquipmentsUseCase searchUseCase;
    private final EquipmentQueryMapper mapper;

    EquipmentQueryController(GetEquipmentByIdUseCase getById,
                             SearchEquipmentsUseCase searchUseCase,
                             EquipmentQueryMapper mapper) {
        this.getById = getById;
        this.searchUseCase = searchUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable("id") Long id) {
        return getById.execute(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class, id));
    }

    @GetMapping
    public ResponseEntity<Page<EquipmentResponse>> searchEquipments(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "serial", required = false) String serialNumber,
            @RequestParam(name = "uid", required = false) String uid,
            @PageableDefault(size = 20, sort = "serialNumber", direction = Sort.Direction.ASC) Pageable pageable) {

        var query = mapper.toSearchQuery(status, type, serialNumber, uid, pageable);
        var page = searchUseCase.execute(query).map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }
}
