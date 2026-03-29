package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.IdempotencyKey;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper
public interface IdempotencyKeyMapper {

    default IdempotencyKey toIdempotencyKey(UUID uuid) {
        return IdempotencyKey.of(uuid);
    }

    default UUID toUuid(IdempotencyKey key) {
        return key.id();
    }
}
