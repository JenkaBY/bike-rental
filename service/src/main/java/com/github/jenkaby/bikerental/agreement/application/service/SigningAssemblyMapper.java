package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.SigningSnapshot;
import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.rental.RentalSigningSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
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

    default AgreementPdfData toPdfData(AgreementTemplate template,
                                       CustomerInfo customer,
                                       RentalSigningSnapshot snapshot,
                                       Instant startedAt,
                                       byte[] signaturePng) {
        var customerData = new AgreementPdfData.CustomerData(
                customer.firstName(), customer.lastName(), customer.phone());
        var rentalData = new AgreementPdfData.RentalData(
                snapshot.rentalId(),
                startedAt,
                snapshot.plannedDuration(),
                toPdfEquipmentLines(snapshot.equipments()),
                snapshot.estimatedCost(),
                snapshot.discountPercent(),
                snapshot.specialPrice());
        return new AgreementPdfData(template, customerData, rentalData, signaturePng);
    }

    default SigningSnapshot toSigningSnapshot(CustomerInfo customer,
                                              RentalSigningSnapshot snapshot,
                                              LocalDateTime startedAt,
                                              Long templateId,
                                              Integer templateVersionNumber,
                                              String templateContentSha256) {
        var customerPart = new SigningSnapshot.Customer(
                customer.firstName(), customer.lastName(), customer.phone());
        var rentalPart = new SigningSnapshot.Rental(
                snapshot.rentalId(),
                snapshot.version(),
                snapshot.plannedDuration(),
                startedAt,
                toSnapshotEquipmentLines(snapshot.equipments()),
                snapshot.estimatedCost(),
                snapshot.discountPercent(),
                snapshot.specialPrice());
        var templatePart = new SigningSnapshot.Template(templateId, templateVersionNumber, templateContentSha256);
        return new SigningSnapshot(customerPart, rentalPart, templatePart);
    }
}
