package com.github.jenkaby.bikerental.agreement.infrastructure.pdf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.agreement.pdf")
public record AgreementPdfProperties(
        @DefaultValue("fonts/DejaVuSans.ttf") String fontLocation,
        @DefaultValue("50") float margin,
        @DefaultValue("11") float bodyFontSize,
        @DefaultValue("6") float smallFontSize,
        @DefaultValue("16") float titleFontSize,
        @DefaultValue("1.4") float leadingFactor,
        @DefaultValue("180") float signatureWidth,
        @DefaultValue("80") float signatureHeight,
        @DefaultValue("Signature placeholder") String signaturePlaceholderLabel,
        @DefaultValue("dd.MM.yyyy HH:mm") String dateTimePattern,
        @DefaultValue("dd.MM.yyyy") String datePattern,
        @DefaultValue("Europe/Minsk") String zoneId,
        @DefaultValue("BYN") String currency) {
}
