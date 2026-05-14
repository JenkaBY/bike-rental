package com.github.jenkaby.bikerental.customer.web.query;

import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomerByIdUseCase;
import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomersByIdsUseCase;
import com.github.jenkaby.bikerental.customer.web.mapper.CustomerWebMapper;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerSearchResponse;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/customers", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
@Slf4j
@Tag(name = OpenApiConfig.Tags.CUSTOMERS)
class CustomerQueryController {

    private final CustomerQueryUseCase customerQueryUseCase;
    private final CustomerWebMapper mapper;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;
    private final GetCustomersByIdsUseCase getCustomersByIdsUseCase;

    CustomerQueryController(CustomerQueryUseCase customerQueryUseCase,
                            GetCustomerByIdUseCase getCustomerByIdUseCase,
                            GetCustomersByIdsUseCase getCustomersByIdsUseCase,
                            CustomerWebMapper customerMapper) {
        this.customerQueryUseCase = customerQueryUseCase;
        this.mapper = customerMapper;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
        this.getCustomersByIdsUseCase = getCustomersByIdsUseCase;
    }

    @GetMapping
    @Operation(summary = "Search customers by phone and get first 10 customers", description = "Returns customers whose phone number contains the given digit sequence. " +
            "Returns 10 customers sorted by last and first name asc, if no phone provided, ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "customers", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerSearchResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid phone search pattern", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<CustomerSearchResponse>> getAll(
            @Parameter(description = "Phone digits to search (4–11 digits)", example = "9161", required = false)
            @RequestParam(name = "phone", required = false)
            @Pattern(regexp = "^\\d{4,11}$", message = "Phone search must be 4 to 11 digits")
            @Nullable String phone) {
        log.info("[GET] Searching customers by phone: {}", phone);
        var results = customerQueryUseCase.searchByPhone(phone);
        log.info("[GET] Found {} customers matching phone: {}", results.size(), phone);
        return ResponseEntity.ok(mapper.toSearchResponses(results));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by UUID", description = "Returns full customer profile by UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer returned", content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CustomerResponse> getById(
            @Parameter(description = "Customer UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable("id") UUID id) {
        log.info("[GET] Fetching customer by id: {}", id);
        var customer = getCustomerByIdUseCase.getById(id);
        return ResponseEntity.ok(mapper.toResponse(customer));
    }

    @GetMapping("/batch")
    @Operation(
            summary = "Batch get customers by UUIDs",
            description = "Returns a flat list of customer records for the provided UUIDs. UUIDs that do not match any record are silently omitted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid ids parameter — missing, malformed UUID, or more than 100 elements",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<CustomerResponse>> getCustomersBatch(
            @Parameter(description = "Comma-separated list of customer UUIDs, 1–100 elements",
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6,9cb12d11-0000-0000-0000-000000000002")
            @RequestParam(name = "ids")
            @NotEmpty(message = "ids must not be empty")
            @Size(max = 100, message = "ids must contain at most 100 elements")
            List<UUID> ids) {
        log.info("[GET] Batch fetch customers ids count={}", ids.size());
        var distinctIds = ids.stream().distinct().toList();
        var customers = getCustomersByIdsUseCase.execute(distinctIds);
        return ResponseEntity.ok(mapper.toResponses(customers));
    }
}
