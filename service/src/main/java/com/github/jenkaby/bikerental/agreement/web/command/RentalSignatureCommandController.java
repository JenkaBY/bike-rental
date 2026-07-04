package com.github.jenkaby.bikerental.agreement.web.command;

import com.github.jenkaby.bikerental.agreement.application.usecase.SignAgreementUseCase;
import com.github.jenkaby.bikerental.agreement.web.command.dto.SignAgreementRequest;
import com.github.jenkaby.bikerental.agreement.web.command.dto.SignatureCreatedResponse;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.web.util.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(path = "/api/rentals/{rentalId}/signatures", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
@Tag(name = OpenApiConfig.Tags.AGREEMENTS)
class RentalSignatureCommandController {

    private final SignAgreementUseCase signAgreementUseCase;

    RentalSignatureCommandController(SignAgreementUseCase signAgreementUseCase) {
        this.signAgreementUseCase = signAgreementUseCase;
    }

    @PostMapping
    @Operation(summary = "Sign the rental agreement",
            description = "Signs the agreement for a rental in AWAITING_SIGNATURE status: renders and stores the PDF, "
                    + "then atomically transitions the rental to ACTIVE. The rental object is not returned; a 201 "
                    + "response means signing and activation both succeeded.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agreement signed and rental activated",
                    content = @Content(schema = @Schema(implementation = SignatureCreatedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid signature image",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rental or customer not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Already signed, rental not awaiting signature, "
                    + "rental version mismatch, no active template, or requested template not the active one",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SignatureCreatedResponse> sign(
            @PathVariable("rentalId") @Positive Long rentalId,
            @Valid @RequestBody SignAgreementRequest request,
            @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent,
            HttpServletRequest httpRequest) {
        log.info("[POST] Signing agreement for rental {}", rentalId);
        var command = new SignAgreementUseCase.SignAgreementCommand(
                rentalId,
                request.signaturePng(),
                request.rentalVersion(),
                request.templateId(),
                request.operatorId(),
                WebUtils.resolveClientIp(httpRequest),
                userAgent);
        var result = signAgreementUseCase.execute(command);
        var response = new SignatureCreatedResponse(result.signatureId(), result.signedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
