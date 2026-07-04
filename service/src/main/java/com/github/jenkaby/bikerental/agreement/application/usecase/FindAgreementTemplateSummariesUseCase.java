package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;

import java.util.List;

public interface FindAgreementTemplateSummariesUseCase {

    List<AgreementTemplateSummary> execute();
}
