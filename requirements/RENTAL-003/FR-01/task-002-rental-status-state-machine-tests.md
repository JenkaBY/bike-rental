# Task 002: Unit Tests for RentalStatus State Machine

> **Applied Skill:** `java.instructions.md` — immutable data, pattern matching;
> `springboot.instructions.md` — focused, stateless service tests

## 1. Objective

Create unit tests covering every valid transition (no exception) and every explicitly rejected
transition (throws `InvalidRentalStatusException` containing both statuses).

## 2. File to Modify / Create

* **File Path:** `service/src/test/java/com/github/jenkaby/bikerental/rental/domain/model/RentalStatusTest.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

**Code to Add/Replace:**

* **Location:** New file — place at the given path.

```java
package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RentalStatusTest {

    @Nested
    class ValidateTransitionTo {

        @Nested
        class ValidTransitions {

            static Stream<Arguments> validTransitions() {
                return Stream.of(
                        Arguments.of(RentalStatus.DRAFT, RentalStatus.ACTIVE),
                        Arguments.of(RentalStatus.DRAFT, RentalStatus.CANCELLED),
                        Arguments.of(RentalStatus.ACTIVE, RentalStatus.CANCELLED)
                );
            }

            @ParameterizedTest(name = "{0} -> {1}")
            @MethodSource("validTransitions")
            void doesNotThrow(RentalStatus from, RentalStatus to) {
                assertThatCode(() -> from.validateTransitionTo(to))
                        .as("Transition %s -> %s should be allowed", from, to)
                        .doesNotThrowAnyException();
            }
        }

        @Nested
        class InvalidTransitions {

            static Stream<Arguments> invalidTransitions() {
                return Stream.of(
                        Arguments.of(RentalStatus.ACTIVE, RentalStatus.DRAFT),
                        Arguments.of(RentalStatus.ACTIVE, RentalStatus.ACTIVE),
                        Arguments.of(RentalStatus.CANCELLED, RentalStatus.ACTIVE),
                        Arguments.of(RentalStatus.CANCELLED, RentalStatus.DRAFT),
                        Arguments.of(RentalStatus.COMPLETED, RentalStatus.ACTIVE),
                        Arguments.of(RentalStatus.COMPLETED, RentalStatus.CANCELLED),
                        Arguments.of(RentalStatus.DEBT, RentalStatus.CANCELLED),
                        Arguments.of(RentalStatus.DEBT, RentalStatus.ACTIVE),
                        Arguments.of(RentalStatus.DRAFT, RentalStatus.DRAFT)
                );
            }

            @ParameterizedTest(name = "{0} -> {1}")
            @MethodSource("invalidTransitions")
            void throwsInvalidRentalStatusException(RentalStatus from, RentalStatus to) {
                assertThatThrownBy(() -> from.validateTransitionTo(to))
                        .as("Transition %s -> %s should be rejected", from, to)
                        .isInstanceOf(InvalidRentalStatusException.class)
                        .hasMessageContaining(from.name())
                        .hasMessageContaining(to.name());
            }
        }
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalStatusTest
```
