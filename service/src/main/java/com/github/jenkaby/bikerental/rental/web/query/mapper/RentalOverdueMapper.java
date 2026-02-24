package com.github.jenkaby.bikerental.rental.web.query.mapper;

import com.github.jenkaby.bikerental.rental.application.service.RentalOverdueCalculator;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper
@RequiredArgsConstructor
public abstract class RentalOverdueMapper {

    protected RentalOverdueCalculator rentalOverdueCalculator;

    public Integer calculate(Rental rental) {
        return rentalOverdueCalculator.calculateOverdueMinutes(rental);
    }

    @Autowired
    public void setRentalOverdueCalculator(RentalOverdueCalculator rentalOverdueCalculator) {
        this.rentalOverdueCalculator = rentalOverdueCalculator;
    }
}
