package com.github.jenkaby.bikerental.agreement.domain.service;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;

public interface AgreementContentRenderer {

    String substitute(AgreementPdfData data);
}
