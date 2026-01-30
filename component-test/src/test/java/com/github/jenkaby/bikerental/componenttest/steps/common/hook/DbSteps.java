package com.github.jenkaby.bikerental.componenttest.steps.common.hook;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DbSteps {

    private static final List<String> TABLE_TO_TRUNCATE = List.of(
            "customers",
            "equipment_statuses",
            "equipment_types",
            "equipments"
    );

    private final JdbcClient jdbcClient;

    @After
    public void truncateDb() {
        log.info("Deleting all records on tables {}", TABLE_TO_TRUNCATE);
        JdbcTestUtils.deleteFromTables(jdbcClient, TABLE_TO_TRUNCATE.toArray(new String[0]));
    }

    @Given("the database is empty for {string} table")
    public void theDatabaseIsEmptyForTable(String tableName) {
        log.info("Deleting all records from table {}", tableName);
        JdbcTestUtils.deleteFromTables(jdbcClient, tableName);
    }

    @Given("the {string} table is empty")
    public void theTableIsEmpty(String tableName) {
        theDatabaseIsEmptyForTable(tableName);
    }
}
