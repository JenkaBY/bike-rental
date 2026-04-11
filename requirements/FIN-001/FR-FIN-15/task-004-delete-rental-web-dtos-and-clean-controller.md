# Task 004: Delete Rental Web DTOs and Clean RentalCommandMapper + RentalCommandController + RentalCommandControllerTest

> **Applied Skill:** `spring-mvc-controller-test` (test cleanup conventions), `mapstruct-hexagonal` (mapper cleanup).

## 1. Objective

Delete the two prepayment-specific DTO records from the Rental web-command layer (`RecordPrepaymentRequest`,
`PrepaymentResponse`). Because `RentalCommandMapper` declares abstract MapStruct methods that reference both types,
and `RentalCommandController` exposes the endpoint that uses them and holds a `RecordPrepaymentUseCase` field, and
the WebMvc test covers that endpoint, **all four files must be cleaned in a single task** so the project compiles
cleanly at the end.

---

## 2. Files to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/RecordPrepaymentRequest.java`
* **Action:** Delete file entirely.

---

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/PrepaymentResponse.java`
* **Action:** Delete file entirely.

---

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/mapper/RentalCommandMapper.java`
* **Action:** Modify existing file — remove two abstract mapping methods, the `PaymentInfoMapper` field/setter, and
  two imports.

---

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`
* **Action:** Modify existing file — remove the `POST /{id}/prepayments` handler, the `RecordPrepaymentUseCase`
  field, its constructor parameter, and its import.

---

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandControllerTest.java`
* **Action:** Modify existing file — remove the `PostRentalPrepayments` nested test class, the
  `recordPrepaymentUseCase` mock field, and three no-longer-needed imports.

---

## 3. Code Implementation

### 3.1 Delete `RecordPrepaymentRequest.java` and `PrepaymentResponse.java`

Delete both DTO files listed in Section 2. No replacements are needed.

---

### 3.2 Modify `RentalCommandMapper.java` — remove prepayment mapping methods and dead field

**Location:** Inside `public abstract class RentalCommandMapper`.

**Step A — Remove the `paymentInfoMapper` field and its setter.** Find and remove the following block:

```java
    protected PaymentInfoMapper paymentInfoMapper;
```

```java
    @Autowired
    public void setPaymentInfoMapper(PaymentInfoMapper paymentInfoMapper) {
        this.paymentInfoMapper = paymentInfoMapper;
    }
```

**Step B — Remove the `toRecordPrepaymentCommand` abstract method** (including its `@Mapping` annotations):

```java
    @Mapping(target = "rentalId", expression = "java(rentalId)")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "paymentMethod", source = "request.paymentMethod")
    @Mapping(target = "operatorId", source = "request.operatorId")
    public abstract RecordPrepaymentUseCase.RecordPrepaymentCommand toRecordPrepaymentCommand(Long rentalId, RecordPrepaymentRequest request);
```

**Step C — Remove the `toPrepaymentResponse` abstract method** (including its `@Mapping` annotations):

```java
    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "amount", source = "amount")
    public abstract PrepaymentResponse toPrepaymentResponse(PaymentInfo paymentInfo);
```

**Step D — Remove the two now-unused imports** from the top of the file:

```java
import com.github.jenkaby.bikerental.finance.PaymentInfo;
```

```java
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
```

> **Note:** The `PaymentInfoMapper` import and the `PaymentInfoMapper` interface class itself are **not** deleted —
> they are not in-scope for this FR. Only the field/setter referencing them inside `RentalCommandMapper` is removed.

---

### 3.3 Modify `RentalCommandController.java` — remove prepayment endpoint and field

**Step A — Remove the `RecordPrepaymentUseCase` import** from the top of the file:

```java
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
```

**Step B — Remove the `recordPrepaymentUseCase` field declaration:**

```java
    private final RecordPrepaymentUseCase recordPrepaymentUseCase;
```

**Step C — Remove the `recordPrepaymentUseCase` constructor parameter and its assignment.** The constructor currently
reads:

```java
    RentalCommandController(
            CreateRentalUseCase createRentalUseCase,
            UpdateRentalUseCase updateRentalUseCase,
            RecordPrepaymentUseCase recordPrepaymentUseCase,
            ReturnEquipmentUseCase returnEquipmentUseCase,
            RentalCommandMapper commandMapper,
            RentalQueryMapper queryMapper) {
        this.createRentalUseCase = createRentalUseCase;
        this.updateRentalUseCase = updateRentalUseCase;
        this.recordPrepaymentUseCase = recordPrepaymentUseCase;
        this.returnEquipmentUseCase = returnEquipmentUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }
```

Replace it with:

```java
    RentalCommandController(
            CreateRentalUseCase createRentalUseCase,
            UpdateRentalUseCase updateRentalUseCase,
            ReturnEquipmentUseCase returnEquipmentUseCase,
            RentalCommandMapper commandMapper,
            RentalQueryMapper queryMapper) {
        this.createRentalUseCase = createRentalUseCase;
        this.updateRentalUseCase = updateRentalUseCase;
        this.returnEquipmentUseCase = returnEquipmentUseCase;
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
    }
```

