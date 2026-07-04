<task_file_template>

# Task 008: Create the AgreementSignedEvent module-API record

> **Applied Skill:** `spring-boot-modulith` / `java-best-practices` — public events implement
> `shared.domain.event.BikeRentalEvent`; it lives in the agreement module ROOT package (module API), NOT in `shared`
> and NOT in an internal sub-package (mirrors `rental.RentalSigningFacade` living in the rental root). Record, zero
> inline comments.

## 1. Objective

Create the public `AgreementSignedEvent` published after a successful signing. It is a future extension point (no
consumer in this FR).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/AgreementSignedEvent.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;

public record AgreementSignedEvent(Long rentalId, Long signatureId) implements BikeRentalEvent {
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
