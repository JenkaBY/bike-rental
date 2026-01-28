package com.github.jenkaby.bikerental;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModuleIdentifier;
import org.springframework.modulith.core.ApplicationModules;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for module structure and relationships.
 * Verifies that modules are properly defined and follow the expected architecture.
 */
class ModulithStructureTest {

    private static final ApplicationModules modules = ApplicationModules.of(BikeRentalApplication.class);

    @Test
    void shouldHaveDefinedModules() {
        assertThat(modules.stream())
                .as("Application should have at least one module defined")
                .isNotEmpty();
    }

    @Test
    void customerModuleShouldBeIndependent() {
        ApplicationModule customer = modules.getModuleByName("customer")
                .orElseThrow(() -> new AssertionError("Customer module not found"));

        assertThat(customer.getDirectDependencies(modules).stream().toList())
                .as("Customer module should only depend on shared module")
                .allMatch(dep -> dep.getTargetModule().getIdentifier().equals(ApplicationModuleIdentifier.of("shared")));
    }

    @Test
    void sharedModuleShouldBeOpen() {
        ApplicationModule shared = modules.getModuleByName("shared")
                .orElseThrow(() -> new AssertionError("Shared module not found"));

        assertThat(shared.isOpen())
                .as("Shared module should be open for all modules to use")
                .isTrue();
    }

    @Test
    void modulesShouldHaveDisplayNames() {
        modules.forEach(module ->
                assertThat(module.getDisplayName())
                        .as("Module %s should have a display name", module.getDisplayName())
                        .isNotBlank()
        );
    }

    @Test
    void shouldListAllModules() {
        List<String> moduleNames = modules.stream()
                .map(ApplicationModule::getIdentifier)
                .map(ApplicationModuleIdentifier::toString)
                .toList();

        System.out.println("Discovered modules:");
        moduleNames.forEach(name -> System.out.println("  - " + name));

        assertThat(moduleNames)
                .as("Should contain core modules")
                .contains("customer", "shared");
    }
}
