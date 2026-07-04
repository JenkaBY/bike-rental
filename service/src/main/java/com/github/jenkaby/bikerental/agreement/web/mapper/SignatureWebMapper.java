package com.github.jenkaby.bikerental.agreement.web.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.web.query.dto.SignatureSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface SignatureWebMapper {

    @Mapping(target = "signatureId", source = "id")
    SignatureSummaryResponse toResponse(AgreementSignatureSummary summary);

    List<SignatureSummaryResponse> toResponses(List<AgreementSignatureSummary> summaries);
}
