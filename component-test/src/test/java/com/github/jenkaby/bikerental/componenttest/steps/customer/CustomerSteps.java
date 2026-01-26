package com.github.jenkaby.bikerental.componenttest.steps.customer;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;

@Slf4j
@RequiredArgsConstructor
public class CustomerSteps {

    private final JdbcClient jdbcClient;

    @Given("the database is empty for {string} table")
    public void theDatabaseIsEmptyForTable(String tableName) {
        log.info("Deleting all records from table {}", tableName);
        JdbcTestUtils.deleteFromTables(jdbcClient, tableName);
    }
}
