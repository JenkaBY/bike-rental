package com.github.jenkaby.bikerental.rental.web.command;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.rental.application.usecase.*;
import com.github.jenkaby.bikerental.rental.domain.exception.InsufficientPrepaymentException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.rental.web.command.mapper.RentalCommandMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalCommandController.class)
@DisplayName("Rental Command Controller WebMvc Tests")
class RentalCommandControllerTest {

    private static final String API_RENTALS = "/api/rentals";
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final Long VALID_EQUIPMENT_ID = 1L;
    private static final Duration VALID_DURATION = Duration.ofHours(2);
    private static final LocalDateTime VALID_START_TIME = LocalDateTime.parse("2026-02-07T10:00:00");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateRentalUseCase createRentalUseCase;

    @MockitoBean
    private UpdateRentalUseCase updateRentalUseCase;

    @MockitoBean
    private RecordPrepaymentUseCase recordPrepaymentUseCase;

    @MockitoBean
    private ReturnEquipmentUseCase returnEquipmentUseCase;

    @MockitoBean
    private RentalCommandMapper commandMapper;

    @MockitoBean
    private RentalQueryMapper queryMapper;

    @Nested
    @DisplayName("POST /api/rentals")
    class PostRentals {

        @Nested
        @DisplayName("Should return 201 Created")
        class ShouldReturn201 {

            @Test
            @DisplayName("when request contains all required fields")
            void whenRequestContainsAllRequiredFields() throws Exception {
                CreateRentalRequest request = new CreateRentalRequest(
                        VALID_CUSTOMER_ID,
                        VALID_EQUIPMENT_ID,
                        VALID_DURATION,
                        null
                );

                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(commandMapper.toCreateCommand(any(CreateRentalRequest.class)))
                        .willReturn(mock(CreateRentalUseCase.CreateRentalCommand.class));
                given(createRentalUseCase.execute(any(CreateRentalUseCase.CreateRentalCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());

                verify(createRentalUseCase).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }

            @Test
            @DisplayName("when request contains all fields including optional tariffId")
            void whenRequestContainsAllFieldsIncludingOptionalTariffId() throws Exception {
                CreateRentalRequest request = new CreateRentalRequest(
                        VALID_CUSTOMER_ID,
                        VALID_EQUIPMENT_ID,
                        VALID_DURATION,
                        123L
                );

                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(commandMapper.toCreateCommand(any(CreateRentalRequest.class)))
                        .willReturn(mock(CreateRentalUseCase.CreateRentalCommand.class));
                given(createRentalUseCase.execute(any(CreateRentalUseCase.CreateRentalCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());

                verify(createRentalUseCase).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }
        }

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            @ParameterizedTest
            @NullSource
            @DisplayName("when customerId is null")
            void whenCustomerIdIsNull(UUID customerId) throws Exception {
                CreateRentalRequest request = new CreateRentalRequest(
                        customerId,
                        VALID_EQUIPMENT_ID,
                        VALID_DURATION,
                        null
                );

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Customer ID is required")));

                verify(createRentalUseCase, never()).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }

            @ParameterizedTest
            @NullSource
            @DisplayName("when equipmentId is null")
            void whenEquipmentIdIsNull(Long equipmentId) throws Exception {
                CreateRentalRequest request = new CreateRentalRequest(
                        VALID_CUSTOMER_ID,
                        equipmentId,
                        VALID_DURATION,
                        null
                );

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Equipment ID is required")));

                verify(createRentalUseCase, never()).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }

            @ParameterizedTest
            @NullSource
            @DisplayName("when duration is null")
            void whenDurationIsNull(Duration duration) throws Exception {
                CreateRentalRequest request = new CreateRentalRequest(
                        VALID_CUSTOMER_ID,
                        VALID_EQUIPMENT_ID,
                        duration,
                        null
                );

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Duration is required")));

                verify(createRentalUseCase, never()).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }

        }
    }

    @Nested
    @DisplayName("POST /api/rentals/draft")
    class PostDraft {

        @Nested
        @DisplayName("Should return 201 Created")
        class ShouldReturn201 {

            @Test
            @DisplayName("when creating draft rental")
            void whenCreatingDraftRental() throws Exception {
                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(createRentalUseCase.execute(any(CreateRentalUseCase.CreateDraftCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(post(API_RENTALS + "/draft"))
                        .andExpect(status().isCreated());

                verify(createRentalUseCase).execute(any(CreateRentalUseCase.CreateDraftCommand.class));
            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/rentals/{id}")
    class PatchRentals {

        @Nested
        @DisplayName("Should return 200 OK")
        class ShouldReturn200 {

            @Test
            @DisplayName("when patch request is valid with REPLACE operation")
            void whenPatchRequestIsValidWithReplaceOperation() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "/customerId",
                                UUID.randomUUID().toString()
                        ))
                );

                given(commandMapper.toPatchMap(any(RentalUpdateJsonPatchRequest.class)))
                        .willReturn(mock(Map.class));
                given(updateRentalUseCase.execute(anyLong(), any(Map.class)))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), any(Map.class));
            }

            @Test
            @DisplayName("when patch request is valid with ADD operation")
            void whenPatchRequestIsValidWithAddOperation() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.ADD,
                                "/equipmentId",
                                123L
                        ))
                );

