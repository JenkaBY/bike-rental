package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateVariableDescriptor;

import java.util.List;

public interface FindAgreementTemplateVariablesUseCase {

    List<AgreementTemplateVariableDescriptor> execute();
}
