package com.github.jenkaby.bikerental;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Tests that verify module boundaries using ArchUnit.
 * These tests ensure that modules respect their boundaries and don't have illegal dependencies.
 */
class ModulithBoundariesTest {

    private static final ApplicationModules modules = ApplicationModules.of(BikeRentalApplication.class);
    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.github.jenkaby.bikerental");
    }

    @Test
    void domainLayerShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .as("Domain layer should not depend on infrastructure layer")
                .check(importedClasses);
    }

    @Test
    void domainLayerShouldNotDependOnWeb() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..web..")
                .as("Domain layer should not depend on web layer")
                .check(importedClasses);
    }

    @Test
    void domainLayerShouldNotDependOnSpringFramework() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .as("Domain layer should not depend on Spring Framework")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    void webLayerShouldNotAccessInfrastructureDirectly() {
        noClasses()
                .that().resideInAPackage("..web..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..")
                .as("Web layer should not directly access infrastructure persistence layer")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    void verifyLayeredArchitecturePerModule() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Web").definedBy("..web..")
                .layer("Application").definedBy("..application..")
                .layer("Domain").definedBy("..domain..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                // Shared layer is not included in layered architecture check - it's cross-cutting
                // ModuleApi includes root packages and event packages (public API)
                .layer("ModuleApi").definedBy("com.github.jenkaby.bikerental.*", "com.github.jenkaby.bikerental.*.event")

                .whereLayer("Web").mayNotBeAccessedByAnyLayer()
                .whereLayer("Web").mayOnlyAccessLayers("Application", "Domain", "ModuleApi")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Web", "Infrastructure", "ModuleApi")
                .whereLayer("Application").mayOnlyAccessLayers("Domain", "ModuleApi") // Application can create module API types
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Web", "ModuleApi")
                // Infrastructure layer: No access restrictions (can access Domain, Application, and Shared)
                .whereLayer("ModuleApi").mayOnlyAccessLayers("Application", "Domain", "Infrastructure", "ModuleApi")

                // Ignore standard library and framework dependencies
                .ignoreDependency(resideInAnyPackage("..infrastructure.."), resideInAnyPackage("java..", "javax..", "jakarta..", "org.springframework..", "org.slf4j..", "lombok..", "org.mapstruct..", "com.github.f4b6a3..", "tools.jackson.."))
                .ignoreDependency(resideInAnyPackage("..shared.."), resideInAnyPackage("java..", "javax..", "jakarta..", "org.springframework..", "org.slf4j..", "lombok..", "org.mapstruct.."))
                .ignoreDependency(resideInAnyPackage("..web.."), resideInAnyPackage("java..", "javax..", "jakarta..", "org.springframework..", "org.slf4j..", "lombok..", "org.mapstruct..", "tools.jackson..", "com.fasterxml.jackson..", "io.swagger.."))
                .ignoreDependency(resideInAnyPackage("..application.."), resideInAnyPackage("java..", "javax..", "jakarta..", "org.springframework..", "org.slf4j..", "lombok..", "org.mapstruct.."))
                .ignoreDependency(resideInAnyPackage("com.github.jenkaby.bikerental.*"), resideInAnyPackage("java..", "javax..", "jakarta..", "org.springframework..", "org.slf4j..", "lombok..", "org.mapstruct..", "org.springframework.modulith.."))
                // Allow ModuleApi to access standard library (records extend java.lang.Record)
                .ignoreDependency(resideInAnyPackage("com.github.jenkaby.bikerental.*.event"), resideInAnyPackage("java..", "javax..", "jakarta.."))
                // Allow any layer to access Shared (cross-cutting concern)
                .ignoreDependency(resideInAnyPackage("..infrastructure.."), resideInAnyPackage("..shared.."))
                .ignoreDependency(resideInAnyPackage("..application.."), resideInAnyPackage("..shared.."))
                .ignoreDependency(resideInAnyPackage("..web.."), resideInAnyPackage("..shared.."))
                // Allow ModuleApi (events) to access Shared domain event marker interface (event contract)
                // This covers events in *.event packages (e.g., rental.event) and root module packages (e.g., finance.PaymentReceived)
                .ignoreDependency(resideInAnyPackage("com.github.jenkaby.bikerental.*.event", "com.github.jenkaby.bikerental.finance"), resideInAnyPackage("..shared.domain.event.."))
                // Allow Shared to access Domain (VO mappers need to create/use Domain VOs)
                .ignoreDependency(resideInAnyPackage("..shared.."), resideInAnyPackage("..domain.."))
                // Allow Application to access ModuleApi (Application creates module API types)
                .ignoreDependency(resideInAnyPackage("..application.."), resideInAnyPackage("com.github.jenkaby.bikerental.customer"))
                // Allow Web to access ModuleApi
                .ignoreDependency(resideInAnyPackage("..web.."), resideInAnyPackage("com.github.jenkaby.bikerental.customer"))
                // Allow ModuleApi (root module packages) to extend Shared exceptions (cross-cutting concern)
                .ignoreDependency(resideInAnyPackage("com.github.jenkaby.bikerental.tariff"), resideInAnyPackage("..shared.exception.."))
                // Allow Application to access Infrastructure (for utilities like PatchValueParser)
                .ignoreDependency(resideInAnyPackage("..application.."), resideInAnyPackage("..infrastructure.util.."))
                // Exclude package-info classes from layer checks (they're metadata annotations, not actual classes)
                .ignoreDependency(simpleName("package-info"), resideInAnyPackage(".."))

                .as("Each module should follow hexagonal architecture layers")
                .check(importedClasses);
    }

    @Test
    void verifyModuleDependencies() {
        modules.verify();
    }
}
