package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            @RequestParam(required = false) String statusSlug,
            @RequestParam(required = false) String typeSlug,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String uid,
            @PageableDefault(size = 20) Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
