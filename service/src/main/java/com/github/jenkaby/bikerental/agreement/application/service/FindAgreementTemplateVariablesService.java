package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateVariablesUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateVariable;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateVariableDescriptor;
import com.github.jenkaby.bikerental.shared.application.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
class FindAgreementTemplateVariablesService implements FindAgreementTemplateVariablesUseCase {

    private final MessageService messageService;

    FindAgreementTemplateVariablesService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public List<AgreementTemplateVariableDescriptor> execute() {
        return Arrays.stream(AgreementTemplateVariable.values())
                .map(variable -> new AgreementTemplateVariableDescriptor(
                        variable.key(),
                        messageService.getMessage(variable.descriptionCode()),
                        messageService.getMessage(variable.exampleCode())))
                .toList();
    }
}
