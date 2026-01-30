package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.application.usecase.SearchEquipmentsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class SearchEquipmentsService implements SearchEquipmentsUseCase {

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> execute(SearchEquipmentsQuery query) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
