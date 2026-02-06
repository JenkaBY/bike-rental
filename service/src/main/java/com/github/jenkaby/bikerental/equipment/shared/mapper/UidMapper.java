package com.github.jenkaby.bikerental.equipment.shared.mapper;

import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import org.mapstruct.Mapper;

@Mapper
public interface UidMapper {
    default Uid toUid(String value) {
        return value != null ? new Uid(value) : null;
    }

    default String toString(Uid uid) {
        return uid != null ? uid.value() : null;
    }
}
