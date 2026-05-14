# Task 003: Add Batch Endpoint Tests to `EquipmentQueryControllerTest`

> **Applied Skill:** `spring-mvc-controller-test` — `@MockitoBean` declarations; `@Nested` class per endpoint;
> happy-path `@Test` methods; `BadRequest` sub-nested class for validation scenarios.

## 1. Objective

Register `GetEquipmentByIdsUseCase` as a `@MockitoBean` in the test class and add a `@Nested class GetBatchEquipments`
covering the three happy-path scenarios (found, not found, empty) and three bad-request scenarios
(missing `ids`, over-100 list, non-positive value).

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/equipment/web/query/EquipmentQueryControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following imports to the existing import block:

```java
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdsUseCase;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
```

**Code to Add/Replace:**

### Step A — Add `@MockitoBean` declaration for the new use case

* **Location:** Inside the `EquipmentQueryControllerTest` class body, immediately after the existing
  `@MockitoBean private SearchEquipmentsUseCase searchUseCase;` declaration.

* **Current code (context lines):**

```java
    @MockitoBean
    private SearchEquipmentsUseCase searchUseCase;

    @MockitoBean
    private EquipmentQueryMapper mapper;
```

* **Snippet (replace with):**

```java
    @MockitoBean
    private SearchEquipmentsUseCase searchUseCase;

    @MockitoBean
    private GetEquipmentByIdsUseCase getByIds;

    @MockitoBean
    private EquipmentQueryMapper mapper;
```

---

### Step B — Add `@Nested class GetBatchEquipments`

* **Location:** Inside the `EquipmentQueryControllerTest` class body, immediately after the closing `}` of
  the existing `@Nested class GetRequests` block.

* **Snippet:**

```java
    @Nested
    class GetBatchEquipments {

        @Test
        void allFound_returns200WithList() throws Exception {
            var domain = mock(Equipment.class);
            var response = mock(EquipmentResponse.class);

            given(getByIds.execute(any())).willReturn(List.of(domain));
            given(mapper.toResponses(List.of(domain))).willReturn(List.of(response));

            mockMvc.perform(get(API_EQUIPMENTS + "/batch")
                            .queryParam("ids", "1,2")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(getByIds).execute(any());
        }

        @Test
        void noneFound_returns200WithEmptyList() throws Exception {
            given(getByIds.execute(any())).willReturn(List.of());
            given(mapper.toResponses(List.of())).willReturn(List.of());

            mockMvc.perform(get(API_EQUIPMENTS + "/batch")
                            .queryParam("ids", "99,100")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Nested
        class BadRequest {

            @Test
            void whenIdsIsMissing() throws Exception {
                mockMvc.perform(get(API_EQUIPMENTS + "/batch")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenIdCountExceeds100() throws Exception {
                var ids = LongStream.rangeClosed(1, 101)
                        .mapToObj(Long::toString)
                        .collect(Collectors.joining(","));

                mockMvc.perform(get(API_EQUIPMENTS + "/batch")
                                .queryParam("ids", ids)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void whenIdsContainsNonPositiveValue() throws Exception {
                mockMvc.perform(get(API_EQUIPMENTS + "/batch")
                                .queryParam("ids", "1,-5,3")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            }
        }
    }
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests EquipmentQueryControllerTest
```

All tests in `EquipmentQueryControllerTest` must pass (pre-existing tests are unaffected; the new nested
class adds 5 tests on top of the existing suite).
