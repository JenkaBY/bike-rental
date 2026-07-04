package com.github.jenkaby.bikerental.agreement.domain.model;

import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotActivatableException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotDeletableException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotEditableException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgreementTemplate {

    @Setter
    private Long id;

    private Long lockVersion;

    private Integer versionNumber;

    private String title;

    private String content;

    private String contentSha256;

    private AgreementTemplateStatus status;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant activatedAt;
    private Instant deactivatedAt;

    public static AgreementTemplate createDraft(String title, String content) {
        Instant now = Instant.now();
        return AgreementTemplate.builder()
                .title(title)
                .content(content)
                .status(AgreementTemplateStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateContent(String title, String content) {
        if (this.status != AgreementTemplateStatus.DRAFT) {
            throw new AgreementTemplateNotEditableException(this.status);
        }
        this.title = title;
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void activate(int versionNumber, String contentSha256, Instant activatedAt) {
        if (this.status != AgreementTemplateStatus.DRAFT) {
            throw new AgreementTemplateNotActivatableException(this.status);
        }
        this.versionNumber = versionNumber;
        this.contentSha256 = contentSha256;
        this.status = AgreementTemplateStatus.ACTIVE;
        this.activatedAt = activatedAt;
        this.updatedAt = activatedAt;
    }

    public void deactivate(Instant deactivatedAt) {
        if (this.status != AgreementTemplateStatus.ACTIVE) {
            throw new AgreementTemplateNotActivatableException(this.status);
        }
        this.status = AgreementTemplateStatus.DEACTIVATED;
        this.deactivatedAt = deactivatedAt;
        this.updatedAt = deactivatedAt;
    }

    public void ensureDeletable() {
        if (this.status != AgreementTemplateStatus.DRAFT) {
            throw new AgreementTemplateNotDeletableException(this.status);
        }
    }
}
