package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgreementTemplateJpaRepository extends JpaRepository<AgreementTemplateJpaEntity, Long> {

    Optional<AgreementTemplateJpaEntity> findByStatus(AgreementTemplateStatus status);

    @Query("select coalesce(max(t.versionNumber), 0) from AgreementTemplateJpaEntity t")
    int findMaxVersionNumber();

    @Query("""
            select new com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary(
                t.id, t.versionNumber, t.title, t.status, t.createdAt, t.activatedAt, t.deactivatedAt)
            from AgreementTemplateJpaEntity t
            order by t.id desc""")
//    FIXME It's violation of layers usage. The persistence layer should not know about the domain layer.
//     The solution is to use a projection interface instead of a DTO class.
    List<AgreementTemplateSummary> findAllSummaries();
}
