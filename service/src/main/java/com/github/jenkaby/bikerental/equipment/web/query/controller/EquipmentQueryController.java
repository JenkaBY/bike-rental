package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentBySerialNumberUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByUidUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/equipments")
public class EquipmentQueryController {


    private final GetEquipmentByIdUseCase getById;
    private final GetEquipmentByUidUseCase getByUid;
    private final GetEquipmentBySerialNumberUseCase getBySerial;
    private final SearchEquipmentsUseCase searchUseCase;
    private final EquipmentQueryMapper mapper;

    EquipmentQueryController(GetEquipmentByIdUseCase getById,
                             GetEquipmentByUidUseCase getByUid,
                             GetEquipmentBySerialNumberUseCase getBySerial,
                             SearchEquipmentsUseCase searchUseCase,
                             EquipmentQueryMapper mapper) {
        this.getById = getById;
        this.getByUid = getByUid;
        this.getBySerial = getBySerial;
        this.searchUseCase = searchUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable("id") Long id) {
        log.info("[GET] Get equipment by id {}", id);
        return getById.execute(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class, id));
    }

    @GetMapping("/by-uid/{uid}")
    public ResponseEntity<EquipmentResponse> getEquipmentByUid(@PathVariable("uid") String uid) {
        log.info("[GET] Get equipment by uid {}", uid);
        var result = getByUid.execute(new com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid(uid));
        return result.map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class, uid));
    }

    @GetMapping("/by-serial/{serialNumber}")
    public ResponseEntity<EquipmentResponse> getEquipmentBySerial(@PathVariable("serialNumber") String serialNumber) {
        log.info("[GET] Get equipment by serial number{}", serialNumber);
        var result = getBySerial.execute(new com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber(serialNumber));
        return result.map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class, serialNumber));
    }

    @GetMapping
    public ResponseEntity<Page<EquipmentResponse>> searchEquipments(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "type", required = false) String type,
            @PageableDefault(size = 20, sort = "serialNumber", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("[GET] Search equipments filters status={} type={}", status, type);
        var query = mapper.toSearchQuery(status, type, pageable);
        var page = searchUseCase.execute(query).map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }
}
