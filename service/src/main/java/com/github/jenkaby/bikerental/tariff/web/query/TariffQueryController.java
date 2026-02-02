package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetAllTariffsUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffQueryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tariffs")
public class TariffQueryController {

    private final GetTariffByIdUseCase getByIdUseCase;
    private final GetAllTariffsUseCase getAllUseCase;
    private final GetActiveTariffsByEquipmentTypeUseCase getActiveByTypeUseCase;
    private final TariffQueryMapper mapper;

    TariffQueryController(GetTariffByIdUseCase getByIdUseCase,
                          GetAllTariffsUseCase getAllUseCase,
                          GetActiveTariffsByEquipmentTypeUseCase getActiveByTypeUseCase,
                          TariffQueryMapper mapper) {
        this.getByIdUseCase = getByIdUseCase;
        this.getAllUseCase = getAllUseCase;
        this.getActiveByTypeUseCase = getActiveByTypeUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TariffResponse> getTariffById(@PathVariable("id") Long id) {
        log.info("[GET] Get tariff by id {}", id);
        Tariff tariff = getByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(tariff));
    }

    @GetMapping
    public ResponseEntity<Page<TariffResponse>> getAllTariffs(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("[GET] Get all tariffs with pagination {}", pageable);

        PageRequest pageRequest = new PageRequest(
                pageable.getPageSize(),
                pageable.getPageNumber(),
                pageable.getSort().toString()
        );
        Page<Tariff> tariffs = getAllUseCase.execute(pageRequest);
        Page<TariffResponse> response = tariffs.map(mapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TariffResponse>> getActiveTariffsByEquipmentType(
            @RequestParam("equipmentType") String equipmentType) {
        log.info("[GET] Get active tariffs for equipment type {}", equipmentType);

        List<Tariff> tariffs = getActiveByTypeUseCase.execute(equipmentType);
        List<TariffResponse> response = tariffs.stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}
