package com.github.jenkaby.bikerental.componenttest.steps.common.hook;

import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import io.cucumber.java.After;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DbSteps {

    private static final String SYSTEM_LEDGER_TYPES = joinEnumNames(LedgerType.class, LedgerType::isSystemLedger);
    private static final String SYSTEM_ACCOUNTS = joinEnumNames(AccountType.class, AccountType::isSystem);


    private static final List<String> TABLE_TO_TRUNCATE = List.of(
            // order is important due to foreign key constraints
            "event_publication", // from modulith event_api
            "event_publication_archive", // from modulith event_api
            "equipment_status_transition_rules",
            "customers",
            "equipments",
            "equipment_statuses",
            "equipment_types",
            "tariffs_v2",
            "rental_equipments",
            "rentals",
            "finance_transaction_records",
            "finance_transactions"
    );


    private final JdbcClient jdbcClient;

    @After
    public void truncateDb() throws InterruptedException {
        try {
            truncateInternally();
        } catch (Exception e) {
            log.error("Error while truncating tables {}, error: {}", TABLE_TO_TRUNCATE, e.getMessage(), e);
            log.info("Try truncate second time");
            Thread.sleep(200);
            truncateInternally();
        }
    }

    private void truncateInternally() {
        log.info("Deleting all records on tables {}", TABLE_TO_TRUNCATE);
        JdbcTestUtils.deleteFromTables(jdbcClient, TABLE_TO_TRUNCATE.toArray(new String[0]));
        JdbcTestUtils.deleteFromTableWhere(jdbcClient,
                "finance_sub_ledgers",
                "ledger_type NOT IN (%s)".formatted(SYSTEM_LEDGER_TYPES)
        );
        JdbcTestUtils.deleteFromTableWhere(jdbcClient,
                "finance_accounts",
                "account_type != %s".formatted(SYSTEM_ACCOUNTS)
        );
    }

    private static <E extends Enum<E>> String joinEnumNames(Class<E> enumClass, Predicate<E> filter) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(filter)
                .map(Enum::name)
                .collect(Collectors.joining("','", "'", "'"));
    }
}
