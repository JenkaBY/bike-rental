package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.RentalAgreementView;

public interface FindRentalAgreementUseCase {

    RentalAgreementView execute(Long rentalId);
}
