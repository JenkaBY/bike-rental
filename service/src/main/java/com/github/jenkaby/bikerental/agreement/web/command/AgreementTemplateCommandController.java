package com.github.jenkaby.bikerental.agreement.web.command;

import com.github.jenkaby.bikerental.agreement.application.usecase.*;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementPdfPreviewRequest;
import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementTemplateRequest;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(path = "/api/agreements", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
@Tag(name = OpenApiConfig.Tags.AGREEMENTS)
class AgreementTemplateCommandController {

    private final CreateAgreementTemplateUseCase createAgreementTemplateUseCase;
    private final UpdateAgreementTemplateUseCase updateAgreementTemplateUseCase;
    private final ActivateAgreementTemplateUseCase activateAgreementTemplateUseCase;
    private final DeleteAgreementTemplateUseCase deleteAgreementTemplateUseCase;
    private final PreviewAgreementPdfUseCase previewAgreementPdfUseCase;
    private final AgreementTemplateWebMapper mapper;

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

    @PostMapping
    @Operation(summary = "Create agreement template draft",
            description = "Creates a new agreement template in DRAFT status")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template draft created",
                    content = @Content(schema = @Schema(implementation = AgreementTemplateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AgreementTemplateResponse> create(@Valid @RequestBody AgreementTemplateRequest request) {
        log.info("[POST] Creating agreement template draft");
        var command = new CreateAgreementTemplateUseCase.CreateAgreementTemplateCommand(request.title(), request.content());
        AgreementTemplate created = createAgreementTemplateUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update agreement template draft",
            description = "Updates title and content of a DRAFT template; ACTIVE and DEACTIVATED templates are immutable")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template updated",
                    content = @Content(schema = @Schema(implementation = AgreementTemplateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Template not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Template is not editable (not in DRAFT status)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AgreementTemplateResponse> update(@PathVariable("id") @Positive Long id,
                                                            @Valid @RequestBody AgreementTemplateRequest request) {
        log.info("[PATCH] Updating agreement template {}", id);
        var command = new UpdateAgreementTemplateUseCase.UpdateAgreementTemplateCommand(id, request.title(), request.content());
        AgreementTemplate updated = updateAgreementTemplateUseCase.execute(command);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate agreement template",
            description = "Activates a DRAFT template, assigns the next version number and deactivates the previously active one")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template activated",
                    content = @Content(schema = @Schema(implementation = AgreementTemplateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Template is not activatable or concurrent activation detected",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AgreementTemplateResponse> activate(@PathVariable("id") @Positive Long id) {
        log.info("[PATCH] Activating agreement template {}", id);
        AgreementTemplate activated = activateAgreementTemplateUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(activated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete agreement template draft",
            description = "Deletes a DRAFT template; ACTIVE and DEACTIVATED templates cannot be deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Template deleted"),
            @ApiResponse(responseCode = "404", description = "Template not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Template is not deletable (not in DRAFT status)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable("id") @Positive Long id) {
        log.info("[DELETE] Deleting agreement template {}", id);
        deleteAgreementTemplateUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

//     FIXME !!! Changes in the initial design. Don't pdf in the url, instead pass header accept: application/pdf
    @PostMapping(value = "/preview-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Preview agreement template as PDF",
            description = "Renders the given title and content into the agreement PDF layout using fixture rental data and a signature placeholder")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rendered PDF document",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "PDF rendering failed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    //    TODO Can this endpoint be lazy and returns StreamingResponseBody ? Investigate pros and cons of this approach
    public ResponseEntity<byte[]> previewPdf(@Valid @RequestBody AgreementPdfPreviewRequest request) {
        log.info("[POST] Rendering agreement preview PDF");
        var command = new PreviewAgreementPdfUseCase.PreviewAgreementPdfCommand(request.title(), request.content());
        byte[] pdf = previewAgreementPdfUseCase.execute(command);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
