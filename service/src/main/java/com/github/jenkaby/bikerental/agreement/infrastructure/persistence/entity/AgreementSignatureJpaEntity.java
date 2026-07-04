package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity;

import com.github.jenkaby.bikerental.agreement.domain.model.SigningSnapshot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agreement_signatures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AgreementSignatureJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false, updatable = false)
    private Long templateId;

    @Column(name = "rental_id", nullable = false, updatable = false)
    private Long rentalId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "operator_id", nullable = false, updatable = false, length = 100)
    private String operatorId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signing_snapshot", nullable = false, updatable = false, columnDefinition = "jsonb")
    private SigningSnapshot signingSnapshot;

    @ToString.Exclude
    @Column(name = "pdf_document", nullable = false, updatable = false)
    private byte[] pdfDocument;

    @Column(name = "pdf_sha256", nullable = false, updatable = false, length = 64)
    private String pdfSha256;

    @Column(name = "template_content_sha256", nullable = false, updatable = false, length = 64)
    private String templateContentSha256;

    @ToString.Exclude
    @Column(name = "signature_image", nullable = false, updatable = false)
    private byte[] signatureImage;

    @Column(name = "signed_at", nullable = false, updatable = false)
    private Instant signedAt;

    @Column(name = "ip_address", updatable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", updatable = false)
    private String userAgent;
}
