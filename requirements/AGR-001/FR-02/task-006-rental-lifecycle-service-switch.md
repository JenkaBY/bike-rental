<task_file_template>

# Task 006: Wire signing use cases into RentalLifecycleService

> **Applied Skill:** `spring-boot-best-practices` — constructor injection; `java-best-practices` — switch
> expression arms, zero inline comments. The lifecycle service is the single dispatch point for the endpoint.

## 1. Objective

Extend `RentalLifecycleService` to dispatch `AWAITING_SIGNATURE → PrepareSigningUseCase` and
`DRAFT → CancelSigningUseCase`, injecting the two new use cases created in task 003/004/005.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RentalLifecycleService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** Add these two imports below the existing `import ...CancelRentalUseCase;` line:

```java
import com.github.jenkaby.bikerental.rental.application.usecase.CancelSigningUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.PrepareSigningUseCase;
```

**Code to Add/Replace:**

### Change 3.1 — Replace the whole class body below the `@Service` annotation

* **Location:** Replace the entire class declaration (from `class RentalLifecycleService implements ...` down
  to and including its final closing brace `}`) with the snippet below. The imports at the top of the file and
  the package statement stay as-is (plus the two new imports above).

* **Snippet:**

```java
class RentalLifecycleService implements RentalLifecycleUseCase {

    private final ActivateRentalUseCase activateRentalUseCase;
    private final CancelRentalUseCase cancelRentalUseCase;
    private final PrepareSigningUseCase prepareSigningUseCase;
    private final CancelSigningUseCase cancelSigningUseCase;

    RentalLifecycleService(ActivateRentalUseCase activateRentalUseCase,
                           CancelRentalUseCase cancelRentalUseCase,
                           PrepareSigningUseCase prepareSigningUseCase,
                           CancelSigningUseCase cancelSigningUseCase) {
        this.activateRentalUseCase = activateRentalUseCase;
        this.cancelRentalUseCase = cancelRentalUseCase;
        this.prepareSigningUseCase = prepareSigningUseCase;
        this.cancelSigningUseCase = cancelSigningUseCase;
    }

    @Override
    public Rental execute(RentalLifecycleCommand command) {
        return switch (command.targetStatus()) {
            case ACTIVE -> activateRentalUseCase.execute(
                    new ActivateRentalUseCase.ActivateCommand(command.rentalId(), command.operatorId()));
            case CANCELLED -> cancelRentalUseCase.execute(
                    new CancelRentalUseCase.CancelCommand(command.rentalId(), command.operatorId()));
            case AWAITING_SIGNATURE -> prepareSigningUseCase.execute(
                    new PrepareSigningUseCase.PrepareSigningCommand(command.rentalId(), command.operatorId()));
            case DRAFT -> cancelSigningUseCase.execute(
                    new CancelSigningUseCase.CancelSigningCommand(command.rentalId(), command.operatorId()));
            default -> throw new IllegalArgumentException(
                    "Unsupported lifecycle target status: " + command.targetStatus());
        };
    }
}
```

> The `@Service` annotation line directly above the class declaration is untouched. Do NOT remove the
> `default` arm — it still guards the remaining statuses (`COMPLETED`, `DEBT`).

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
