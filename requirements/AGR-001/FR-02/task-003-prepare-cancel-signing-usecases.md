<task_file_template>

# Task 003: Create PrepareSigningUseCase and CancelSigningUseCase interfaces

> **Applied Skill:** `spring-boot-best-practices` — use-case interfaces are contracts in `application/usecase`;
> `java-best-practices` — nested command records, no inline comments. Mirrors `ActivateRentalUseCase` /
> `CancelRentalUseCase`.

## 1. Objective

Add two new use-case interfaces the lifecycle service will delegate to, each with a nested command record
carrying `rentalId` and `operatorId`, returning the mutated `Rental`.

## 2. File to Modify / Create

* **File Path 1:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/PrepareSigningUseCase.java`
* **File Path 2:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/CancelSigningUseCase.java`
* **Action:** Create New File (both)

## 3. Code Implementation

### File 1 — `PrepareSigningUseCase.java`

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

public interface PrepareSigningUseCase {

    Rental execute(PrepareSigningCommand command);

    record PrepareSigningCommand(Long rentalId, String operatorId) {
    }
}
```

### File 2 — `CancelSigningUseCase.java`

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

public interface CancelSigningUseCase {

    Rental execute(CancelSigningCommand command);

    record CancelSigningCommand(Long rentalId, String operatorId) {
    }
}
```

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
