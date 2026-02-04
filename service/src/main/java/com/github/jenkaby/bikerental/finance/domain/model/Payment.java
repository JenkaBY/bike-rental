package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Payment {

    @Setter
    private UUID id;
    private final Long rentalId;
    private final Money amount;
    private final PaymentType paymentType;
    private final PaymentMethod paymentMethod;
    private final Instant createdAt;
    private final String operatorId;
    private final String receiptNumber;

    public boolean hasRental() {
        return rentalId != null;
    }

    public boolean isForRental(Long rentalId) {
        return this.rentalId != null && this.rentalId.equals(rentalId);
    }


}
