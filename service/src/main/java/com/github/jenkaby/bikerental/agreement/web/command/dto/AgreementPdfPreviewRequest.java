package com.github.jenkaby.bikerental.agreement.web.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgreementPdfPreviewRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        String content
) {
}
