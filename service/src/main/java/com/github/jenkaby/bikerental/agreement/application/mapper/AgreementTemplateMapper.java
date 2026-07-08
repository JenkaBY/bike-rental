package com.github.jenkaby.bikerental.agreement.application.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.RentalAgreementView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AgreementTemplateMapper {

    @Mapping(target = "templateActivatedAt", source = "source.activatedAt")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "templateId", source = "source.id")
    RentalAgreementView toView(AgreementTemplate source, String content, String title);
}
