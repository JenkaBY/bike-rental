package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalSignaturesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetSignaturePdfUseCase;
import com.github.jenkaby.bikerental.agreement.web.mapper.SignatureWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.SignatureSummaryResponse;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/rentals/{rentalId}/signatures")
@Slf4j
@Tag(name = OpenApiConfig.Tags.AGREEMENTS)
class RentalSignatureQueryController {

    private final FindRentalSignaturesUseCase findRentalSignaturesUseCase;
    private final GetSignaturePdfUseCase getSignaturePdfUseCase;
    private final SignatureWebMapper mapper;

    RentalSignatureQueryController(FindRentalSignaturesUseCase findRentalSignaturesUseCase,
                                   GetSignaturePdfUseCase getSignaturePdfUseCase,
                                   SignatureWebMapper mapper) {
        this.findRentalSignaturesUseCase = findRentalSignaturesUseCase;
        this.getSignaturePdfUseCase = getSignaturePdfUseCase;
        this.mapper = mapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List rental signatures",
            description = "Returns the signature summaries for a rental as JSON (0 or 1 entry, since a rental has "
                    + "at most one signature). Select this representation with Accept: application/json.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signature summaries",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SignatureSummaryResponse.class))))
    })
    public ResponseEntity<List<SignatureSummaryResponse>> list(@PathVariable("rentalId") @Positive Long rentalId) {
        log.info("[GET] Listing signatures for rental {}", rentalId);
        var summaries = findRentalSignaturesUseCase.execute(rentalId);
        return ResponseEntity.ok(mapper.toResponses(summaries));
    }

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download the signed agreement PDF",
            description = "Returns the signed agreement document for a rental as a PDF attachment. Select this "
                    + "representation with Accept: application/pdf. There is no signatureId in the path since a "
                    + "rental has at most one signature.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signed agreement PDF",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Rental has not been signed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<byte[]> download(@PathVariable("rentalId") @Positive Long rentalId) {
        log.info("[GET] Downloading signature PDF for rental {}", rentalId);
        byte[] pdf = getSignaturePdfUseCase.execute(rentalId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"rental-%d-agreement.pdf\"".formatted(rentalId))
                .body(pdf);
    }
}
