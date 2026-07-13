package com.github.jenkaby.bikerental.tariff;

import java.util.Optional;
import java.util.UUID;

public interface TariffV2Facade {

    Optional<TariffV2Info> findById(Long tariffId);

    /**
     * @deprecated superseded by {@link #calculateRentalCostV2(RentalCostCalculationV2Command)}, which bills each
     * equipment item from a shared {@code startAt} and its own {@code returnAt} timestamp. Retained only for the
     * legacy V1 cost-calculation endpoint; do not use in new code.
     */
    @Deprecated(forRemoval = true)
    RentalCostCalculationResult calculateRentalCost(RentalCostCalculationCommand command);

    RentalCostCalculationResult calculateRentalCostV2(RentalCostCalculationV2Command command);

    RentalCostQuote getQuote(UUID quoteId) throws QuoteNotFoundException, QuoteExpiredException;

    RentalCostQuote consumeQuote(UUID quoteId) throws QuoteNotFoundException, QuoteExpiredException, QuoteAlreadyConsumedException;
}
