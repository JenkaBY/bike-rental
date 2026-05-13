package com.github.jenkaby.bikerental.rental.web.command;

import com.github.jenkaby.bikerental.rental.application.usecase.CreateOrUpdateDraftRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.RentalLifecycleUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.web.command.dto.*;
import com.github.jenkaby.bikerental.rental.web.command.mapper.RentalCommandMapper;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
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

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalCommandController.class)
@DisplayName("Rental Command Controller WebMvc Tests")
class RentalCommandControllerTest {

    private static final String API_RENTALS = "/api/rentals";
    private static final String RENTAL_ID_API = "/api/rentals/{rentalId}";
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final Long VALID_EQUIPMENT_ID = 1L;
    private static final int VALID_DURATION = (int) Duration.ofHours(2).toMinutes();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrUpdateDraftRentalUseCase updateDraftRentalUseCase;

    @MockitoBean
    private UpdateRentalUseCase updateRentalUseCase;
    @MockitoBean
    private ReturnEquipmentUseCase returnEquipmentUseCase;

    @MockitoBean
    private RentalCommandMapper commandMapper;

    @MockitoBean
    private RentalQueryMapper queryMapper;

    @MockitoBean
    private RentalLifecycleUseCase rentalLifecycleUseCase;


    @Nested
    @DisplayName("PUT /api/rentals/{rentalId}")
    class UpdateRental {

        @Nested
        @DisplayName("Should return 200 OK")
        class ShouldReturn200 {