**Step D — Remove the entire `recordPrepayment` handler method** (including all `@Operation`, `@ApiResponses`,
and `@ApiResponse` annotations preceding it):

```java
    @PostMapping(value = "/{id}/prepayments")
    @Operation(summary = "Record prepayment for rental")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Prepayment recorded",
                    content = @Content(schema = @Schema(implementation = PrepaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rental not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PrepaymentResponse> recordPrepayment(
            @Parameter(description = "Rental ID", example = "1") @PathVariable(name = "id") Long id,
            @Valid @RequestBody RecordPrepaymentRequest request) {
        log.info("[POST] Recording prepayment for rental {}", id);
        var command = commandMapper.toRecordPrepaymentCommand(id, request);
        var paymentInfo = recordPrepaymentUseCase.execute(command);
        var response = commandMapper.toPrepaymentResponse(paymentInfo);
        log.info("[POST] Prepayment recorded for rental {} with receipt {}", id, paymentInfo.receiptNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
```

---

### 3.4 Modify `RentalCommandControllerTest.java` — remove prepayment test class and dead references

**Step A — Remove three imports** from the top of the file:

```java
import com.github.jenkaby.bikerental.finance.PaymentMethod;
```

```java
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
```

```java
import com.github.jenkaby.bikerental.rental.domain.exception.InsufficientPrepaymentException;
```

**Step B — Remove the `recordPrepaymentUseCase` mock field declaration:**

```java
    @MockitoBean
    private RecordPrepaymentUseCase recordPrepaymentUseCase;
```

**Step C — Remove the entire `PostRentalPrepayments` nested class.** Find and remove the following block in its
entirety (from the blank line before `@Nested` through the closing `}`):

```java

    @Nested
    @DisplayName("POST /api/rentals/{id}/prepayments")
    class PostRentalPrepayments {

        @ParameterizedTest
        @MethodSource("invalidPrepaymentRequestTestCases")
        @DisplayName("Should return 400 Bad Request when request is invalid")
        void shouldReturn400WhenRequestIsInvalid(
                RecordPrepaymentRequest request,
                String expectedTitle) throws Exception {
            mockMvc.perform(post(API_RENTALS + "/1/prepayments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value(expectedTitle))
                    .andExpect(jsonPath("$.detail").value("Validation error"))
                    .andExpect(jsonPath("$.errors[0].code").exists());

            verify(recordPrepaymentUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("Should return 422 when prepayment amount is below estimated cost")
        void shouldReturn422WhenPrepaymentAmountBelowEstimatedCost() throws Exception {
            RecordPrepaymentRequest request = new RecordPrepaymentRequest(
                    new BigDecimal("50.00"),
                    PaymentMethod.CASH,
                    "operator-1"
            );

            var command = new RecordPrepaymentUseCase.RecordPrepaymentCommand(
                    1L,
                    com.github.jenkaby.bikerental.shared.domain.model.vo.Money.of("50.00"),
                    PaymentMethod.CASH,
                    "operator-1"
            );
            given(commandMapper.toRecordPrepaymentCommand(eq(1L), any(RecordPrepaymentRequest.class)))
                    .willReturn(command);
            doThrow(InsufficientPrepaymentException.amountBelowEstimatedCost(1L))
                    .when(recordPrepaymentUseCase).execute(any(RecordPrepaymentUseCase.RecordPrepaymentCommand.class));

            mockMvc.perform(post(API_RENTALS + "/1/prepayments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.title").value("Insufficient prepayment"))
                    .andExpect(jsonPath("$.detail").value(containsString("at least the estimated cost")));
        }

        private static Stream<Arguments> invalidPrepaymentRequestTestCases() {
            return Stream.of(
                    Arguments.of(
                            new RecordPrepaymentRequest(BigDecimal.ZERO, PaymentMethod.CASH, "operator-1"),
                            "Bad Request"
                    ),
                    Arguments.of(
                            new RecordPrepaymentRequest(new BigDecimal("-10.00"), PaymentMethod.CASH, "operator-1"),
                            "Bad Request"
                    ),
                    Arguments.of(
                            new RecordPrepaymentRequest(new BigDecimal("100.00"), null, "op-1"),
                            "Bad Request"
                    ),
                    Arguments.of(
                            new RecordPrepaymentRequest(new BigDecimal("100.00"), PaymentMethod.CASH, null),
                            "Bad Request"
                    ),
                    Arguments.of(
                            new RecordPrepaymentRequest(new BigDecimal("100.00"), PaymentMethod.CASH, "   "),
                            "Bad Request"
                    )
            );
        }
    }
```

---

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalCommandControllerTest
```

Expected result: `BUILD SUCCESSFUL`. All remaining tests in `RentalCommandControllerTest` pass; no tests explore
`POST /api/rentals/{id}/prepayments`.
