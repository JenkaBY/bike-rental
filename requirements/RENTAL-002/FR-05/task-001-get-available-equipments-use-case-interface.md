# Task 001: Create `GetAvailableForRentEquipmentsUseCase` Interface

> **Applied Skill:** `d:\Projects\private\bikerent\.github\instructions\java.instructions.md` — Use-case interface
> placed in `rental/application/usecase/`; returns the shared domain `Page` of the rental-owned
> `AvailableForRentalEquipment` (not `EquipmentInfo` directly); follows the existing use-case interface naming
> pattern (`FindRentalsUseCase`, `GetRentalByIdUseCase`).

## 1. Objective

Create the use-case contract interface `GetAvailableForRentEquipmentsUseCase` in `rental/application/usecase/`. It
declares a single method that accepts the equipment search filter and page request and returns a domain `Page` of the
rental-owned `AvailableForRentalEquipment` record. The controller and any future callers depend only on this interface,
not on `EquipmentInfo` from the equipment module.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/GetAvailableForRentEquipmentsUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

public interface GetAvailableForRentEquipmentsUseCase {

    Page<AvailableForRentalEquipment> getAvailableEquipments(EquipmentSearchFilter filter, PageRequest pageRequest);
}
```

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
