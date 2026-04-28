package com.github.jenkaby.bikerental.customer.web.query;

import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomerByIdUseCase;
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
import jakarta.validation.constraints.Pattern;
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

    CustomerQueryController(CustomerQueryUseCase customerQueryUseCase,
                            GetCustomerByIdUseCase getCustomerByIdUseCase,
                            CustomerWebMapper customerMapper) {
        this.customerQueryUseCase = customerQueryUseCase;
        this.mapper = customerMapper;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
    }

    @GetMapping
    @Operation(summary = "Search customers by phone", description = "Returns customers whose phone number contains the given digit sequence. " +
            "Returns 10 customers sorted by last and first name asc, if no phone provided, ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching customers returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerSearchResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid phone search pattern", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<CustomerSearchResponse>> searchByPhone(
            @Parameter(description = "Phone digits to search (4–11 digits)", example = "9161")
            @RequestParam(name = "phone", required = false)
            @Pattern(regexp = "^\\d{4,11}$", message = "Phone search must be 4 to 11 digits") String phone) {
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
}
