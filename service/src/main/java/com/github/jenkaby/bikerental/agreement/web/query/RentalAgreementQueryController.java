package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalAgreementUseCase;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.RentalAgreementResponse;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(path = "/api/rentals/{rentalId}/agreement", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Tag(name = OpenApiConfig.Tags.AGREEMENTS)
class RentalAgreementQueryController {

    private final FindRentalAgreementUseCase findRentalAgreementUseCase;
    private final AgreementTemplateWebMapper mapper;

    RentalAgreementQueryController(FindRentalAgreementUseCase findRentalAgreementUseCase,
                                   AgreementTemplateWebMapper mapper) {
        this.findRentalAgreementUseCase = findRentalAgreementUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Get the active agreement rendered for a rental",
            description = "Returns the active agreement template's title and content with all placeholders substituted "
                    + "with the rental's customer and cost data, so the customer reads the exact text they will sign. "
                    + "Available only while the rental is AWAITING_SIGNATURE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rendered agreement text",
                    content = @Content(schema = @Schema(implementation = RentalAgreementResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rental or customer not found, or no active template",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Rental is not awaiting signature",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<RentalAgreementResponse> getRentalAgreement(@PathVariable("rentalId") @Positive Long rentalId) {
        log.info("[GET] Rendering agreement text for rental {}", rentalId);
        var view = findRentalAgreementUseCase.execute(rentalId);
        return ResponseEntity.ok(mapper.toRentalAgreementResponse(view));
    }
}
