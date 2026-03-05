# [TECH-012] - Connect Spring Boot Actuator

**Status:** Completed  
**Added:** 2026-03-05  
**Updated:** 2026-03-05

## Original Request

Подключить Spring Boot Actuator с требованиями:

- Настройки management endpoints вынести в `application-management-config.yaml`
- Actuator должен возвращать текущую версию и другую информацию о приложении (build info)

## Thought Process

Actuator в проекте не был подключён (см. TECH-008 — при настройке deploy пришлось убрать healthCheckPath).
Конфигурация management выносится в отдельный файл для разделения ответственности. Build info уже настроен в
build.gradle.

## Implementation Plan

- [x] 2.1 Добавить `spring-boot-starter-actuator` в gradle/libs.versions.toml и service/build.gradle
- [x] 2.2 Создать service/src/main/resources/application-management-config.yaml с настройками management.endpoints,
  management.endpoint.health, management.info.env.enabled
- [x] 2.3 Добавить в application.yaml импорт: spring.config.import: optional:classpath:
  application-management-config.yaml
- [x] 2.4 Настроить /actuator/info для отдачи build info (версия, git.commit) — генерируется через buildInfo в
  build.gradle
- [x] 2.5 Создать component test: component-test/src/test/resources/features/actuator.feature — сценарии для GET
  /actuator/health (status UP) и GET /actuator/info (build.version)
- [x] 2.6 Обновить memory-bank/techContext.md — зафиксировать фактическую конфигурацию

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                                                             | Status   | Updated    | Notes                   |
|-----|-------------------------------------------------------------------------|----------|------------|-------------------------|
| 2.1 | Add spring-boot-starter-actuator to libs.versions.toml and build.gradle | Complete | 2026-03-05 |                         |
| 2.2 | Create application-management-config.yaml                               | Complete | 2026-03-05 |                         |
| 2.3 | Add spring.config.import to application.yaml                            | Complete | 2026-03-05 |                         |
| 2.4 | Verify buildInfo for /actuator/info                                     | Complete | 2026-03-05 | Already in build.gradle |
| 2.5 | Create actuator.feature component test                                  | Complete | 2026-03-05 |                         |
| 2.6 | Update techContext.md                                                   | Complete | 2026-03-05 |                         |

## Progress Log

### 2026-03-05

- Task created and implemented. Added spring-boot-starter-actuator, application-management-config.yaml,
  spring.config.import, actuator.feature component test. Updated techContext.md.
