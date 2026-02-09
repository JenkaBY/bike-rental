package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;

import java.util.Map;


public interface UpdateRentalUseCase {

    /**
     * Updates rental according to JSON Merge Patch standard (RFC 7396).
     * Only specified fields in the patch map are updated.
     *
     * @param rentalId ID of the rental to update
     * @param patch    Map with fields to update (only specified fields are updated)
     * @return Updated rental
     */
    Rental execute(Long rentalId, Map<String, Object> patch);
}
