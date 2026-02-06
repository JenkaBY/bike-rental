package com.github.jenkaby.bikerental.rental.web.query;

import com.github.jenkaby.bikerental.rental.application.usecase.GetRentalByIdUseCase;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
@Slf4j
class RentalQueryController {

    private final GetRentalByIdUseCase getRentalByIdUseCase;
    private final RentalQueryMapper mapper;

    RentalQueryController(
            GetRentalByIdUseCase getRentalByIdUseCase,
            RentalQueryMapper mapper) {
        this.getRentalByIdUseCase = getRentalByIdUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getRentalById(@PathVariable("id") @Id Long id) {
        log.info("[GET] Get rental by id: {}", id);
        var rental = getRentalByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(rental));
    }
}
