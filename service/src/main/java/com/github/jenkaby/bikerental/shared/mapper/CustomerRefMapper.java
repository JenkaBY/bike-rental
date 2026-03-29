package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper
public interface CustomerRefMapper {

    default CustomerRef fromUUID(UUID id) {
        return CustomerRef.of(id);
    }

    default UUID toUUID(CustomerRef ref) {
        return ref == null ? null : ref.id();
    }
}
