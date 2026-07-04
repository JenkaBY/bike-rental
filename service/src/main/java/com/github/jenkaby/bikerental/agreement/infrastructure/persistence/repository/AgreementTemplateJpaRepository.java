package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
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
            select t.id as id, t.versionNumber as versionNumber, t.title as title, t.status as status,
                   t.createdAt as createdAt, t.activatedAt as activatedAt, t.deactivatedAt as deactivatedAt
            from AgreementTemplateJpaEntity t
            order by t.id desc""")
    List<AgreementTemplateSummaryProjection> findAllSummaries();
}
