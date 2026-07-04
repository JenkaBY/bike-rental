<task_file_template>

# Task 011: Add the `POST /api/agreements/preview-pdf` endpoint to the command controller

> **Applied Skill:** `spring-mvc-controller-test` / `spring-boot-best-practices` — constructor injection, `@Valid`
> request body, explicit `produces = MediaType.APPLICATION_PDF_VALUE`. `.claude/rules/java-style.md` — zero inline
> comments. Extends the existing `AgreementTemplateCommandController`.

## 1. Objective

Expose the stateless preview endpoint: validate the request, delegate to `PreviewAgreementPdfUseCase`, and return the
raw PDF bytes with `Content-Type: application/pdf` and status 200. Depends on Task 008 (use case), Task 010 (DTO).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/command/AgreementTemplateCommandController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add these two imports in the import block:

* Immediately AFTER `import com.github.jenkaby.bikerental.agreement.application.usecase.DeleteAgreementTemplateUseCase;`:

```java
import com.github.jenkaby.bikerental.agreement.application.usecase.PreviewAgreementPdfUseCase;
```

* Immediately AFTER `import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementTemplateRequest;`:

```java
import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementPdfPreviewRequest;
```

**Code change 1 — new field:**

* **Location:** In the field declarations, immediately AFTER the line
  `private final DeleteAgreementTemplateUseCase deleteAgreementTemplateUseCase;`.
* **Snippet:**

```java
    private final PreviewAgreementPdfUseCase previewAgreementPdfUseCase;
```

**Code change 2 — replace the constructor.**

* **Location:** Replace the ENTIRE existing constructor.
* **Find this exact block:**

```java
    AgreementTemplateCommandController(CreateAgreementTemplateUseCase createAgreementTemplateUseCase,
                                       UpdateAgreementTemplateUseCase updateAgreementTemplateUseCase,
                                       ActivateAgreementTemplateUseCase activateAgreementTemplateUseCase,
                                       DeleteAgreementTemplateUseCase deleteAgreementTemplateUseCase,
                                       AgreementTemplateWebMapper mapper) {
        this.createAgreementTemplateUseCase = createAgreementTemplateUseCase;
        this.updateAgreementTemplateUseCase = updateAgreementTemplateUseCase;
        this.activateAgreementTemplateUseCase = activateAgreementTemplateUseCase;
        this.deleteAgreementTemplateUseCase = deleteAgreementTemplateUseCase;
        this.mapper = mapper;
    }
```

* **Replace with:**

```java
    AgreementTemplateCommandController(CreateAgreementTemplateUseCase createAgreementTemplateUseCase,
                                       UpdateAgreementTemplateUseCase updateAgreementTemplateUseCase,
                                       ActivateAgreementTemplateUseCase activateAgreementTemplateUseCase,
                                       DeleteAgreementTemplateUseCase deleteAgreementTemplateUseCase,
                                       PreviewAgreementPdfUseCase previewAgreementPdfUseCase,
                                       AgreementTemplateWebMapper mapper) {
        this.createAgreementTemplateUseCase = createAgreementTemplateUseCase;
        this.updateAgreementTemplateUseCase = updateAgreementTemplateUseCase;
        this.activateAgreementTemplateUseCase = activateAgreementTemplateUseCase;
        this.deleteAgreementTemplateUseCase = deleteAgreementTemplateUseCase;
        this.previewAgreementPdfUseCase = previewAgreementPdfUseCase;
        this.mapper = mapper;
    }
```

**Code change 3 — new endpoint method:**

* **Location:** Inside the class body, immediately AFTER the closing brace `}` of the existing `delete(...)` method and
  BEFORE the class's final closing brace.
* **Snippet:**

```java
    @PostMapping(value = "/preview-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> previewPdf(@Valid @RequestBody AgreementPdfPreviewRequest request) {
        log.info("[POST] Rendering agreement preview PDF");
        var command = new PreviewAgreementPdfUseCase.PreviewAgreementPdfCommand(request.title(), request.content());
        byte[] pdf = previewAgreementPdfUseCase.execute(command);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
```

> `MediaType`, `ResponseEntity`, `PostMapping`, `RequestBody`, `Valid`, and `log` are already imported/available in
> this file — do NOT re-import. Only the two new imports at the top of this task are added. Keep the class-level
> `@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})` untouched; the method-level `produces` on this
> endpoint overrides it for the PDF response.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
