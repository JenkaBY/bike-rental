package com.github.jenkaby.bikerental;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for module dependency rules.
 * Verifies that the actual module dependencies match the expected architecture.
 */
@DisplayName("Modulith Dependency Rules")
class ModulithDependencyRulesTest {

    private static final ApplicationModules modules = ApplicationModules.of(BikeRentalApplication.class);

    @Test
    @DisplayName("Customer module should be a core independent module")
    void customerModuleShouldBeIndependent() {
        ApplicationModule customer = getModule("customer");

        Set<String> dependencies = customer.getDirectDependencies(modules).stream()
                .map(dep -> dep.getTargetModule().getIdentifier().toString())
                .collect(Collectors.toSet());

        assertThat(dependencies)
                .as("Customer module should only depend on shared module")
                .containsOnly("shared");
    }

    @Test
    @DisplayName("Shared module should have no dependencies on business modules")
    void sharedModuleShouldNotDependOnBusinessModules() {
        ApplicationModule shared = getModule("shared");

        Set<String> dependencies = shared.getDirectDependencies(modules).stream()
                .map(dep -> dep.getTargetModule().getIdentifier().toString())
                .collect(Collectors.toSet());

        assertThat(dependencies)
                .as("Shared module should have no dependencies on business modules")
                .isEmpty();
    }

    @Test
    @DisplayName("All modules should be properly named")
    void allModulesShouldHaveProperNames() {
        modules.forEach(module -> {
            assertThat(module.getIdentifier().toString())
                    .as("Module name should not be empty")
                    .isNotBlank();

            assertThat(module.getDisplayName())
                    .as("Module %s should have a display name", module.getIdentifier())
                    .isNotBlank();

            System.out.printf("Module: %s - %s%n", module.getIdentifier(), module.getDisplayName());
        });
    }

    @Test
    @DisplayName("Customer module should define base package")
    void customerModuleShouldDefineBasePackage() {
        ApplicationModule customer = getModule("customer");

        assertThat(customer.getBasePackage().getName())
                .as("Customer module should have correct base package")
                .isEqualTo("com.github.jenkaby.bikerental.customer");
    }

    @Test
    @DisplayName("Modules should not have circular dependencies")
    void modulesShouldNotHaveCircularDependencies() {
        modules.forEach(module -> {
            var directDependencies = module.getDirectDependencies(modules);

            directDependencies.stream().forEach(dependency -> {
                Set<String> transitiveDeps = dependency.getTargetModule().getDirectDependencies(modules).stream()
                        .map(dep -> dep.getTargetModule().getIdentifier().toString())
                        .collect(Collectors.toSet());

                assertThat(transitiveDeps)
                        .as("Module %s should not create circular dependency back to %s",
                                dependency.getTargetModule().getIdentifier(), module.getIdentifier())
                        .doesNotContain(module.getIdentifier().toString());
            });
        });
    }

    @Test
    @DisplayName("Customer module should expose only public API packages")
    void customerModuleShouldExposeOnlyPublicApi() {
        ApplicationModule customer = getModule("customer");

        // Count unique target modules (not individual dependency instances)
        long uniqueModuleDependencies = customer.getDirectDependencies(modules).stream()
                .map(dep -> dep.getTargetModule().getIdentifier())
                .distinct()
                .count();

        assertThat(uniqueModuleDependencies)
                .as("Customer module should depend on only one other module (shared)")
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("Print module dependency graph")
    void printModuleDependencyGraph() {
        System.out.println("\n=== Module Dependency Graph ===\n");

        modules.forEach(module -> {
            System.out.printf("Module: %s (%s)%n", module.getIdentifier(), module.getDisplayName());
            System.out.printf("  Base Package: %s%n", module.getBasePackage().getName());
            System.out.printf("  Open: %s%n", module.isOpen());

            var dependencies = module.getDirectDependencies(modules);
            if (!dependencies.isEmpty()) {
                System.out.println("  Dependencies:");
                dependencies.stream().distinct().forEach(dep ->
                        System.out.printf("    -> %s%n", dep.getTargetModule().getIdentifier())
                );
            } else {
                System.out.println("  Dependencies: None");
            }
            System.out.println();
        });
    }

    private ApplicationModule getModule(String name) {
        return modules.getModuleByName(name)
                .orElseThrow(() -> new AssertionError("Module '" + name + "' not found"));
    }
}
