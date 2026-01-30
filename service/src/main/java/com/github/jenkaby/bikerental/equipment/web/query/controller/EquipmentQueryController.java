package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentQueryController {

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping
    public ResponseEntity<Page<EquipmentResponse>> searchEquipments(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "serial", required = false) String serialNumber,
            @RequestParam(name = "uid", required = false) String uid,
            @PageableDefault(size = 20, sort = "serialNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
