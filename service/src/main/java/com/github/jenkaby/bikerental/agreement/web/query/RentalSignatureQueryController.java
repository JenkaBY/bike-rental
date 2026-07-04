package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalSignaturesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetSignaturePdfUseCase;
import com.github.jenkaby.bikerental.agreement.web.mapper.SignatureWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.SignatureSummaryResponse;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    public ResponseEntity<List<SignatureSummaryResponse>> list(@PathVariable("rentalId") @Positive Long rentalId) {
        log.info("[GET] Listing signatures for rental {}", rentalId);
        var summaries = findRentalSignaturesUseCase.execute(rentalId);
        return ResponseEntity.ok(mapper.toResponses(summaries));
    }

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
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
