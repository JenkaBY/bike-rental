# Task 001: Create `RentalAvailabilityQueryController`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\instructions\springboot.instructions.md` — Package-private
> `@RestController` in `web/query/` following the `RentalQueryController` pattern; `@Validated` on class for
> constraint validation; constructor injection; OpenAPI annotations consistent with existing query controllers.

## 1. Objective

Create `RentalAvailabilityQueryController` in `rental/web/query/` exposing
`GET /api/rentals/available-equipments`. The controller accepts optional `q` plus Spring Data's `Pageable` (resolved via
`@PageableDefault`) and delegates entirely to `GetAvailableForRentEquipmentsUseCase`.
`PageMapper.toPageRequest(Pageable)`
converts the Spring pageable to the domain `PageRequest`, consistent with `RentalQueryController`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/RentalAvailabilityQueryController.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.web.query;

import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.rental.application.usecase.GetAvailableForRentEquipmentsUseCase;
import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalAvailabilityQueryMapper;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/rentals", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
@Tag(name = OpenApiConfig.Tags.RENTALS)
class RentalAvailabilityQueryController {

    private final GetAvailableForRentEquipmentsUseCase getAvailableForRentEquipmentsUseCase;
    private final RentalAvailabilityQueryMapper mapper;
    private final PageMapper pageMapper;

    RentalAvailabilityQueryController(
            GetAvailableForRentEquipmentsUseCase getAvailableForRentEquipmentsUseCase,
            RentalAvailabilityQueryMapper mapper,
            PageMapper pageMapper) {
        this.getAvailableForRentEquipmentsUseCase = getAvailableForRentEquipmentsUseCase;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    @GetMapping("/available-equipments")
    @Operation(
            summary = "Get available equipment for rent",
            description = "Returns equipment that is in GOOD condition and not currently occupied by an active or assigned rental. " +
                    "Pagination is best-effort: the returned page may contain fewer items than the requested size."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available equipment page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<AvailableEquipmentResponse>> getAvailableEquipments(
            @Parameter(description = "Partial, case-insensitive text matched OR-style against uid, model, and serialNumber")
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("[GET] Get available equipments for rent: q={}, pageable={}", q, pageable);
        var filter = new EquipmentSearchFilter(q);
        var pageRequest = pageMapper.toPageRequest(pageable);
        var result = getAvailableForRentEquipmentsUseCase.getAvailableEquipments(filter, pageRequest);
        return ResponseEntity.ok(result.map(mapper::toResponse));
    }
}
```

> **Key rules:**
> - The class is **package-private** (no `public` modifier), consistent with `RentalQueryController`.
> - `@Validated` is **not** needed — there are no bean-validation annotations on the method params.
> - `RentalAvailabilityQueryMapper mapper` is the third constructor dependency; it converts the domain
    > `AvailableForRentalEquipment` to the web `AvailableEquipmentResponse`.
> - `result.map(mapper::toResponse)` uses `Page.map()` to transform each element; this is the `Page<T>.map()` method
    > defined on the shared domain `Page` record.
> - `PageMapper.toPageRequest(Pageable)` converts to the domain `PageRequest` — identical pattern to
    `RentalQueryController`.
> - `EquipmentSearchFilter(q)` accepts `null` as `q` (the record has no null-check); `null` means no text filter.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
./gradlew :service:test "-Dspring.profiles.active=test"
```
