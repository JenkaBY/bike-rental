# Bike Rental Application

[![Java CI](https://github.com/jenkaby/bike-rental/actions/workflows/build.yml/badge.svg)](https://github.com/jenkaby/bike-rental/actions/workflows/build.yml)
[![Component Test Coverage](https://jenkaby.github.io/bike-rental/badges/jacoco.svg?raw=true)](https://jenkaby.github.io/bike-rental/)
[![Component Branch Coverage](https://jenkaby.github.io/bike-rental/badges/branches.svg?raw=true)](https://jenkaby.github.io/bike-rental/)

The API is available at: [API](https://bike-rental-app-8pwz.onrender.com/swagger-ui/index.htm). Be patient; it loads
slowly on the first request because of the free tier.

It's swagger documentation is available
at: [swagger-ui/index.html](https://bike-rental-app-8pwz.onrender.com/swagger-ui/index.html)

The UI project is available at: [UI](https://github.com/JenkaBY/bikerental-ui). Follow the readme.md file there.

## Project Overview

This project is a bike rental application designed to manage the rental process, including customer management,
equipment management, tariff management, and financial transactions.
The application is built using Java and Spring Boot, with a modular architecture to ensure scalability and
maintainability.

## Local development

### Prerequisites

- Java 21
- Docker and Docker Compose

### Run infrastructure

1. Start docker containers from root directory:
   ```bash
   docker compose -f ./docker/docker-compose.yaml up -d
   ```

### Testing

There are 2 kinds of tests: 
- unit tests. Inside the `service` module.
- component(integration) tests. It's located in the `component-test` module and implemented via Cucumber framework. Read
  more in the [component-test/README.md](./component-test/README.md).


To run tests, use the following command from the root directory:
```shell
./gradlew test "-Dspring.profiles.active=test,docker"
```

## Dev Deployment

The application is deployed on Render.com as Docker container. See [deployment.md](./deployment.md) for detailed deployment instructions.

## License

This project is proprietary software. All rights reserved by the author.
See [LICENSE](LICENSE) for details.

## Changelog

- *2026-03-05* Migrate to spec-workflow. See [README](README-spec-flow.MD)