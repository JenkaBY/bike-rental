package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;

import java.util.List;

public interface FindRentalSignaturesUseCase {

    List<AgreementSignatureSummary> execute(Long rentalId);
}
