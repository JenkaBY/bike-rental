package com.github.jenkaby.bikerental.equipment.web.command;

import com.github.jenkaby.bikerental.equipment.web.command.dto.EquipmentStatusRequest;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentStatusResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/equipment-statuses")
public class EquipmentStatusCommandController {

    @PostMapping
    public ResponseEntity<EquipmentStatusResponse> create(@RequestBody EquipmentStatusRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{slug}")
    public ResponseEntity<EquipmentStatusResponse> update(@PathVariable("slug") @NotEmpty String slug,
                                                          @RequestBody @Valid EquipmentStatusRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
