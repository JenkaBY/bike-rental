package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-statuses")
public class EquipmentStatusQueryController {

    @GetMapping
    public ResponseEntity<List<EquipmentStatusResponse>> getAllEquipmentStatuses() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
