package com.github.jenkaby.bikerental.equipment.shared.mapper;

import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import org.mapstruct.Mapper;

@Mapper
public interface SerialNumberMapper {
    default SerialNumber toSerialNumber(String value) {
        return value != null ? new SerialNumber(value) : null;
    }

    default String toString(SerialNumber serialNumber) {
        return serialNumber != null ? serialNumber.value() : null;
    }
}
