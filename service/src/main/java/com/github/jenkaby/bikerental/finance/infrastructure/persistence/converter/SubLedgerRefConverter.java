package com.github.jenkaby.bikerental.finance.infrastructure.persistence.converter;

import com.github.jenkaby.bikerental.finance.domain.model.SubLedgerRef;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Converter
public class SubLedgerRefConverter implements AttributeConverter<SubLedgerRef, UUID> {

    @Override
    public @Nullable UUID convertToDatabaseColumn(@Nullable SubLedgerRef ref) {
        return ref == null ? null : ref.id();
    }

    @Override
    public @Nullable SubLedgerRef convertToEntityAttribute(@Nullable UUID uuid) {
        return new SubLedgerRef(uuid);
    }
}
