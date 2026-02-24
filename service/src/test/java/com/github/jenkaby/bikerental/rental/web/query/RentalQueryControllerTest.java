package com.github.jenkaby.bikerental.rental.web.query;

import com.github.jenkaby.bikerental.rental.application.usecase.FindRentalsUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.GetRentalByIdUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalResponse;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSummaryResponse;
import com.github.jenkaby.bikerental.rental.web.query.mapper.RentalQueryMapper;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.mapper.PageMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = RentalQueryController.class)
class RentalQueryControllerTest {

    private static final String API_RENTALS = "/api/rentals";
    private static final Long RENTAL_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetRentalByIdUseCase getRentalByIdUseCase;

    @MockitoBean
    private FindRentalsUseCase findRentalsUseCase;

    @MockitoBean
    private RentalQueryMapper mapper;

    @MockitoBean
    private PageMapper pageMapper;

    @Nested
    class GetRentalById {

        @Test
        void getRentalById_found() throws Exception {
            var rental = mock(Rental.class);
            var response = mock(RentalResponse.class);

            given(getRentalByIdUseCase.execute(RENTAL_ID)).willReturn(rental);
            given(mapper.toResponse(rental)).willReturn(response);

            mockMvc.perform(get(API_RENTALS + "/{id}", RENTAL_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void getRentalById_invalidId() throws Exception {
            mockMvc.perform(get(API_RENTALS + "/abc")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetRentals {

        static Stream<Arguments> searchParameterCombinations() {
            var customerId = UUID.randomUUID().toString();
            return Stream.of(
                    Arguments.of("status only", "ACTIVE", null),
                    Arguments.of("customerId only", null, customerId),
                    Arguments.of("both params", "ACTIVE", customerId),
                    Arguments.of("no params", null, null)
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("searchParameterCombinations")
        void getRentals_returnsOk(String description, String status, String customerId) throws Exception {
            var pageRequest = new PageRequest(20, 0, null);
            var page = new Page<>(List.of(mock(Rental.class)), 1L, pageRequest);

            given(pageMapper.toPageRequest(any())).willReturn(pageRequest);
            given(findRentalsUseCase.execute(any(FindRentalsUseCase.FindRentalsQuery.class))).willReturn(page);
            given(mapper.toRentalSummaryResponse(any(Rental.class))).willReturn(mock(RentalSummaryResponse.class));

            var request = get(API_RENTALS).accept(MediaType.APPLICATION_JSON);
            if (status != null) request = request.param("status", status);
            if (customerId != null) request = request.param("customerId", customerId);

            mockMvc.perform(request)
                    .andExpect(status().isOk());
        }

        @Test
        void getRentals_invalidStatus() throws Exception {
            mockMvc.perform(get(API_RENTALS)
                            .param("status", "INVALID_STATUS")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

}
