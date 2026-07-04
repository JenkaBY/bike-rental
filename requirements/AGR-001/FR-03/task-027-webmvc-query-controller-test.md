<task_file_template>

# Task 027: WebMvc test for AgreementTemplateQueryController

> **Applied Skill:** `spring-mvc-controller-test` / `unit-tests` rule — `@ApiTest(controllers = ...)`;
> `@MockitoBean` for every dependency; `@Nested` per endpoint; path-variable validation negative
> (non-positive / non-numeric id) belongs HERE. Mirrors
> `customer/web/query/CustomerQueryController` test conventions.

## 1. Objective

Cover the query controller: 200 list, 200 get-active, 200 get-by-id, and 400 when `{id}` is not a
positive Long. Confirms the `/active` literal route resolves to `getActive` (not the `{id}` mapping).
Depends on Task 017 (controller), Task 010 (use cases), Task 015 (mapper).

## 2. File to Modify / Create

* **File Path:** `service/src/test/java/com/github/jenkaby/bikerental/agreement/web/query/AgreementTemplateQueryControllerTest.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateSummariesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetActiveAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = AgreementTemplateQueryController.class)
class AgreementTemplateQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindAgreementTemplateSummariesUseCase findAgreementTemplateSummariesUseCase;

    @MockitoBean
    private GetActiveAgreementTemplateUseCase getActiveAgreementTemplateUseCase;

    @MockitoBean
    private GetAgreementTemplateUseCase getAgreementTemplateUseCase;

    @MockitoBean
    private AgreementTemplateWebMapper mapper;

    @Nested
    class GetAgreements {

        @Test
        void shouldReturn200WithSummaryList() throws Exception {
            given(findAgreementTemplateSummariesUseCase.execute()).willReturn(List.of());
            given(mapper.toSummaryResponses(any())).willReturn(List.of());

            mockMvc.perform(get("/api/agreements"))
                    .andExpect(status().isOk());

            verify(findAgreementTemplateSummariesUseCase).execute();
        }
    }

    @Nested
    class GetActiveAgreement {

        @Test
        void shouldReturn200AndResolveActiveRouteNotIdRoute() throws Exception {
            given(getActiveAgreementTemplateUseCase.execute()).willReturn(mock(AgreementTemplate.class));
            given(mapper.toResponse(any(AgreementTemplate.class))).willReturn(mock(AgreementTemplateResponse.class));

            mockMvc.perform(get("/api/agreements/active"))
                    .andExpect(status().isOk());

            verify(getActiveAgreementTemplateUseCase).execute();
            verify(getAgreementTemplateUseCase, never()).execute(any());
        }
    }

    @Nested
    class GetAgreementById {

        @Test
        void shouldReturn200WhenIdIsValid() throws Exception {
            given(getAgreementTemplateUseCase.execute(any(Long.class))).willReturn(mock(AgreementTemplate.class));
            given(mapper.toResponse(any(AgreementTemplate.class))).willReturn(mock(AgreementTemplateResponse.class));

            mockMvc.perform(get("/api/agreements/{id}", 7L))
                    .andExpect(status().isOk());

            verify(getAgreementTemplateUseCase).execute(7L);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void shouldReturn400WhenIdIsNotPositive(long id) throws Exception {
            mockMvc.perform(get("/api/agreements/{id}", id))
                    .andExpect(status().isBadRequest());

            verify(getAgreementTemplateUseCase, never()).execute(any());
        }

        @Test
        void shouldReturn400WhenIdIsNotNumeric() throws Exception {
            mockMvc.perform(get("/api/agreements/{id}", "not-a-number"))
                    .andExpect(status().isBadRequest());

            verify(getAgreementTemplateUseCase, never()).execute(any());
        }
    }
}
```

> `shouldReturn400WhenIdIsNotNumeric` sends `/api/agreements/not-a-number`; because `/active` is a
> literal route and `{id}` is a `Long`, a non-numeric non-`active` segment fails type conversion → 400,
> confirming there is no route ambiguity.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests AgreementTemplateQueryControllerTest
```

</task_file_template>
