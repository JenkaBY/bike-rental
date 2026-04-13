# Task 013: Add New Component Test Scenarios — Discount and SPECIAL Pricing

> **Applied Skills:**  
> `.github/skills/spring-boot-java-cucumber/SKILL.md` — Cucumber Scenario / Scenario Outline, Gherkin, BDD

## 1. Objective

Add three new Gherkin scenarios to `rental.feature` that cover FR acceptance criteria Scenarios 2, 3, and
the rejected-invalid-special-tariff case (FR rule: "If specialTariffId is provided but the referenced
tariff does not exist or is not of pricingType = SPECIAL, 422 is returned").

The Background in Task 012 adds the necessary V2 tariff fixtures (IDs 10–13); these scenarios rely on them.

## 2. File to Modify

* **File Path:** `component-test/src/test/resources/features/rental/rental.feature`
* **Action:** Modify Existing File — append the three scenarios at the end of the file, after the last
  existing scenario.

---

## 3. Scenarios to Add

Append the following block **at the very end of `rental.feature`**:

```gherkin
  # --- FR-TR-01 V2 Pricing Scenarios ---

  Scenario: Create rental with discount — hold reflects discounted total
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | discountPercent |
      | CUS1       | 1            | PT2H     | OP1        | 10              |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId | status | plannedDuration | estimatedCost |
      | CUS1       | DRAFT  | 120             | 14.40         |
    And the rental response only contains rental equipments
      | equipmentId | equipmentUid | status   | estimatedCost |
      | 1           | BIKE-001     | ASSIGNED | 16.00         |
    And rental was persisted in database
      | discountPercent |
      | 10              |

  Scenario: Create rental with SPECIAL tariff — specialPrice used as total
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | specialTariffId | specialPrice |
      | CUS1       | 1            | PT2H     | OP1        | 13              | 15.00        |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 201
    And the rental response only contains
      | customerId | status | plannedDuration | estimatedCost |
      | CUS1       | DRAFT  | 120             | 15.00         |
    And rental was persisted in database
      | specialTariffId | specialPrice |
      | 13              | 15.00        |

  Scenario: Rejected — specialTariffId references a non-SPECIAL tariff type
    Given a rental request with the following data
      | customerId | equipmentIds | duration | operatorId | specialTariffId | specialPrice |
      | CUS1       | 1            | PT2H     | OP1        | 10              | 15.00        |
    When a POST request has been made to "/api/rentals" endpoint
    Then the response status is 422
    And the response contains
      | path        | value                    |
      | $.errorCode | tariff.special.type_invalid |
```

---

## 4. Explanation of Assertions

### Scenario: discount

- `discountPercent = 10`, bicycle 120min → V2 `itemCost = 16.00`, `totalCost = 14.40`
- `RentalEquipment.estimatedCost = 16.00` (pre-discount item cost from V2 breakdown)
- `Rental.getEstimatedCost()` = sum(16.00) × 0.9 = **14.40** (discount applied at domain level)
- DB: `discountPercent = 10` asserted via `rental was persisted in database` step updated in Task 011

### Scenario: SPECIAL tariff

- `specialTariffId = 13` (SPECIAL type), `specialPrice = 15.00`
- V2 SPECIAL returns `itemCost = 0` per breakdown item; `totalCost = specialPrice = 15.00`
- `Rental.getEstimatedCost()` short-circuits on `specialPrice` → **15.00**
- `RentalEquipment.estimatedCost = 0` (not asserted in the scenario to keep it focused on the total)
- DB: `specialTariffId = 13, specialPrice = 15.00` asserted via `rental was persisted in database`

### Scenario: non-SPECIAL tariff rejection

- `specialTariffId = 10` (DEGRESSIVE_HOURLY, **not** SPECIAL)
- `TariffV2Facade.calculateRentalCost` throws `InvalidSpecialTariffTypeException` (Task 009)
- `RentalRestControllerAdvice.handleInvalidSpecialTariffType` maps it to **422** (Task 009)
- `errorCode = "tariff.special.type_invalid"` (from `InvalidSpecialTariffTypeException.ERROR_CODE`)
- The `the response contains` step is the generic json-path assertion step already used in
  `rental.feature` (e.g., the "no suitable tariff found" scenario).

---

## 5. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test,docker"
```
