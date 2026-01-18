# Description

This module contains component tests. To run the tests, a **test** **profile** must be **activated** as minimum.

## Local development and running tests

The tests could be run against the dockerized infrastructure or without it.

NB:

1. Generally, to speed up the tests execution the dockerized infrastructure can be run once via
   the [docker-compose.yaml](../docker/docker-compose.yaml).
   It will minimize the time needed to run the tests for set up environment via testcontainers by skipping the
   infrastructure set up phase.
2. Additionally, liquibase migrations is disabled for the **test** profile, so, the database schema should be already
   created and up to date. It can be achieved by:
    - change `spring.liquibase.enabled=true` in the [application-test.yaml](/src/test/resources/application-test.yaml)
      file
    - to apply liquibase migrations, run component tests only once with the **test** profile against the dockerized
      infrastructure running.
    - after that, revert(`spring.liquibase.enabled=false`) the change in
      the [application-test.yaml](/src/test/resources/application-test.yaml) file
    - further component tests runs can be executed without the liquibase migrations, until the database schema changes
      are introduced.

## Running tests in CI/CD

It runs in the CI/CD pipeline on GitHub with the spring profiles:

- test
- docker