package com.github.jenkaby.bikerental.rental.web.command.mapper;

import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.web.command.dto.SettlementResponse;
import com.github.jenkaby.bikerental.shared.domain.TransactionRef;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SettlementMapper {

    SettlementResponse toResponse(SettlementInfo source);

    default List<UUID> toUuidList(List<TransactionRef> source) {
        if (source == null) {
            return null;
        }
        return source.stream()
                .map(SettlementMapper::toUuid)
                .toList();
    }

    static UUID toUuid(TransactionRef source) {
        return source != null ? source.id() : null;
    }
}
