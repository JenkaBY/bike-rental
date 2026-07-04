<task_file_template>

# Task 011: Create the SigningAssemblyMapper (MapStruct)

> **Applied Skill:** `mapstruct-hexagonal` — a package-private `@Mapper` interface in the SAME package as its consumer
> service (mirrors `rental/RentalSigningSnapshotMapper.java`), with `default` methods for value-object conversions and
> multi-source mapping. Avoids hand-assembling the `AgreementPdfData` / `SigningSnapshot` field-by-field inside the
> service. Zero inline comments. Depends on Task 003.

## 1. Objective

Create a mapper that assembles the renderer input `AgreementPdfData` and the persisted `SigningSnapshot` from the rental
snapshot (`RentalSigningSnapshot`), the customer (`CustomerInfo`), the active template, the resolved `startedAt`, and the
decoded PNG bytes. The equipment line list is mapped once and reused by both.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/SigningAssemblyMapper.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.model.SigningSnapshot;
import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.rental.RentalSigningSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
interface SigningAssemblyMapper {

    @Mapping(target = "uid", source = "equipmentUid")
    @Mapping(target = "name", source = "equipmentTypeSlug")
    @Mapping(target = "estimatedCost", source = "estimatedCost")
    AgreementPdfData.EquipmentLine toPdfEquipmentLine(RentalSigningSnapshot.EquipmentItem item);

    List<AgreementPdfData.EquipmentLine> toPdfEquipmentLines(List<RentalSigningSnapshot.EquipmentItem> items);

    @Mapping(target = "uid", source = "equipmentUid")
    @Mapping(target = "name", source = "equipmentTypeSlug")
    @Mapping(target = "estimatedCost", source = "estimatedCost")
    SigningSnapshot.EquipmentLine toSnapshotEquipmentLine(RentalSigningSnapshot.EquipmentItem item);

    List<SigningSnapshot.EquipmentLine> toSnapshotEquipmentLines(List<RentalSigningSnapshot.EquipmentItem> items);

    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "customer.firstName", source = "customer.firstName")
    @Mapping(target = "customer.lastName", source = "customer.lastName")
    @Mapping(target = "customer.phone", source = "customer.phone")
    @Mapping(target = "rental.rentalId", source = "snapshot.rentalId")
    @Mapping(target = "rental.startedAt", source = "startedAt")
    @Mapping(target = "rental.plannedDuration", source = "snapshot.plannedDuration")
    @Mapping(target = "rental.equipments", source = "snapshot.equipments")
    @Mapping(target = "signaturePng", source = "signaturePng")
    AgreementPdfData toPdfData(String title,
                               String content,
                               CustomerInfo customer,
                               RentalSigningSnapshot snapshot,
                               LocalDateTime startedAt,
                               byte[] signaturePng);

    @Mapping(target = "customer.firstName", source = "customer.firstName")
    @Mapping(target = "customer.lastName", source = "customer.lastName")
    @Mapping(target = "customer.phone", source = "customer.phone")
    @Mapping(target = "rental.rentalId", source = "snapshot.rentalId")
    @Mapping(target = "rental.rentalVersion", source = "snapshot.version")
    @Mapping(target = "rental.plannedDuration", source = "snapshot.plannedDuration")
    @Mapping(target = "rental.startedAt", source = "startedAt")
    @Mapping(target = "rental.equipments", source = "snapshot.equipments")
    @Mapping(target = "rental.estimatedTotal", source = "snapshot.estimatedCost")
    @Mapping(target = "template.templateId", source = "templateId")
    @Mapping(target = "template.versionNumber", source = "templateVersionNumber")
    @Mapping(target = "template.contentSha256", source = "templateContentSha256")
    SigningSnapshot toSigningSnapshot(CustomerInfo customer,
                                      RentalSigningSnapshot snapshot,
                                      LocalDateTime startedAt,
                                      Long templateId,
                                      Integer templateVersionNumber,
                                      String templateContentSha256);
}
```

> The two `toPdfData` / `toSigningSnapshot` methods each map the `equipments` list via the entry line mappers above;
> MapStruct selects `toPdfEquipmentLines` for the `AgreementPdfData` target and `toSnapshotEquipmentLines` for the
> `SigningSnapshot` target by return type. Do NOT add a component-model annotation attribute — the project sets
> `-Amapstruct.defaultComponentModel=spring` globally, so this interface is a Spring bean automatically.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
