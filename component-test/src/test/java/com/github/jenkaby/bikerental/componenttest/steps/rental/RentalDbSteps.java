package com.github.jenkaby.bikerental.componenttest.steps.rental;

import com.github.jenkaby.bikerental.componenttest.config.db.repository.InsertableRentalRepository;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RentalDbSteps {

    private final InsertableRentalRepository rentalRepository;
    private final ScenarioContext scenarioContext;

    @Given("(a )rental(s) exist(s) in the database with the following data")
    public void aRentalExistsInTheDatabaseWithTheFollowingData(List<RentalJpaEntity> rentals) {
        log.debug("Creating rentals in database: {}", rentals);

        rentalRepository.insertAll(rentals);
    }

    @Given("a single rental exists in the database with the following data")
    public void aSingleRentalExistsInTheDatabaseWithTheFollowingData(RentalJpaEntity rental) {
        log.debug("Creating rental in database: {}", rental);

        var insert = rentalRepository.insert(rental);
        scenarioContext.setRequestedObjectId(insert.getId().toString());
    }
}
