<task_file_template>

# Task 010: Create the agreement use case interfaces + commands

> **Applied Skill:** `spring-boot-best-practices` — application contracts are interfaces in
> `application/usecase`; `@Service` implementations live in `application/service`. `java-best-practices`
> — records for commands, zero inline comments. Mirrors
> `customer/application/usecase/CreateCustomerUseCase.java` (nested command record inside the interface).

## 1. Objective

Declare the seven use case interfaces (four commands, three queries) the controllers depend on.
Depends on Task 007 (`AgreementTemplate`) and Task 006 (`AgreementTemplateSummary`).

## 2. Files to Modify / Create

Create SEVEN new files under
`service/src/main/java/com/github/jenkaby/bikerental/agreement/application/usecase/`.

## 3. Code Implementation

### File 1: `CreateAgreementTemplateUseCase.java`

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;

public interface CreateAgreementTemplateUseCase {

    AgreementTemplate execute(CreateAgreementTemplateCommand command);

    record CreateAgreementTemplateCommand(String title, String content) {
    }
}
```

### File 2: `UpdateAgreementTemplateUseCase.java`

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;

public interface UpdateAgreementTemplateUseCase {

    AgreementTemplate execute(UpdateAgreementTemplateCommand command);

    record UpdateAgreementTemplateCommand(Long id, String title, String content) {
    }
}
```

### File 3: `ActivateAgreementTemplateUseCase.java`

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;

public interface ActivateAgreementTemplateUseCase {

    AgreementTemplate execute(Long id);
}
```

### File 4: `DeleteAgreementTemplateUseCase.java`

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

public interface DeleteAgreementTemplateUseCase {

    void execute(Long id);
}
```

### File 5: `GetAgreementTemplateUseCase.java`

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;

public interface GetAgreementTemplateUseCase {

    AgreementTemplate execute(Long id);
}
```

### File 6: `FindAgreementTemplateSummariesUseCase.java`

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;

import java.util.List;

public interface FindAgreementTemplateSummariesUseCase {

    List<AgreementTemplateSummary> execute();
}
```

### File 7: `GetActiveAgreementTemplateUseCase.java`

```java
package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;

public interface GetActiveAgreementTemplateUseCase {

    AgreementTemplate execute();
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
