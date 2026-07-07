package com.github.jenkaby.bikerental.agreement.web.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateVariableDescriptor;
import com.github.jenkaby.bikerental.agreement.domain.model.RentalAgreementView;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateSummaryResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateVariableResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.RentalAgreementResponse;
import com.github.jenkaby.bikerental.shared.mapper.InstantMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = {InstantMapper.class})
public interface AgreementTemplateWebMapper {

    AgreementTemplateResponse toResponse(AgreementTemplate template);

    AgreementTemplateSummaryResponse toSummaryResponse(AgreementTemplateSummary summary);

    List<AgreementTemplateSummaryResponse> toSummaryResponses(List<AgreementTemplateSummary> summaries);

    AgreementTemplateVariableResponse toVariableResponse(AgreementTemplateVariableDescriptor descriptor);

    List<AgreementTemplateVariableResponse> toVariableResponses(List<AgreementTemplateVariableDescriptor> descriptors);

    RentalAgreementResponse toRentalAgreementResponse(RentalAgreementView view);
}
