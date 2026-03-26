package com.github.jenkaby.bikerental.shared.domain.event;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;

public record CustomerRegistered(CustomerRef customerRef) implements BikeRentalEvent {
}
