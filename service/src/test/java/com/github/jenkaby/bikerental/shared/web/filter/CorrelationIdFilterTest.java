package com.github.jenkaby.bikerental.shared.web.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void cleanMdc() {
        MDC.clear();
    }

    @Test
    void whenHeaderPresent_thenReuseValue() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "existing-correlation-id");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("existing-correlation-id");
    }

    @Test
    void whenHeaderAbsent_thenGenerateUuid() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        var header = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertThat(header).isNotNull().isNotBlank();
    }

    @Test
    void whenHeaderAbsent_thenTwoRequestsGetDifferentIds() throws Exception {
        var response1 = new MockHttpServletResponse();
        var response2 = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response1, new MockFilterChain());
        filter.doFilter(new MockHttpServletRequest(), response2, new MockFilterChain());

        assertThat(response1.getHeader(CorrelationIdFilter.HEADER_NAME))
                .isNotEqualTo(response2.getHeader(CorrelationIdFilter.HEADER_NAME));
    }

    @Test
    void afterFilterCompletes_mdcIsCleared() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}

