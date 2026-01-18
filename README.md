# Bike Rental Application

## Project Overview


## Local development

### Prerequisites
- Java 25
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
./gradlew test -Dspring.profiles.active=test,docker
```


### OpenRewrite usage

Modify the [openrework.gradle](openrework.gradle) file, add/uncomment recipes and run

```shell
./gradlew rewriteRun --init-script openrework.gradle
```