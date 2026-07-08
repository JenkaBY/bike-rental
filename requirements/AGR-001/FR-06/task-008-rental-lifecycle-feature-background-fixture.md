<task_file_template>

# Task 008: Add an ACTIVE agreement template fixture to rental-lifecycle.feature Background

> **Applied Skill:** `spring-boot-java-cucumber` / `component-tests` — reuses the existing
> `agreement templates exist in the database with the following data` step (see
> `AgreementTemplateSteps.agreementTemplatesExistInTheDatabase`, `component-test/src/test/java/.../steps/agreement/AgreementTemplateSteps.java`
> line 26) and copies the exact row shape already used in `agreement-signing.feature` Background (all NOT NULL
> columns populated, per `.claude/rules/component-tests.md`). No new table — `AgreementTemplateJpaEntity` is already
> in `DbSteps.TABLE_TO_TRUNCATE`. Depends on Tasks 001–007 (production code must compile first). Independent of Task
> 009 (new step class) but both touch files needed by Tasks 010/011, so do this one first.

## 1. Objective

Add one `ACTIVE` agreement template fixture row (`id=5`) to the `Background` section of `rental-lifecycle.feature`,
needed by the new composite "activate via signing" step that scenarios A and D will use in later tasks.

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/resources/features/rental/rental-lifecycle.feature`
* **Action:** Modify Existing File

## 3. Code Implementation

**Code to Add/Replace:**

* **Location:** At the end of the `Background:` section, immediately after the last existing step (the
  `And the following tariff v2 records exist in db` table, which currently ends the Background right before the
  blank line preceding `Scenario Outline: Activate a DRAFT rental...`).
* **Snippet:**

```gherkin
    And agreement templates exist in the database with the following data
      | id | versionNumber | title               | content                                    | contentSha256 | status | createdAt           | updatedAt           | activatedAt         |
      | 5  | 3             | Rental Agreement v3 | You agree to return the equipment on time. | SHA_ZERO      | ACTIVE | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 | 2026-01-01T09:00:00 |
```

Insert this immediately after the existing:

```gherkin
    And the following tariff v2 records exist in db
      | id | name                   | description               | equipmentType | pricingType       | status | validFrom  | validTo |
      | 10 | Degressive Hourly Bike | Bicycle degressive hourly | BICYCLE       | DEGRESSIVE_HOURLY | ACTIVE | 2026-01-01 |         |
      | 11 | Flat Fee Helmet        | Helmet flat fee           | HELMET        | FLAT_FEE          | ACTIVE | 2026-01-01 |         |
      | 12 | Flat Hourly Scooter    | Scooter flat hourly rate  | SCOOTER       | FLAT_HOURLY       | ACTIVE | 2026-01-01 |         |
```

and before the blank line that separates `Background:` from `Scenario Outline: Activate a DRAFT rental — status
becomes ACTIVE, hold placed, event published`.

This fixture row is used by scenarios A (the `Activate a DRAFT rental...` Scenario Outline) and D (both `Cancel an
ACTIVE rental with hold...` scenarios) in later tasks; it is harmless for scenario C (`Cancel a <rentalStatus>
rental without hold`) since that scenario never triggers signing.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server. Assume the DB is already up. This feature
file will not fully pass yet — scenarios still reference the old `ACTIVE` lifecycle step at this point in the
checklist; that is expected and fixed in later tasks. This task only confirms the file remains valid Gherkin.

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```

</task_file_template>
