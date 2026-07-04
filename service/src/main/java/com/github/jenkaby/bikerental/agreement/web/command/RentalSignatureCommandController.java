package com.github.jenkaby.bikerental.agreement.web.command;

import com.github.jenkaby.bikerental.agreement.application.usecase.SignAgreementUseCase;
import com.github.jenkaby.bikerental.agreement.web.command.dto.SignAgreementRequest;
import com.github.jenkaby.bikerental.agreement.web.command.dto.SignatureCreatedResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(path = "/api/rentals/{rentalId}/signatures", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
class RentalSignatureCommandController {

    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final SignAgreementUseCase signAgreementUseCase;

    RentalSignatureCommandController(SignAgreementUseCase signAgreementUseCase) {
        this.signAgreementUseCase = signAgreementUseCase;
    }

    @PostMapping
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
                resolveIpAddress(httpRequest),
                userAgent);
        var result = signAgreementUseCase.execute(command);
        var response = new SignatureCreatedResponse(result.signatureId(), result.signedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String resolveIpAddress(HttpServletRequest httpRequest) {
        String forwardedFor = httpRequest.getHeader(FORWARDED_FOR_HEADER);
        if (StringUtils.hasText(forwardedFor)) {
            for (String token : forwardedFor.split(",")) {
                var trimmed = token.trim();
                if (StringUtils.hasText(trimmed)) {
                    return trimmed;
                }
            }
        }
        return httpRequest.getRemoteAddr();
    }
}
