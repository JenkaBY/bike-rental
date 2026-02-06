package com.github.jenkaby.bikerental.rental.web.command;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
@Slf4j
class RentalCommandController {

    private final CreateRentalUseCase createRentalUseCase;
    private final RentalQueryMapper queryMapper;

    RentalCommandController(
            CreateRentalUseCase createRentalUseCase,
            RentalQueryMapper queryMapper) {
        this.createRentalUseCase = createRentalUseCase;
        this.queryMapper = queryMapper;
    }

    @PostMapping
    public ResponseEntity<RentalResponse> createRental() {
        log.info("[POST] Creating new rental draft");
        var command = new CreateRentalUseCase.CreateRentalCommand();
        Rental rental = createRentalUseCase.execute(command);
        var response = queryMapper.toResponse(rental);
        log.info("[POST] Rental created successfully with id: {}", rental.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
