package com.github.jenkaby.bikerental.agreement.web.command.dto;

import java.time.Instant;

public record SignatureCreatedResponse(Long signatureId, Instant signedAt) {
}
