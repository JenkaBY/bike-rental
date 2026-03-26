package com.github.jenkaby.bikerental.componenttest.steps.common.hook;

import io.cucumber.java.After;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DbSteps {

    private static final List<String> TABLE_TO_TRUNCATE = List.of(
            // order is important due to foreign key constraints
            "event_publication", // from modulith event_api
            "event_publication_archive", // from modulith event_api
            "equipment_status_transition_rules",
            "customers",
            "equipments",
            "equipment_statuses",
            "equipment_types",
            "tariffs",
            "tariffs_v2",
            "payments",
            "rental_equipments",
            "rentals",
            "finance_sub_ledgers",
            "finance_accounts"
        );

    private final JdbcClient jdbcClient;

    @After
    public void truncateDb() {
        log.info("Deleting all records on tables {}", TABLE_TO_TRUNCATE);
        JdbcTestUtils.deleteFromTables(jdbcClient, TABLE_TO_TRUNCATE.toArray(new String[0]));
    }
}
