package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementSignatureJpaEntity;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.projection.AgreementSignatureSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AgreementSignatureJpaRepository extends JpaRepository<AgreementSignatureJpaEntity, Long> {

    boolean existsByRentalId(Long rentalId);

    @Query("""
            select s.id as id, s.templateId as templateId,
                   t.versionNumber as templateVersionNumber, s.signedAt as signedAt
            from AgreementSignatureJpaEntity s
            join AgreementTemplateJpaEntity t on t.id = s.templateId
            where s.rentalId = :rentalId""")
    Optional<AgreementSignatureSummaryProjection> findSummaryByRentalId(@Param("rentalId") Long rentalId);

    @Query("select s.pdfDocument from AgreementSignatureJpaEntity s where s.rentalId = :rentalId")
    Optional<byte[]> findPdfByRentalId(@Param("rentalId") Long rentalId);
}
