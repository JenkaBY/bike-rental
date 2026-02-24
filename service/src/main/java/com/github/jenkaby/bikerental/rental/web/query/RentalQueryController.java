package com.github.jenkaby.bikerental.rental.web.query;

import com.github.jenkaby.bikerental.rental.application.usecase.FindRentalsUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.GetRentalByIdUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSummaryResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/rentals")
@Slf4j
class RentalQueryController {

    private final GetRentalByIdUseCase getRentalByIdUseCase;
    private final FindRentalsUseCase findRentalsUseCase;
    private final RentalQueryMapper mapper;
    private final PageMapper pageMapper;

    RentalQueryController(
            GetRentalByIdUseCase getRentalByIdUseCase,
            FindRentalsUseCase findRentalsUseCase,
            RentalQueryMapper mapper,
            PageMapper pageMapper) {
        this.getRentalByIdUseCase = getRentalByIdUseCase;
        this.findRentalsUseCase = findRentalsUseCase;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getRentalById(@PathVariable("id") @Id Long id) {
        log.info("[GET] Get rental by id: {}", id);
        var rental = getRentalByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(rental));
    }

    @GetMapping
    public ResponseEntity<Page<RentalSummaryResponse>> getRentals(
            @RequestParam(name = "status", required = false) RentalStatus status,
            @RequestParam(name = "customerId", required = false) UUID customerId,
            @PageableDefault(size = 20, sort = "expectedReturnAt", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("[GET] Get rentals with filters status={}, customerId={}", status, customerId);

        PageRequest pageRequest = pageMapper.toPageRequest(pageable);
        var query = new FindRentalsUseCase.FindRentalsQuery(status, customerId, pageRequest);

        Page<Rental> rentals = findRentalsUseCase.execute(query);

        Page<RentalSummaryResponse> response = rentals.map(mapper::toRentalSummaryResponse);
        return ResponseEntity.ok(response);
    }
}