                given(commandMapper.toPatchMap(any(RentalUpdateJsonPatchRequest.class)))
                        .willReturn(mock(Map.class));
                given(updateRentalUseCase.execute(anyLong(), any(Map.class)))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), any(Map.class));
            }

            @ParameterizedTest
            @ValueSource(strings = {"/customerId", "/equipmentId", "/tariffId", "/duration", "/status"})
            @DisplayName("when patch request uses valid paths")
            void whenPatchRequestUsesValidPaths(String path) throws Exception {
                Object value = switch (path) {
                    case "/customerId" -> UUID.randomUUID().toString();
                    case "/equipmentId", "/tariffId" -> 123L;
                    case "/duration" -> "PT2H";
                    case "/status" -> "ACTIVE";
                    default -> "value";
                };

                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                path,
                                value
                        ))
                );

                // Special handling for duration/startTime - they must be together
                if ("/duration".equals(path)) {
                    List<RentalPatchOperation> operations = List.of(
                            new RentalPatchOperation(JsonPatchOperation.REPLACE, "/duration", "PT2H")
                    );
                    request = new RentalUpdateJsonPatchRequest(operations);
                }

                given(commandMapper.toPatchMap(any(RentalUpdateJsonPatchRequest.class)))
                        .willReturn(mock(Map.class));
                given(updateRentalUseCase.execute(anyLong(), any(Map.class)))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), any(Map.class));
            }

            @ParameterizedTest
            @ValueSource(strings = {"DRAFT", "ACTIVE", "COMPLETED", "CANCELLED"})
            @DisplayName("when patch request uses valid status values")
            void whenPatchRequestUsesValidStatusValues(String status) throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "/status",
                                status
                        ))
                );

                given(commandMapper.toPatchMap(any(RentalUpdateJsonPatchRequest.class)))
                        .willReturn(mock(Map.class));
                given(updateRentalUseCase.execute(anyLong(), any(Map.class)))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), any(Map.class));
            }

            @Test
            @DisplayName("when patch request contains duration and startTime together")
            void whenPatchRequestContainsDurationAndStartTimeTogether() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(
                                new RentalPatchOperation(JsonPatchOperation.REPLACE, "/duration", "PT2H")
                        )
                );

                given(commandMapper.toPatchMap(any(RentalUpdateJsonPatchRequest.class)))
                        .willReturn(mock(Map.class));
                given(updateRentalUseCase.execute(anyLong(), any(Map.class)))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), any(Map.class));
            }
        }

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            @ParameterizedTest
            @NullSource
            @DisplayName("when operations list is null")
            void whenOperationsListIsNull(List<RentalPatchOperation> operations) throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(operations);

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Patch operations are required")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("when operations list is empty")
            void whenOperationsListIsEmpty(List<RentalPatchOperation> operations) throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(operations);

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("At least one patch operation is required")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when operation op is null")
            void whenOperationOpIsNull() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                null,
                                "/customerId",
                                UUID.randomUUID().toString()
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Operation 'op' is required")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when operation path is null")
            void whenOperationPathIsNull() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                null,
                                UUID.randomUUID().toString()
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Path is required")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @ParameterizedTest
            @ValueSource(strings = {"", "   ", "\t"})
            @DisplayName("when operation path is blank")
            void whenOperationPathIsBlank(String path) throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                path,
                                UUID.randomUUID().toString()
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Path is required")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when operation path does not start with '/'")
            void whenOperationPathDoesNotStartWithSlash() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "customerId",
                                UUID.randomUUID().toString()
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Path must start with '/'")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when operation path is not in allowed paths")
            void whenOperationPathIsNotInAllowedPaths() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "/invalidPath",
                                "value"
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Path '/invalidPath' is not allowed")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when operation value is null for REPLACE operation")
            void whenOperationValueIsNullForReplaceOperation() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "/customerId",
                                null
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Value is required for operation 'replace'")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when operation value is null for ADD operation")
            void whenOperationValueIsNullForAddOperation() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.ADD,
                                "/customerId",
                                null
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Value is required for operation 'add'")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when status value is invalid")
            void whenStatusValueIsInvalid() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "/status",
                                "INVALID_STATUS"
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Invalid status value 'INVALID_STATUS'")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/rentals/{id}/prepayments")
    class PostRentalPrepayments {

        @ParameterizedTest
        @MethodSource("invalidPrepaymentRequestTestCases")
        @DisplayName("Should return 400 Bad Request when request is invalid")
        void shouldReturn400WhenRequestIsInvalid(
                RecordPrepaymentRequest request,
                String expectedErrorMessage,
                String expectedTitle,
                String expectedDetail) throws Exception {
            mockMvc.perform(post(API_RENTALS + "/1/prepayments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value(expectedTitle))
                    .andExpect(jsonPath("$.detail").value(containsString(expectedDetail)));

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
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Insufficient prepayment"))
                    .andExpect(jsonPath("$.detail").value(containsString("at least the estimated cost")));
        }

        private static Stream<Arguments> invalidPrepaymentRequestTestCases() {
            return Stream.of(
                    Arguments.of(
                            new RecordPrepaymentRequest(BigDecimal.ZERO, PaymentMethod.CASH, "operator-1"),
                            "Amount must be positive",
                            "Bad Request",
                            "Amount must be positive"
                    ),
                    Arguments.of(
                            new RecordPrepaymentRequest(new BigDecimal("-10.00"), PaymentMethod.CARD, "operator-1"),
                            "Amount must be positive",
                            "Bad Request",
                            "Amount must be positive"
                    ),
                    Arguments.of(
                            new RecordPrepaymentRequest(new BigDecimal("100.00"), null, "op-1"),
                            "Payment method is required",
                            "Bad Request",
                            "Payment method is required"
                    ),
                    Arguments.of(
                            new RecordPrepaymentRequest(new BigDecimal("100.00"), PaymentMethod.CASH, null),
                            "Operator is required",
                            "Bad Request",
                            "Operator is required"
                    ),
                    Arguments.of(
                            new RecordPrepaymentRequest(new BigDecimal("100.00"), PaymentMethod.CASH, "   "),
                            "Operator is required",
                            "Bad Request",
                            "Operator is required"
                    )
            );
        }
    }

    @Nested
    @DisplayName("POST /api/rentals/return")
    class PostRentalReturn {

        @Nested
        @DisplayName("Should return 200 OK")
        class ShouldReturn200 {

            @ParameterizedTest(name = "{1}")
            @MethodSource("validReturnRequestTestCases")
            @DisplayName("when return request contains valid identifier")
            void whenReturnRequestContainsValidIdentifier(ReturnEquipmentRequest request, String scenario) throws Exception {
                var returnResult = buildReturnResult();
                given(commandMapper.toReturnCommand(any(ReturnEquipmentRequest.class)))
                        .willReturn(mock(ReturnEquipmentUseCase.ReturnEquipmentCommand.class));
                given(returnEquipmentUseCase.execute(any()))
                        .willReturn(returnResult);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(post(API_RENTALS + "/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(returnEquipmentUseCase).execute(any(ReturnEquipmentUseCase.ReturnEquipmentCommand.class));
            }

            private static Stream<Arguments> validReturnRequestTestCases() {
                return Stream.of(
                        Arguments.of(
                                new ReturnEquipmentRequest(1L, null, null, PaymentMethod.CASH, "operator-1"),
                                "identified by rentalId"
                        ),
                        Arguments.of(
                                new ReturnEquipmentRequest(null, 1L, null, null, null),
                                "identified by equipmentId"
                        ),
                        Arguments.of(
                                new ReturnEquipmentRequest(null, null, "BIKE-001", null, null),
                                "identified by equipmentUid"
                        )
                );
            }

            private ReturnEquipmentResult buildReturnResult() {
                Rental rental = mock(Rental.class);
                when(rental.getId()).thenReturn(1L);

                Money zero = Money.zero();
                RentalCost cost = mock(RentalCost.class);
                when(cost.baseCost()).thenReturn(zero);
                when(cost.overtimeCost()).thenReturn(zero);
                when(cost.actualMinutes()).thenReturn(60);
                when(cost.billableMinutes()).thenReturn(60);
                when(cost.plannedMinutes()).thenReturn(60);
                when(cost.overtimeMinutes()).thenReturn(0);
                when(cost.forgivenessApplied()).thenReturn(false);
                when(cost.calculationMessage()).thenReturn("OK");

                return new ReturnEquipmentResult(rental, cost, zero, null);
            }
        }

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            @ParameterizedTest(name = "{1}")
            @MethodSource("invalidReturnRequestTestCases")
            @DisplayName("when return request is invalid")
            void whenReturnRequestIsInvalid(ReturnEquipmentRequest request, String scenario) throws Exception {
                mockMvc.perform(post(API_RENTALS + "/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString(
                                "At least one of rentalId, equipmentId, or equipmentUid must be provided")));

                verify(returnEquipmentUseCase, never()).execute(any(ReturnEquipmentUseCase.ReturnEquipmentCommand.class));
            }

            private static Stream<Arguments> invalidReturnRequestTestCases() {
                return Stream.of(
                        Arguments.of(
                                new ReturnEquipmentRequest(null, null, null, null, null),
                                "all identifiers are null"
                        ),
                        Arguments.of(
                                new ReturnEquipmentRequest(null, null, "", null, null),
                                "equipmentUid is empty, rentalId and equipmentId are null"
                        ),
                        Arguments.of(
                                new ReturnEquipmentRequest(null, null, "   ", null, null),
                                "equipmentUid is blank, rentalId and equipmentId are null"
                        )
                );
            }
        }
    }
}
