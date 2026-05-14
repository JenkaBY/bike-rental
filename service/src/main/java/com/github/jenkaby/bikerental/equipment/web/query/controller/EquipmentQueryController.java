package com.github.jenkaby.bikerental.equipment.web.query.controller;

import com.github.jenkaby.bikerental.equipment.application.usecase.*;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import com.github.jenkaby.bikerental.equipment.web.query.dto.EquipmentResponse;
import com.github.jenkaby.bikerental.equipment.web.query.mapper.EquipmentQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/api/equipments", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = OpenApiConfig.Tags.EQUIPMENT)
public class EquipmentQueryController {


    private final GetEquipmentByIdUseCase getById;
    private final GetEquipmentByUidUseCase getByUid;
    private final GetEquipmentBySerialNumberUseCase getBySerial;
    private final SearchEquipmentsUseCase searchUseCase;
    private final GetEquipmentByIdsUseCase getByIds;
    private final EquipmentQueryMapper mapper;

    EquipmentQueryController(GetEquipmentByIdUseCase getById,
                             GetEquipmentByUidUseCase getByUid,
                             GetEquipmentBySerialNumberUseCase getBySerial,
                             SearchEquipmentsUseCase searchUseCase,
                             GetEquipmentByIdsUseCase getByIds,
                             EquipmentQueryMapper mapper) {
        this.getById = getById;
        this.getByUid = getByUid;
        this.getBySerial = getBySerial;
        this.searchUseCase = searchUseCase;
        this.getByIds = getByIds;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment found",
                    content = @Content(schema = @Schema(implementation = EquipmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Equipment not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentResponse> getEquipmentById(
            @Parameter(description = "Equipment ID", example = "1") @PathVariable("id") Long id) {
        log.info("[GET] Get equipment by id {}", id);
        return getById.execute(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class, id));
    }

    @GetMapping("/by-uid/{uid}")
    @Operation(summary = "Get equipment by UID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment found",
                    content = @Content(schema = @Schema(implementation = EquipmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Equipment not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentResponse> getEquipmentByUid(
            @Parameter(description = "Equipment UID", example = "BIKE-001") @PathVariable("uid") String uid) {
        log.info("[GET] Get equipment by uid {}", uid);
        var result = getByUid.execute(new Uid(uid));
        return result.map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class, uid));
    }

    @GetMapping("/by-serial/{serialNumber}")
    @Operation(summary = "Get equipment by serial number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment found",
                    content = @Content(schema = @Schema(implementation = EquipmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Equipment not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EquipmentResponse> getEquipmentBySerial(
            @Parameter(description = "Serial number", example = "SN-123456") @PathVariable("serialNumber") String serialNumber) {
        log.info("[GET] Get equipment by serial number{}", serialNumber);
        var result = getBySerial.execute(new SerialNumber(serialNumber));
        return result.map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(Equipment.class, serialNumber));
    }

    @GetMapping
    @Operation(summary = "Search equipment", description = "Returns paginated equipment list filtered by status, type, and/or free-text search across uid, serial number, and model")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<EquipmentResponse>> searchEquipments(
            @Deprecated(forRemoval = true)
            @Parameter(description = "Status slug filter", example = "available") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "Type slug filter", example = "bike") @RequestParam(name = "type", required = false) String type,
            @Parameter(description = "Free-text search across uid, serial number, and model (case-insensitive substring match)", example = "city") @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 20, sort = "serialNumber", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("[GET] Search equipments filters status={} type={} q={}", status, type, q);
        var query = mapper.toSearchQuery(status, type, q, pageable);
        var page = searchUseCase.execute(query).map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/batch")
    @Operation(
            summary = "Batch get equipment by IDs",
            description = "Returns a flat list of equipment records for the provided IDs. IDs that do not match any record are silently omitted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EquipmentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid ids parameter — missing, non-numeric, non-positive, or more than 100 elements",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<EquipmentResponse>> getBatchEquipments(
            @Parameter(description = "Comma-separated list of positive equipment IDs, 1–100 elements", example = "1,2,3")
            @RequestParam(name = "ids")
            @NotEmpty(message = "ids must not be empty")
            @Size(max = 100, message = "ids must contain at most 100 elements")
            List<@Positive(message = "Each equipment ID must be a positive number") Long> ids) {
        log.info("[GET] Batch fetch equipment ids count={}", ids.size());
        var distinctIds = ids.stream().distinct().toList();
        var equipment = getByIds.execute(distinctIds);
        return ResponseEntity.ok(mapper.toResponses(equipment));
    }
}
