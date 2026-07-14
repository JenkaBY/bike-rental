package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.QuoteRef;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper
public interface QuoteRefMapper {

    default QuoteRef fromUUID(UUID id) {
        return id == null ? null : QuoteRef.of(id);
    }

    default UUID toUUID(QuoteRef ref) {
        return ref == null ? null : ref.id();
    }
}
