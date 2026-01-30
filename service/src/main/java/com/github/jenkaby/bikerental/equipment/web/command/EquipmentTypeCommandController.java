package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentTypeRequest;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentTypeResponse;
import com.github.jenkaby.bikerental.shared.web.support.Slug;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/equipment-types")
public class EquipmentTypeCommandController {

    @PostMapping
    public ResponseEntity<EquipmentTypeResponse> create(@RequestBody EquipmentTypeRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{slug}")
    public ResponseEntity<EquipmentTypeResponse> update(@PathVariable("slug") @Slug String slug,
                                                        @RequestBody @Valid EquipmentTypeRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
