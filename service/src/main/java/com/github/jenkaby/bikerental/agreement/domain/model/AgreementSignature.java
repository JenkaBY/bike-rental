package com.github.jenkaby.bikerental.agreement.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgreementSignature {

    private Long id;

    private Long templateId;

    private Long rentalId;

    private UUID customerId;

    private String operatorId;

    private SigningSnapshot signingSnapshot;

    private byte[] pdfDocument;

    private String pdfSha256;

    private String templateContentSha256;

    private byte[] signatureImage;

    private Instant signedAt;

    private String ipAddress;

    private String userAgent;

    public static AgreementSignature create(Long templateId,
                                            Long rentalId,
                                            UUID customerId,
                                            String operatorId,
                                            SigningSnapshot signingSnapshot,
                                            byte[] pdfDocument,
                                            String pdfSha256,
                                            String templateContentSha256,
                                            byte[] signatureImage,
                                            Instant signedAt,
                                            String ipAddress,
                                            String userAgent) {
        return AgreementSignature.builder()
                .templateId(templateId)
                .rentalId(rentalId)
                .customerId(customerId)
                .operatorId(operatorId)
                .signingSnapshot(signingSnapshot)
                .pdfDocument(pdfDocument)
                .pdfSha256(pdfSha256)
                .templateContentSha256(templateContentSha256)
                .signatureImage(signatureImage)
                .signedAt(signedAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }
}
