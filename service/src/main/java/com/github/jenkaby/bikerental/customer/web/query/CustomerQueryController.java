package com.github.jenkaby.bikerental.customer.web.query;

import com.github.jenkaby.bikerental.customer.application.usecase.CustomerQueryUseCase;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerSearchResponse;
import com.github.jenkaby.bikerental.customer.web.query.mapper.CustomerQueryMapper;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@Validated
@Slf4j
@Tag(name = OpenApiConfig.Tags.CUSTOMERS)
class CustomerQueryController {

    private final CustomerQueryUseCase customerQueryUseCase;
    private final CustomerQueryMapper mapper;

    CustomerQueryController(CustomerQueryUseCase customerQueryUseCase, CustomerQueryMapper mapper) {
        this.customerQueryUseCase = customerQueryUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Search customers by phone", description = "Returns customers whose phone number contains the given digit sequence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching customers returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerSearchResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid phone search pattern",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<CustomerSearchResponse>> searchByPhone(
            @Parameter(description = "Phone digits to search (4–11 digits)", example = "9161")
            @RequestParam("phone")
            @Pattern(regexp = "^\\d{4,11}$", message = "Phone search must be 4 to 11 digits")
            String phone) {
        log.info("[GET] Searching customers by phone: {}", phone);
        var results = customerQueryUseCase.searchByPhone(phone);
        log.info("[GET] Found {} customers matching phone: {}", results.size(), phone);
        return ResponseEntity.ok(mapper.toSearchResponses(results));
    }
}
