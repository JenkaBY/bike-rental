# Task 007: Update EquipmentQueryControllerTest to Cover the q Parameter

> **Applied Skill:** `spring-mvc-controller-test` — `@ApiTest` / `@MockitoBean` pattern; `@Nested` structure
> per method; stub alignment after mapper signature change.

## 1. Objective

Fix the two existing `searchEquipments_*` tests broken by the mapper signature change (tasks 001 and 005), and
add a new test covering a request that includes the `q` query parameter.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/equipment/web/query/EquipmentQueryControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — all required imports are already present.

**Code to Add/Replace:**

### 3a. Fix `searchEquipments_found` — update stale `SearchEquipmentsQuery` constructor and `toSearchQuery` stub

* **Location:** The existing `searchEquipments_found` test method inside the `GetRequests` nested class.

* **Current code:**

```java
        @Test
        void searchEquipments_found() throws Exception {
            var domain = mock(Equipment.class);

            var response = mock(EquipmentResponse.class);
            // mapper.toSearchQuery(...) will be called by controller; provide a valid query object
            given(mapper.toSearchQuery(any(), any(), any())).willReturn(new com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase.SearchEquipmentsQuery(
                    null, null, new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            // create a properly typed Page<Equipment> to avoid generic inference issues
            var page = new Page<>(List.of(domain), 1L, new PageRequest(20, 0, null));

            given(searchUseCase.execute(any())).willReturn(page);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_EQUIPMENTS).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchUseCase).execute(any());
        }
```

* **Snippet (replace with):**

```java
        @Test
        void searchEquipments_found() throws Exception {
            var domain = mock(Equipment.class);

            var response = mock(EquipmentResponse.class);
            given(mapper.toSearchQuery(any(), any(), any(), any())).willReturn(new com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase.SearchEquipmentsQuery(
                    null, null, null, new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            var page = new Page<>(List.of(domain), 1L, new PageRequest(20, 0, null));

            given(searchUseCase.execute(any())).willReturn(page);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_EQUIPMENTS).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchUseCase).execute(any());
        }
```

---

### 3b. Fix `searchEquipments_empty` — update stale `SearchEquipmentsQuery` constructor and `toSearchQuery` stub

* **Location:** The existing `searchEquipments_empty` test method inside the `GetRequests` nested class, immediately
  below `searchEquipments_found`.

* **Current code:**

```java
        @Test
        void searchEquipments_empty() throws Exception {
            given(mapper.toSearchQuery(any(), any(), any())).willReturn(new com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase.SearchEquipmentsQuery(
                    null, null, new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            given(searchUseCase.execute(any())).willReturn(com.github.jenkaby.bikerental.shared.domain.model.vo.Page.empty(new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            mockMvc.perform(get(API_EQUIPMENTS).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchUseCase).execute(any());
        }
```

* **Snippet (replace with):**

```java
        @Test
        void searchEquipments_empty() throws Exception {
            given(mapper.toSearchQuery(any(), any(), any(), any())).willReturn(new com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase.SearchEquipmentsQuery(
                    null, null, null, new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            given(searchUseCase.execute(any())).willReturn(com.github.jenkaby.bikerental.shared.domain.model.vo.Page.empty(new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            mockMvc.perform(get(API_EQUIPMENTS).accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchUseCase).execute(any());
        }
```

---

### 3c. Add new test `searchEquipments_withQ_returnsOk`

* **Location:** Add this new test method directly after `searchEquipments_empty` and before
  `getEquipmentById_invalidId`, inside the `GetRequests` nested class.

* **Snippet:**

```java
        @Test
        void searchEquipments_withQ_returnsOk() throws Exception {
            var domain = mock(Equipment.class);
            var response = mock(EquipmentResponse.class);
            given(mapper.toSearchQuery(any(), any(), any(), any())).willReturn(new com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase.SearchEquipmentsQuery(
                    null, null, "bike", new com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest(20, 0, null)));

            var page = new Page<>(List.of(domain), 1L, new PageRequest(20, 0, null));

            given(searchUseCase.execute(any())).willReturn(page);
            given(mapper.toResponse(domain)).willReturn(response);

            mockMvc.perform(get(API_EQUIPMENTS)
                            .param("q", "bike")
                            .accept(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchUseCase).execute(any());
        }
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests EquipmentQueryControllerTest
```

> All tests in `EquipmentQueryControllerTest` must pass: the two fixed existing tests plus the new
> `searchEquipments_withQ_returnsOk` test.
