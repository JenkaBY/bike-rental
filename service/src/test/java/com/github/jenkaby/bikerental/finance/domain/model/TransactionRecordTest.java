package com.github.jenkaby.bikerental.finance.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRecordTest {

    @ParameterizedTest(name = "{0} {1} {2} -> {3}")
    @CsvSource({
            "CASH,            DEBIT,  50.00, 50.00",
            "CASH,            CREDIT, 50.00, -50.00",
            "CARD_TERMINAL,   DEBIT,  30.00, 30.00",
            "BANK_TRANSFER,   CREDIT, 30.00, -30.00",
            "CUSTOMER_WALLET, DEBIT,  10.00, -10.00",
            "CUSTOMER_WALLET, CREDIT, 10.00, 10.00",
            "CUSTOMER_HOLD,   DEBIT,  10.00, -10.00",
            "CUSTOMER_HOLD,   CREDIT, 10.00, 10.00",
            "REVENUE,         CREDIT, 70.00, 70.00",
            "ADJUSTMENT,      DEBIT,  70.00, -70.00"
    })
    void signedBalanceDeltaRespectsAssetLedgerConvention(LedgerType ledgerType, EntryDirection direction,
                                                         String amount, String expectedSignedDelta) {
        var record = TransactionRecord.builder()
                .id(UUID.randomUUID())
                .subLedgerRef(new SubLedgerRef(UUID.randomUUID()))
                .ledgerType(ledgerType)
                .direction(direction)
                .amount(Money.of(amount))
                .build();

        assertThat(record.signedBalanceDelta().amount())
                .isEqualByComparingTo(expectedSignedDelta);
    }
}
