package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateSummariesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateVariablesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetActiveAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateSummaryResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateVariableResponse;
import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/api/agreements", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
@Tag(name = OpenApiConfig.Tags.AGREEMENTS)
class AgreementTemplateQueryController {

    private final FindAgreementTemplateSummariesUseCase findAgreementTemplateSummariesUseCase;
    private final GetActiveAgreementTemplateUseCase getActiveAgreementTemplateUseCase;
    private final GetAgreementTemplateUseCase getAgreementTemplateUseCase;
    private final FindAgreementTemplateVariablesUseCase findAgreementTemplateVariablesUseCase;
    private final AgreementTemplateWebMapper mapper;

    AgreementTemplateQueryController(FindAgreementTemplateSummariesUseCase findAgreementTemplateSummariesUseCase,
                                     GetActiveAgreementTemplateUseCase getActiveAgreementTemplateUseCase,
                                     GetAgreementTemplateUseCase getAgreementTemplateUseCase,
                                     FindAgreementTemplateVariablesUseCase findAgreementTemplateVariablesUseCase,
                                     AgreementTemplateWebMapper mapper) {
        this.findAgreementTemplateSummariesUseCase = findAgreementTemplateSummariesUseCase;
        this.getActiveAgreementTemplateUseCase = getActiveAgreementTemplateUseCase;
        this.getAgreementTemplateUseCase = getAgreementTemplateUseCase;
        this.findAgreementTemplateVariablesUseCase = findAgreementTemplateVariablesUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "List agreement templates",
            description = "Returns summaries of all agreement templates without content")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template summaries",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AgreementTemplateSummaryResponse.class))))
    })
    public ResponseEntity<List<AgreementTemplateSummaryResponse>> findAll() {
        log.info("[GET] Listing agreement template summaries");
        var summaries = findAgreementTemplateSummariesUseCase.execute();
        return ResponseEntity.ok(mapper.toSummaryResponses(summaries));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active agreement template",
            description = "Returns the single currently ACTIVE template used for signing")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active template",
                    content = @Content(schema = @Schema(implementation = AgreementTemplateResponse.class))),
            @ApiResponse(responseCode = "404", description = "No active template exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AgreementTemplateResponse> getActive() {
        log.info("[GET] Fetching active agreement template");
        var active = getActiveAgreementTemplateUseCase.execute();
        return ResponseEntity.ok(mapper.toResponse(active));
    }

    @GetMapping("/variables")
    @Operation(summary = "List template placeholder variables",
            description = "Returns the catalog of {{key}} placeholders that can be used in agreement template "
                    + "content and are substituted with customer/rental data when the PDF is rendered")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Placeholder variable catalog",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AgreementTemplateVariableResponse.class))))
    })
    public ResponseEntity<List<AgreementTemplateVariableResponse>> findVariables() {
        log.info("[GET] Listing agreement template placeholder variables");
        var variables = findAgreementTemplateVariablesUseCase.execute();
        return ResponseEntity.ok(mapper.toVariableResponses(variables));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agreement template by id",
            description = "Returns the full template including content")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template",
                    content = @Content(schema = @Schema(implementation = AgreementTemplateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AgreementTemplateResponse> getById(@PathVariable("id") @Positive Long id) {
        log.info("[GET] Fetching agreement template {}", id);
        var template = getAgreementTemplateUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(template));
    }
}