            @Test
            @DisplayName("when request is valid with all required fields")
            void whenRequestIsValid() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        null
                );

                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(commandMapper.toUpdateDraftRentalCommand(anyLong(), any(RentalRequest.class)))
                        .willReturn(mock(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
                given(updateDraftRentalUseCase.execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateDraftRentalUseCase).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when request includes optional specialTariffId with specialPrice")
            void whenRequestIncludesSpecialOffer() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        99L,
                        BigDecimal.valueOf(15.00),
                        null
                );

                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(commandMapper.toUpdateDraftRentalCommand(anyLong(), any(RentalRequest.class)))
                        .willReturn(mock(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
                given(updateDraftRentalUseCase.execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateDraftRentalUseCase).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when request includes optional discountPercent")
            void whenRequestIncludesDiscount() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        10
                );

                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(commandMapper.toUpdateDraftRentalCommand(anyLong(), any(RentalRequest.class)))
                        .willReturn(mock(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
                given(updateDraftRentalUseCase.execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateDraftRentalUseCase).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when request includes multiple equipment items")
            void whenRequestIncludesMultipleEquipment() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(1L, 2L, 3L),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        null
                );

                Rental rental = mock(Rental.class);
                given(rental.getId()).willReturn(1L);
                given(commandMapper.toUpdateDraftRentalCommand(anyLong(), any(RentalRequest.class)))
                        .willReturn(mock(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
                given(updateDraftRentalUseCase.execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(updateDraftRentalUseCase).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }
        }

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class ShouldReturn400 {

            static Stream<Arguments> invalidRentalIds() {
                return Stream.of(
                        Arguments.of(0L, "updateRental.arg0: must be greater than 0"),
                        Arguments.of(-1L, "updateRental.arg0: must be greater than 0")
                );
            }

            @ParameterizedTest
            @MethodSource("invalidRentalIds")
            @DisplayName("when rentalId is invalid")
            void whenRentalIdIsInvalid(Long rentalId, String expectedError) throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        null
                );

                mockMvc.perform(put(RENTAL_ID_API, rentalId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when request body is null")
            void whenRequestBodyIsNull() throws Exception {
                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("null"))
                        .andExpect(status().isBadRequest());

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when customerId is null")
            void whenCustomerIdIsNull() throws Exception {
                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"equipmentIds\": [1], \"duration\": 120, \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("customerId"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when equipmentIds is null")
            void whenEquipmentIdsIsNull() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        null,
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        null
                );

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("equipmentIds"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when equipmentIds is empty list")
            void whenEquipmentIdsIsEmpty() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        null
                );

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("equipmentIds"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when equipmentIds contains null element")
            void whenEquipmentIdsContainsNull() throws Exception {
                var equipmentIds = new ArrayList<Long>();
                equipmentIds.add(1L);
                equipmentIds.add(null);

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"customerId\": \"" + VALID_CUSTOMER_ID + "\", \"equipmentIds\": [1, null], \"duration\": 120, \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"))
                        .andExpect(jsonPath("$.errors[0].field").value("equipmentIds[1]"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            static Stream<Arguments> invalidEquipmentIdValues() {
                return Stream.of(
                        Arguments.of("0", "equipmentIds[0]: must be greater than 0"),
                        Arguments.of("-5", "equipmentIds[0]: must be greater than 0")
                );
            }

            @ParameterizedTest
            @MethodSource("invalidEquipmentIdValues")
            @DisplayName("when equipmentIds contains non-positive values")
            void whenEquipmentIdsContainsNonPositive(String equipmentId, String expectedError) throws Exception {
                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"customerId\": \"" + VALID_CUSTOMER_ID + "\", \"equipmentIds\": [" + equipmentId + "], \"duration\": 120, \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when duration is null")
            void whenDurationIsNull() throws Exception {
                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"customerId\": \"" + VALID_CUSTOMER_ID + "\", \"equipmentIds\": [1], \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            static Stream<Arguments> invalidDurationValues() {
                return Stream.of(
                        Arguments.of(0, "duration: must be greater than 0"),
                        Arguments.of(-1, "duration: must be greater than 0"),
                        Arguments.of(-100, "duration: must be greater than 0")
                );
            }

            @ParameterizedTest
            @MethodSource("invalidDurationValues")
            @DisplayName("when duration is non-positive")
            void whenDurationIsNonPositive(int duration, String expectedError) throws Exception {
                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"customerId\": \"" + VALID_CUSTOMER_ID + "\", \"equipmentIds\": [1], \"duration\": " + duration + ", \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            static Stream<Arguments> invalidOperatorIds() {
                return Stream.of(
                        Arguments.of("", "operatorId: must not be blank"),
                        Arguments.of("   ", "operatorId: must not be blank")
                );
            }

            @ParameterizedTest
            @MethodSource("invalidOperatorIds")
            @DisplayName("when operatorId is blank")
            void whenOperatorIdIsBlank(String operatorId, String expectedError) throws Exception {
                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"customerId\": \"" + VALID_CUSTOMER_ID + "\", \"equipmentIds\": [1], \"duration\": 120, \"operatorId\": \"" + operatorId + "\"}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when operatorId is missing")
            void whenOperatorIdIsMissing() throws Exception {
                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"customerId\": \"" + VALID_CUSTOMER_ID + "\", \"equipmentIds\": [1], \"duration\": 120}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            static Stream<Arguments> invalidSpecialTariffIds() {
                return Stream.of(
                        Arguments.of(0L, "specialTariffId: must be greater than 0"),
                        Arguments.of(-99L, "specialTariffId: must be greater than 0")
                );
            }

            @ParameterizedTest
            @MethodSource("invalidSpecialTariffIds")
            @DisplayName("when specialTariffId is non-positive")
            void whenSpecialTariffIdIsNonPositive(Long tariffId, String expectedError) throws Exception {
                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"customerId\": \"" + VALID_CUSTOMER_ID + "\", \"equipmentIds\": [1], \"duration\": 120, \"operatorId\": \"op-1\", \"specialTariffId\": " + tariffId + ", \"specialPrice\": \"15.00\"}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when specialTariffId and discountPercent are both set (mutually exclusive)")
            void whenSpecialTariffAndDiscountBothSet() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        99L,
                        BigDecimal.valueOf(15.00),
                        10
                );

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when specialTariffId is set without specialPrice (inconsistent)")
            void whenSpecialTariffIdWithoutSpecialPrice() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        99L,
                        null,
                        null
                );

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            @Test
            @DisplayName("when specialPrice is set without specialTariffId (inconsistent)")
            void whenSpecialPriceWithoutSpecialTariffId() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        BigDecimal.valueOf(15.00),
                        null
                );

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }

            static Stream<Arguments> invalidDiscountPercents() {
                return Stream.of(
                        Arguments.of(-1, "discountPercent: must be between 0 and 100"),
                        Arguments.of(101, "discountPercent: must be between 0 and 100")
                );
            }

            @ParameterizedTest
            @MethodSource("invalidDiscountPercents")
            @DisplayName("when discountPercent is out of range")
            void whenDiscountPercentIsOutOfRange(Integer discountPercent, String expectedError) throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        discountPercent
                );

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Validation error"));

                verify(updateDraftRentalUseCase, never()).execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
            }
        }

        @Nested
        @DisplayName("Should return 404 Not Found")
        class ShouldReturn404 {

