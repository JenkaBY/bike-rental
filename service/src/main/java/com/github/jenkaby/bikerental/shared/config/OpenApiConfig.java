package com.github.jenkaby.bikerental.shared.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
public class OpenApiConfig {

    public static final class Tags {
        public static final String AGREEMENTS = "Agreements";
        public static final String CUSTOMERS = "Customers";
        public static final String EQUIPMENT = "Equipments Catalogue";
        public static final String EQUIPMENT_TYPES = "Equipment Types";
        public static final String EQUIPMENT_STATUSES = "Equipment Statuses";
        public static final String FINANCE = "Finance";
        public static final String RENTALS = "Rentals";
        public static final String TARIFFS = "Tariffs";
        public static final String USERS = "Users";

        private Tags() {
        }
    }

    private final Optional<BuildProperties> buildProperties;

    public OpenApiConfig(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI bikeRentalOpenAPI() {
        var gitCommit = buildProperties
                .map(bp -> bp.get("git.commit"))
                .orElse("unknown");
        var version = "commit: " + gitCommit;

        var problemDetailSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ProblemDetail.class)).schema;

        return new OpenAPI()
                .components(new Components().addSchemas(ProblemDetail.class.getSimpleName(), problemDetailSchema))
                .info(new Info()
                        .title("Bike Rental API")
                        .version(version)
                        .description("REST API for the Bike Rental management system")
                        .contact(new Contact().name("Bike Rental Team")))
                .tags(List.of(
                        new Tag().name(Tags.AGREEMENTS).description("Rental agreement template management"),
                        new Tag().name(Tags.CUSTOMERS).description("Customer search and profile management"),
                        new Tag().name(Tags.EQUIPMENT).description("Equipment catalog management"),
                        new Tag().name(Tags.EQUIPMENT_TYPES).description("Equipment type catalog"),
                        new Tag().name(Tags.EQUIPMENT_STATUSES).description("Equipment status catalog and allowed transitions"),
                        new Tag().name(Tags.FINANCE).description("Payment recording and history"),
                        new Tag().name(Tags.RENTALS).description("Rental lifecycle management"),
                        new Tag().name(Tags.TARIFFS).description("Tariff catalog and selection"),
                        new Tag().name(Tags.USERS).description("Users and self management")
                ));
    }

    @Bean
    public GroupedOpenApi allGroup() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("All")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi agreementsGroup() {
        return GroupedOpenApi.builder()
                .group("agreements")
                .displayName(Tags.AGREEMENTS)
                .pathsToMatch("/api/agreements/**", "/api/rentals/*/signatures/**", "/api/rentals/*/agreement")
                .build();
    }

    @Bean
    public GroupedOpenApi customersGroup() {
        return GroupedOpenApi.builder()
                .group("customers")
                .displayName(Tags.CUSTOMERS)
                .pathsToMatch("/api/customers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi equipmentGroup() {
        return GroupedOpenApi.builder()
                .group("equipment")
                .displayName(Tags.EQUIPMENT)
                .pathsToMatch("/api/equipments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi equipmentTypesGroup() {
        return GroupedOpenApi.builder()
                .group("equipment-types")
                .displayName(Tags.EQUIPMENT_TYPES)
                .pathsToMatch("/api/equipment-types/**")
                .build();
    }

    @Bean
    public GroupedOpenApi equipmentStatusesGroup() {
        return GroupedOpenApi.builder()
                .group("equipment-statuses")
                .displayName(Tags.EQUIPMENT_STATUSES)
                .pathsToMatch("/api/equipment-statuses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi financeGroup() {
        return GroupedOpenApi.builder()
                .group("finance")
                .displayName(Tags.FINANCE)
                .pathsToMatch("/api/finance/**")
                .build();
    }

    @Bean
    public GroupedOpenApi rentalsGroup() {
        return GroupedOpenApi.builder()
                .group("rentals")
                .displayName(Tags.RENTALS)
                .pathsToMatch("/api/rentals/**")
                .build();
    }

    @Bean
    public GroupedOpenApi tariffsGroup() {
        return GroupedOpenApi.builder()
                .group("tariffs")
                .displayName(Tags.TARIFFS)
                .pathsToMatch("/api/tariffs/**")
                .build();
    }

    @Bean
    public GroupedOpenApi identityGroup() {
        var unauthorizedResponse = new ApiResponse()
                .description("Unauthorized — missing or invalid JWT")
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                        new MediaType().schema(new Schema<ProblemDetail>().$ref("#/components/schemas/ProblemDetail"))
                ));
        var forbiddenResponse = new ApiResponse()
                .description("Forbidden — insufficient roles")
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                        new MediaType().schema(new Schema<ProblemDetail>().$ref("#/components/schemas/ProblemDetail"))
                ));

        return GroupedOpenApi.builder()
                .group("identity")
                .displayName(Tags.USERS)
                .pathsToMatch("/api/auth/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    var responses = operation.getResponses();
                    if (responses == null) {
                        responses = new ApiResponses();
                        operation.setResponses(responses);
                    }
                    responses.putIfAbsent("401", unauthorizedResponse);
                    responses.putIfAbsent("403", forbiddenResponse);
                    return operation;
                })
                .build();
    }

    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        var internalErrorResponse = new ApiResponse()
                .description("Internal server error")
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                        new MediaType().schema(new Schema<ProblemDetail>().$ref("#/components/schemas/ProblemDetail"))
                ));

        return openApi -> {
            if (openApi.getPaths() == null) return;
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        ApiResponses responses = operation.getResponses();
                        if (responses == null) {
                            responses = new ApiResponses();
                            operation.setResponses(responses);
                        }
                        responses.putIfAbsent("500", internalErrorResponse);
                    })
            );
        };
    }
}
