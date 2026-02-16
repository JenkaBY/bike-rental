package com.github.jenkaby.bikerental.tariff.web.query;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import com.github.jenkaby.bikerental.tariff.application.mapper.TariffToInfoMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetAllTariffsUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffForRentalUseCase;
import com.github.jenkaby.bikerental.tariff.application.util.TariffPeriodSelector;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffResponse;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffSelectionResponse;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffQueryMapper;
import com.github.jenkaby.bikerental.tariff.web.query.mapper.TariffSelectionMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/tariffs")
public class TariffQueryController {

    private final GetTariffByIdUseCase getByIdUseCase;
    private final GetAllTariffsUseCase getAllUseCase;
    private final GetActiveTariffsByEquipmentTypeUseCase getActiveByTypeUseCase;
    private final SelectTariffForRentalUseCase selectTariffForRentalUseCase;
    private final TariffPeriodSelector tariffPeriodSelector;
    private final TariffToInfoMapper tariffToInfoMapper;
    private final TariffQueryMapper mapper;
    private final TariffSelectionMapper selectionMapper;

    TariffQueryController(GetTariffByIdUseCase getByIdUseCase,
                          GetAllTariffsUseCase getAllUseCase,
                          GetActiveTariffsByEquipmentTypeUseCase getActiveByTypeUseCase,
                          SelectTariffForRentalUseCase selectTariffForRentalUseCase,
                          TariffPeriodSelector tariffPeriodSelector,
                          TariffToInfoMapper tariffToInfoMapper,
                          TariffQueryMapper mapper,
                          TariffSelectionMapper selectionMapper) {
        this.getByIdUseCase = getByIdUseCase;
        this.getAllUseCase = getAllUseCase;
        this.getActiveByTypeUseCase = getActiveByTypeUseCase;
        this.selectTariffForRentalUseCase = selectTariffForRentalUseCase;
        this.tariffPeriodSelector = tariffPeriodSelector;
        this.tariffToInfoMapper = tariffToInfoMapper;
        this.mapper = mapper;
        this.selectionMapper = selectionMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TariffResponse> getTariffById(@PathVariable("id") @Id Long id) {
        log.info("[GET] Get tariff by id {}", id);
        Tariff tariff = getByIdUseCase.get(id);
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

    @GetMapping("/selection")
    public ResponseEntity<TariffSelectionResponse> selectTariff(
            @RequestParam("equipmentType") @NotBlank String equipmentType,
            @RequestParam("durationMinutes") @Positive int durationMinutes,
            @RequestParam(value = "rentalDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentalDate) {
        log.info("[GET] Select tariff for equipmentType={}, durationMinutes={}, rentalDate={}",
                equipmentType, durationMinutes, rentalDate);

        var command = new SelectTariffForRentalUseCase.SelectTariffCommand(equipmentType, durationMinutes, rentalDate);
        Tariff selectedTariff = selectTariffForRentalUseCase.execute(command);

        Duration duration = Duration.ofMinutes(durationMinutes);
        TariffPeriod period = tariffPeriodSelector.selectPeriod(duration);

        TariffSelectionResponse response = selectionMapper.toSelectionResponse(
                tariffToInfoMapper.toTariffInfo(selectedTariff),
                period
        );
        
        return ResponseEntity.ok(response);
    }
}
