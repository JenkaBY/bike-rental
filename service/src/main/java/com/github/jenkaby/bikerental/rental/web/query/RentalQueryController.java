package com.github.jenkaby.bikerental.rental.web.query;

import com.github.jenkaby.bikerental.rental.application.usecase.FindRentalsUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.GetRentalByIdUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSummaryResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import com.github.jenkaby.bikerental.shared.web.support.Id;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(path = "/api/rentals", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
@Tag(name = OpenApiConfig.Tags.RENTALS)
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
    @Operation(summary = "Get rental by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rental found",
                    content = @Content(schema = @Schema(implementation = RentalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rental not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RentalResponse> getRentalById(@Parameter(description = "Rental ID", example = "1") @PathVariable("id") @Id Long id) {
        log.info("[GET] Get rental by id: {}", id);
        var rental = getRentalByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(rental));
    }

    @GetMapping
    @Operation(summary = "Search rentals", description = "Returns a paginated list of rentals filtered by status, customer or equipment UID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rental page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<RentalSummaryResponse>> getRentals(
            @Parameter(description = "Rental status filter", example = "ACTIVE") @RequestParam(name = "status", required = false) RentalStatus status,
            @Parameter(description = "Customer UUID filter") @RequestParam(name = "customerId", required = false) UUID customerId,
            @Parameter(description = "Equipment UID filter", example = "BIKE-001") @RequestParam(name = "equipmentUid", required = false) String equipmentUid,
            @Parameter(description = "Created-at range start (inclusive), format yyyy-MM-dd", example = "2026-02-15") @RequestParam(name = "from", required = false) LocalDate from,
            @Parameter(description = "Created-at range end (inclusive), format yyyy-MM-dd", example = "2026-02-20") @RequestParam(name = "to", required = false) LocalDate to,
            @PageableDefault(size = 20, sort = "expectedReturnAt", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("[GET] Get rentals with filters status={}, customerId={}, equipmentUid={}, from={}, to={}", status, customerId, equipmentUid, from, to);

        var pageRequest = pageMapper.toPageRequest(pageable);
        var query = new FindRentalsUseCase.FindRentalsQuery(status, customerId, equipmentUid, pageRequest, from, to);

        Page<Rental> rentals = findRentalsUseCase.execute(query);

        Page<RentalSummaryResponse> response = rentals.map(mapper::toRentalSummaryResponse);
        return ResponseEntity.ok(response);
    }
}
