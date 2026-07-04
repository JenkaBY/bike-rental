package com.github.jenkaby.bikerental.agreement.domain.service;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;

public interface AgreementPdfRenderer {

    byte[] render(AgreementPdfData data);
}
