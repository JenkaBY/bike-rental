<task_file_template>

# Task 013: Add WebMvc tests for the preview endpoint (200 + validation 400s)

> **Applied Skill:** `.claude/rules/unit-tests.md` / `spring-mvc-controller-test` — `@ApiTest`, `@MockitoBean` (not
> `@MockBean`), AssertJ/MockMvc, `actual`/`expected` naming, request-validation negative cases live HERE (not in
> component tests). Extends the existing `AgreementTemplateCommandControllerTest`.

## 1. Objective

Register the new `PreviewAgreementPdfUseCase` mock and cover: 200 with `Content-Type: application/pdf` when valid, and
400 for blank `title` / blank `content`. Depends on Task 008 (use case), Task 010 (DTO), Task 011 (endpoint).

## 2. File to Modify / Create

* **File Path:** `service/src/test/java/com/github/jenkaby/bikerental/agreement/web/command/AgreementTemplateCommandControllerTest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

* Add immediately AFTER
  `import com.github.jenkaby.bikerental.agreement.application.usecase.DeleteAgreementTemplateUseCase;`:

```java
import com.github.jenkaby.bikerental.agreement.application.usecase.PreviewAgreementPdfUseCase;
```

* Add immediately AFTER
  `import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementTemplateRequest;`:

```java
import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementPdfPreviewRequest;
```

**Code change 1 — new mocked bean.**

* **Location:** Immediately AFTER the existing `@MockitoBean private DeleteAgreementTemplateUseCase deleteAgreementTemplateUseCase;`
  field.
* **Snippet:**

```java
    @MockitoBean
    private PreviewAgreementPdfUseCase previewAgreementPdfUseCase;
```

**Code change 2 — new nested test class.**

* **Location:** Inside the top-level class body, immediately AFTER the closing brace `}` of the existing
  `DeleteAgreement` nested class and BEFORE the top-level class's final closing brace.
* **Snippet:**

```java
    @Nested
    class PreviewPdf {

        @Test
        void shouldReturn200AndPdfWhenRequestIsValid() throws Exception {
            var request = new AgreementPdfPreviewRequest("Rental Agreement", "Вы соглашаетесь с условиями.");
            byte[] expected = "%PDF-1.7 fake".getBytes();
            given(previewAgreementPdfUseCase.execute(any(PreviewAgreementPdfUseCase.PreviewAgreementPdfCommand.class)))
                    .willReturn(expected);

            mockMvc.perform(post("/api/agreements/preview-pdf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PDF)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));

            verify(previewAgreementPdfUseCase).execute(any(PreviewAgreementPdfUseCase.PreviewAgreementPdfCommand.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenTitleIsBlank(String title) throws Exception {
            var request = new AgreementPdfPreviewRequest(title, "valid content");

            mockMvc.perform(post("/api/agreements/preview-pdf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("title")));

            verify(previewAgreementPdfUseCase, never()).execute(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t"})
        @NullAndEmptySource
        void shouldReturn400WhenContentIsBlank(String content) throws Exception {
            var request = new AgreementPdfPreviewRequest("valid title", content);

            mockMvc.perform(post("/api/agreements/preview-pdf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value(containsString("content")));

            verify(previewAgreementPdfUseCase, never()).execute(any());
        }
    }
```

**Code change 3 — add the `content()` matcher static import.**

* **Location:** In the static-import block, immediately AFTER
  `import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;`.
* **Snippet:**

```java
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
```

> `post`, `status`, `jsonPath`, `containsString`, `given`, `any`, `verify`, `never`, `@Nested`, `@Test`,
> `@ParameterizedTest`, `@ValueSource`, `@NullAndEmptySource`, and `MediaType` are already imported in this file — do
> NOT re-import them. Only the three additions above are new.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests AgreementTemplateCommandControllerTest
```

</task_file_template>
