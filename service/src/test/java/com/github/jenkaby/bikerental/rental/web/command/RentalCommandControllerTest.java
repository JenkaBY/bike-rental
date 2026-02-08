package com.github.jenkaby.bikerental.rental.web.command;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.web.command.dto.CreateRentalRequest;
import com.github.jenkaby.bikerental.rental.web.command.dto.JsonPatchOperation;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalPatchOperation;
import com.github.jenkaby.bikerental.rental.web.command.dto.RentalUpdateJsonPatchRequest;
import com.github.jenkaby.bikerental.rental.web.command.mapper.RentalCommandMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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
                        VALID_START_TIME,
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
                        VALID_START_TIME,
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
                        VALID_START_TIME,
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
                        VALID_START_TIME,
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
                        VALID_START_TIME,
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

            @ParameterizedTest
            @NullSource
            @DisplayName("when startTime is null")
            void whenStartTimeIsNull(LocalDateTime startTime) throws Exception {
                CreateRentalRequest request = new CreateRentalRequest(
                        VALID_CUSTOMER_ID,
                        VALID_EQUIPMENT_ID,
                        VALID_DURATION,
                        startTime,
                        null
                );

                mockMvc.perform(post(API_RENTALS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Start time is required")));

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
            @ValueSource(strings = {"/customerId", "/equipmentId", "/tariffId", "/duration", "/startTime", "/status"})
            @DisplayName("when patch request uses valid paths")
            void whenPatchRequestUsesValidPaths(String path) throws Exception {
                Object value = switch (path) {
                    case "/customerId" -> UUID.randomUUID().toString();
                    case "/equipmentId", "/tariffId" -> 123L;
                    case "/duration" -> "PT2H";
                    case "/startTime" -> "2026-02-07T10:00:00";
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
                if ("/duration".equals(path) || "/startTime".equals(path)) {
                    List<RentalPatchOperation> operations = List.of(
                            new RentalPatchOperation(JsonPatchOperation.REPLACE, "/duration", "PT2H"),
                            new RentalPatchOperation(JsonPatchOperation.REPLACE, "/startTime", "2026-02-07T10:00:00")
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
                                new RentalPatchOperation(JsonPatchOperation.REPLACE, "/duration", "PT2H"),
                                new RentalPatchOperation(JsonPatchOperation.REPLACE, "/startTime", "2026-02-07T10:00:00")
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
            @DisplayName("when duration is provided without startTime")
            void whenDurationIsProvidedWithoutStartTime() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "/duration",
                                "PT2H"
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Both duration and startTime must be provided together")));

                verify(updateRentalUseCase, never()).execute(anyLong(), anyMap());
            }

            @Test
            @DisplayName("when startTime is provided without duration")
            void whenStartTimeIsProvidedWithoutDuration() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "/startTime",
                                "2026-02-07T10:00:00"
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value(containsString("Both duration and startTime must be provided together")));

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
}
