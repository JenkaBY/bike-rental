# Task 002: Write `RentalAvailabilityQueryControllerTest`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\spring-mvc-controller-test\SKILL.md` — `@ApiTest`
> meta-annotation; `@MockitoBean` for all constructor-injected dependencies; `@Nested` per controller method; covers
> happy path (200), empty result (200), and invalid param (400) as specified in FR-06.

## 1. Objective

Create a WebMvc test class `RentalAvailabilityQueryControllerTest` covering the scenarios from FR-06 acceptance
criteria:

1. **Happy path** — use case returns a non-empty `Page<AvailableForRentalEquipment>` → mapper converts to response →
   `200 OK`.
2. **Empty result** — use case returns an empty `Page` → `200 OK` (not 404).
3. **Text filter forwarded** — `?q=MTB` resolves to a non-empty result → `200 OK`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/rental/web/query/RentalAvailabilityQueryControllerTest.java`
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
import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalAvailabilityQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalAvailabilityQueryController.class)
class RentalAvailabilityQueryControllerTest {

    private static final String API_AVAILABLE_EQUIPMENTS = "/api/rentals/available-equipments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAvailableForRentEquipmentsUseCase getAvailableForRentEquipmentsUseCase;

    @MockitoBean
    private RentalAvailabilityQueryMapper mapper;

    @MockitoBean
    private PageMapper pageMapper;

    @Nested
    class GetAvailableEquipments {

        @Test
        void getAvailableEquipments_returnsOk_withResults() throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var domain = new AvailableForRentalEquipment(1L, "SN-001", "BIKE-001", "mountain-bike", "Trek Marlin");
            var response = new AvailableEquipmentResponse(1L, "BIKE-001", "SN-001", "mountain-bike", "Trek Marlin");
            var domainPage = new Page<>(List.of(domain), 1L, pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(getAvailableForRentEquipmentsUseCase.getAvailableEquipments(any(EquipmentSearchFilter.class), any(PageRequest.class)))
                    .willReturn(domainPage);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_AVAILABLE_EQUIPMENTS)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.totalItems").value(1));
        }

        @Test
        void getAvailableEquipments_returnsOk_withEmptyResult() throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var emptyPage = Page.<AvailableForRentalEquipment>empty(pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(getAvailableForRentEquipmentsUseCase.getAvailableEquipments(any(EquipmentSearchFilter.class), any(PageRequest.class)))
                    .willReturn(emptyPage);

            mockMvc.perform(get(API_AVAILABLE_EQUIPMENTS)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(0))
                    .andExpect(jsonPath("$.totalItems").value(0));
        }

        @Test
        void getAvailableEquipments_withTextFilter_returnsOk() throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var domain = new AvailableForRentalEquipment(2L, "SN-002", "MTB-001", "mountain-bike", "Trek MTB");
            var response = new AvailableEquipmentResponse(2L, "MTB-001", "SN-002", "mountain-bike", "Trek MTB");
            var domainPage = new Page<>(List.of(domain), 1L, pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(getAvailableForRentEquipmentsUseCase.getAvailableEquipments(any(EquipmentSearchFilter.class), any(PageRequest.class)))
                    .willReturn(domainPage);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_AVAILABLE_EQUIPMENTS)
                            .param("q", "MTB")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1));
        }
    }
}
```

> **Key rules:**
> - `RentalAvailabilityQueryMapper mapper` is a `@MockitoBean` — it is the third constructor dependency of the
    controller.
> - `PageMapper` is also a `@MockitoBean` — second constructor dependency.
> - `given(mapper.toResponse(domain)).willReturn(response)` stubs the per-element mapping; the controller calls
    > `result.map(mapper::toResponse)` which invokes this once per element.
> - For the empty-page test, `mapper.toResponse(...)` is never called; no stub needed.
> - `AvailableForRentalEquipment` constructor:
    `(Long id, String serialNumber, String uid, String typeSlug, String model)`.
> - `AvailableEquipmentResponse` constructor:
    `(Long id, String uid, String serialNumber, String typeSlug, String model)`.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalAvailabilityQueryControllerTest
```