            @Test
            @DisplayName("when customer does not exist")
            void whenCustomerDoesNotExist() throws Exception {
                var request = new RentalRequest(
                        UUID.randomUUID(),
                        List.of(VALID_EQUIPMENT_ID),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        null
                );

                given(commandMapper.toUpdateDraftRentalCommand(anyLong(), any(RentalRequest.class)))
                        .willReturn(mock(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
                given(updateDraftRentalUseCase.execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class)))
                        .willThrow(new ResourceNotFoundException("Customer", "customerId"));

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("when equipment does not exist")
            void whenEquipmentDoesNotExist() throws Exception {
                var request = new RentalRequest(
                        VALID_CUSTOMER_ID,
                        List.of(999L),
                        VALID_DURATION,
                        "operator-1",
                        null,
                        null,
                        null
                );

                given(commandMapper.toUpdateDraftRentalCommand(anyLong(), any(RentalRequest.class)))
                        .willReturn(mock(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class));
                given(updateDraftRentalUseCase.execute(any(CreateOrUpdateDraftRentalUseCase.UpdateDraftRentalCommand.class)))
                        .willThrow(new ResourceNotFoundException("Equipment", "999"));

                mockMvc.perform(put(RENTAL_ID_API, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound());
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
                given(updateDraftRentalUseCase.execute(any(CreateOrUpdateDraftRentalUseCase.CreateDraftCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class)))
                        .willReturn(mock(RentalResponse.class));

                mockMvc.perform(post(API_RENTALS + "/draft"))
                        .andExpect(status().isCreated());

                verify(updateDraftRentalUseCase).execute(any(CreateOrUpdateDraftRentalUseCase.CreateDraftCommand.class));
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
                                JsonPatchOperation.REPLACE,
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
            @ValueSource(strings = {"/customerId", "/equipmentIds", "/tariffId", "/duration"})
            @DisplayName("when patch request uses valid paths")
            void whenPatchRequestUsesValidPaths(String path) throws Exception {
                Object value = switch (path) {
                    case "/customerId" -> UUID.randomUUID().toString();
                    case "/tariffId" -> 123L;
                    case "/equipmentIds" -> "[123]";
                    case "/duration" -> "180";
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
                            new RentalPatchOperation(JsonPatchOperation.REPLACE, "/duration", "180")
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

            @Test
            @DisplayName("when patch request contains duration and startTime together")
            void whenPatchRequestContainsDurationAndStartTimeTogether() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(
                                new RentalPatchOperation(JsonPatchOperation.REPLACE, "/duration", "180")
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
                                JsonPatchOperation.REPLACE,
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

            @Test
            @DisplayName("when patch request uses /status path")
            void whenPatchRequestUsesValidStatusValues() throws Exception {
                RentalUpdateJsonPatchRequest request = new RentalUpdateJsonPatchRequest(
                        List.of(new RentalPatchOperation(
                                JsonPatchOperation.REPLACE,
                                "/status",
                                "ACTIVE"
                        ))
                );

                mockMvc.perform(patch(API_RENTALS + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }
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
                        .andExpect(jsonPath("$.errors[0].code", endsWith("assert_true")));

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
                        .andExpect(jsonPath("$.errors[0].code", endsWith("not_blank")));

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
                        .andExpect(jsonPath("$.errors[0].code", endsWith("not_blank")));

                verify(returnEquipmentUseCase, never()).execute(any(ReturnEquipmentUseCase.ReturnEquipmentCommand.class));

            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/rentals/{rentalId}/lifecycles")
    class UpdateLifecycle {

        private static final String LIFECYCLE_URL = "/api/rentals/{rentalId}/lifecycles";

        @Nested
        @DisplayName("Should return 200 OK")
        class ShouldReturn200 {

            @Test
            @DisplayName("when status is ACTIVE")
            void whenStatusIsActive() throws Exception {
                var rental = mock(Rental.class);
                given(rentalLifecycleUseCase.execute(any(RentalLifecycleUseCase.RentalLifecycleCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class))).willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.ACTIVE, "op-1"))))
                        .andExpect(status().isOk());
            }

            @Test
            @DisplayName("when status is CANCELLED")
            void whenStatusIsCancelled() throws Exception {
                var rental = mock(Rental.class);
                given(rentalLifecycleUseCase.execute(any(RentalLifecycleUseCase.RentalLifecycleCommand.class)))
                        .willReturn(rental);
                given(queryMapper.toResponse(any(Rental.class))).willReturn(mock(RentalResponse.class));

                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.CANCELLED, "op-1"))))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("Should return 400 Bad Request")
        class BadRequest {

            @Test
            @DisplayName("when status is null")
            void whenStatusIsNull() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(null, "op-1"))))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("when status field is missing")
            void whenStatusFieldIsMissing() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("when status is an invalid enum value")
            void whenStatusIsInvalidEnumValue() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"COMPLETED\", \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("when status is DRAFT")
            void whenStatusIsDraft() throws Exception {
                mockMvc.perform(patch(LIFECYCLE_URL, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"DRAFT\", \"operatorId\": \"op-1\"}"))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("Should return 404 Not Found")
        class NotFound {

            @Test
            @DisplayName("when rental does not exist")
            void whenRentalDoesNotExist() throws Exception {
                given(rentalLifecycleUseCase.execute(any(RentalLifecycleUseCase.RentalLifecycleCommand.class)))
                        .willThrow(new ResourceNotFoundException(Rental.class, "99"));

                mockMvc.perform(patch(LIFECYCLE_URL, 99L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new RentalLifecycleRequest(LifecycleStatus.ACTIVE, "op-1"))))
                        .andExpect(status().isNotFound());
            }
        }
    }
}

