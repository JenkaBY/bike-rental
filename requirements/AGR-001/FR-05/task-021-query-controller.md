<task_file_template>

# Task 021: Create the RentalSignatureQueryController (content negotiation)

> **Applied Skill:** `spring-boot-best-practices` — one path, two `@GetMapping`s discriminated by `produces`
> (JSON summary list vs PDF attachment). Empty list when unsigned (NOT 404) for JSON; the PDF use case throws
> `ResourceNotFoundException` when unsigned → the existing global handler returns 404. Zero inline comments. Depends on
> Tasks 010, 018, 019.

## 1. Objective

Create the GET controller at `/api/rentals/{rentalId}/signatures` serving either a JSON summary list or the PDF
attachment based on the `Accept` header.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/query/RentalSignatureQueryController.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
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
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
