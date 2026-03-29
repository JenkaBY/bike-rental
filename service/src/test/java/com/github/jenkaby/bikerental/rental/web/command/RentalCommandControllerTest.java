package com.github.jenkaby.bikerental.rental.web.command;

import com.github.jenkaby.bikerental.finance.PaymentMethod;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InsufficientPrepaymentException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.rental.web.command.mapper.RentalCommandMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyLong;
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
                        List.of(VALID_EQUIPMENT_ID),
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
                        List.of(VALID_EQUIPMENT_ID),
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
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        null
                );

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

                verify(createRentalUseCase, never()).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }

            @ParameterizedTest
            @NullSource
            @DisplayName("when equipmentIds is null")
            void whenEquipmentIdIsNull(Long equipmentId) throws Exception {
                List<Long> eqIds = new ArrayList<>();
                eqIds.add(equipmentId);
                CreateRentalRequest request = new CreateRentalRequest(
                        VALID_CUSTOMER_ID,
                        eqIds,
                        VALID_DURATION,
                        null
                );

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

                verify(createRentalUseCase, never()).execute(any(CreateRentalUseCase.CreateRentalCommand.class));
            }

            @ParameterizedTest
            @NullSource
            @DisplayName("when duration is null")
            void whenDurationIsNull(Duration duration) throws Exception {
                CreateRentalRequest request = new CreateRentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        duration,
                        null
                );

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .willReturn(Map.of());
                given(updateRentalUseCase.execute(anyLong(), anyMap()))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when patch request is valid with ADD operation")
            void whenPatchRequestIsValidWithAddOperation() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.ADD,
                                "/equipmentIds",
                                "[123]"
                        ))
                );

                given(commandMapper.toPatchMap(any(RentalUpdateJsonPatchRequest.class)))
                        .willReturn(Map.of());
                given(updateRentalUseCase.execute(anyLong(), anyMap()))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), anyMap());
            }

            @ParameterizedTest
            @ValueSource(strings = {"/customerId", "/equipmentIds", "/tariffId", "/duration", "/status"})
            @DisplayName("when patch request uses valid paths")
            void whenPatchRequestUsesValidPaths(String path) throws Exception {
                Object value = switch (path) {
                    case "/customerId" -> UUID.randomUUID().toString();
                    case "/tariffId" -> 123L;
                    case "/equipmentIds" -> "[123]";
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
                        .willReturn(Map.of());
                given(updateRentalUseCase.execute(anyLong(), anyMap()))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), anyMap());
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
                        .willReturn(Map.of());
                given(updateRentalUseCase.execute(anyLong(), anyMap()))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), anyMap());
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
                        .willReturn(Map.of());
                given(updateRentalUseCase.execute(anyLong(), anyMap()))
                        .willReturn(mock(Rental.class));
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateRentalUseCase).execute(anyLong(), anyMap());
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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @ParameterizedTest
            @DisplayName("when EquipmentIds value is invalid")
            @CsvSource(value = {"[", "]", "123", "1,2,3"})
            void whenEquipmentIdsValueIsInvalid(String value) throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.ADD,
                                "/equipmentIds",
                                value
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].code").exists());

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

    @Nested
    @DisplayName("POST /api/rentals/return")
    class PostRentalReturn {

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {


            @DisplayName("when return request is invalid and no ids")
            void whenReturnRequestHasNoIdsIsInvalid() throws Exception {
                var request = new ReturnEquipmentRequest(null, null, null, null, "operator");

                mockMvc.perform(post(API_RENTALS + "/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.instance").value("/api/rentals/return"))
                        .andExpect(jsonPath("$.errors[0].field").value("validIdentifiers"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.assert_true"));

                verify(returnEquipmentUseCase, never()).execute(any(ReturnEquipmentUseCase.ReturnEquipmentCommand.class));
            }

            @Test
            void whenReturnRequestHasListWithEmptyForEquipmentUds() throws Exception {
                var list = new ArrayList<String>();
                list.add("");
                var request = new ReturnEquipmentRequest(null, null, list, null, "operator");

                mockMvc.perform(post(API_RENTALS + "/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.instance").value("/api/rentals/return"))
                        .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                        .andExpect(jsonPath("$.errors[0].field").value("equipmentUids[0]"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.not_blank"));

                verify(returnEquipmentUseCase, never()).execute(any(ReturnEquipmentUseCase.ReturnEquipmentCommand.class));
            }

            @Test
            void whenReturnRequestHasListWithNullForEquipmentUids() throws Exception {
                var list = new ArrayList<String>();
                list.add(" ");
                var request = new ReturnEquipmentRequest(null, null, list, null, "operator");

                mockMvc.perform(post(API_RENTALS + "/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.instance").value("/api/rentals/return"))
                        .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                        .andExpect(jsonPath("$.errors[0].field").value("equipmentUids[0]"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.not_blank"));

                verify(returnEquipmentUseCase, never()).execute(any(ReturnEquipmentUseCase.ReturnEquipmentCommand.class));
            }

            @ParameterizedTest
            @NullAndEmptySource
            void whenOperatorIdIsBlank(String operator) throws Exception {
                var request = new ReturnEquipmentRequest(1L, null, null, null, operator);

                mockMvc.perform(post(API_RENTALS + "/return")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.instance").value("/api/rentals/return"))
                        .andExpect(jsonPath("$.errorCode").value("shared.method_arguments.validation_failed"))
                        .andExpect(jsonPath("$.errors[0].field").value("operatorId"))
                        .andExpect(jsonPath("$.errors[0].code").value("validation.not_blank"));

                verify(returnEquipmentUseCase, never()).execute(any(ReturnEquipmentUseCase.ReturnEquipmentCommand.class));

            }
        }
    }
}
