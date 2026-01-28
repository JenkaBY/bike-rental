package com.github.jenkaby.bikerental;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
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
                .layer("ModuleApi").definedBy("com.github.jenkaby.bikerental.*")

                .whereLayer("Web").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Web", "Infrastructure", "ModuleApi")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Web", "ModuleApi")
                .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application", "Infrastructure")
                .whereLayer("ModuleApi").mayOnlyAccessLayers("Application", "Domain", "Infrastructure", "ModuleApi")
                .ignoreDependency(resideInAnyPackage("..infrastructure.."), resideInAnyPackage("java..", "javax..", "jakarta..", "org.springframework..", "org.slf4j..", "lombok..", "org.mapstruct.."))
                .ignoreDependency(resideInAnyPackage("com.github.jenkaby.bikerental.*"), resideInAnyPackage("java..", "javax..", "jakarta..", "org.springframework..", "org.slf4j..", "lombok..", "org.mapstruct..", "org.springframework.modulith.."))

                .as("Each module should follow hexagonal architecture layers")
                .check(importedClasses);
    }

    @Test
    void verifyModuleDependencies() {
        modules.verify();
    }
}
