# [TECH-010] - CORS Filter with Configurable Allowed Origins

**Status:** Completed  
**Added:** 2026-02-28  
**Updated:** 2026-02-28

## Original Request

Добавить CORS фильтр, который принимает разрешённые хосты из `application.yaml`.

## Thought Process

Фронтенд приложения работает на отдельном домене/порту и отправляет cross-origin запросы к REST API. Без настроенного
CORS браузер блокирует такие запросы. Разрешённые origins должны быть конфигурируемы через `application.yaml`, чтобы
можно было задавать разные значения для dev / staging / prod окружений.

### Подход

Использовать Spring MVC `CorsConfigurationSource` + `WebMvcConfigurer.addCorsMappings()` (или `CorsFilter` бин).
Конфигурацию хранить в `AppProperties` (или отдельном `CorsProperties`) с биндингом
через `@ConfigurationProperties`. Свойство:

```yaml
app:
  cors:
    allowed-origins:
      - "http://localhost:3000"
      - "https://bikerent.example.com"
    allowed-methods:
      - GET
      - POST
      - PUT
      - PATCH
      - DELETE
      - OPTIONS
    allowed-headers:
      - "*"
    allow-credentials: true
    max-age: 3600
```

Класс `CorsProperties` (`@ConfigurationProperties(prefix = "app.cors")`) будет содержать:

- `allowedOrigins: List<String>`
- `allowedMethods: List<String>`
- `allowedHeaders: List<String>`
- `allowCredentials: boolean`
- `maxAge: long`

`CorsConfig` (`@Configuration`) реализует `WebMvcConfigurer` и регистрирует маппинг `/**` на основе `CorsProperties`.

### Архитектурная позиция

Конфигурационный класс располагается в `service/src/main/java/.../config/` — adapter-слой (web infrastructure).
Нет зависимостей от domain или application слоёв, что не нарушает гексагональную архитектуру.

### Тестирование

- **Unit-тест** `CorsConfigTest`: проверить, что `CorsConfigurationSource` возвращает корректный `CorsConfiguration`
  для разных origins из properties.
- **WebMvc-тест**: отправить preflight `OPTIONS` запрос и убедиться, что заголовки `Access-Control-Allow-Origin` и
  `Access-Control-Allow-Methods` присутствуют в ответе.
- **Интеграционный тест** (component-test): можно вынести в отдельный сценарий при необходимости.

## Implementation Plan

- [ ] 1.1 Создать `CorsProperties` — `@ConfigurationProperties(prefix = "app.cors")` с полями `allowedOrigins`,
  `allowedMethods`, `allowedHeaders`, `allowCredentials`, `maxAge`
- [ ] 1.2 Добавить конфигурацию `app.cors` в `application.yaml` со значениями по умолчанию для dev
- [ ] 1.3 Создать `CorsConfig` — `@Configuration` + `WebMvcConfigurer`, регистрирующий CORS маппинг `/**`
- [ ] 1.4 Добавить `CorsProperties` в основной `AppProperties` или зарегистрировать как отдельный бин
- [ ] 1.5 Написать unit-тест `CorsConfigTest`
- [ ] 1.6 Написать WebMvc-тест preflight OPTIONS запроса
- [ ] 1.7 Убедиться, что все тесты проходят с `test` профилем

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                               | Status   | Updated    | Notes                                                   |
|-----|-------------------------------------------|----------|------------|---------------------------------------------------------|
| 1.1 | Создать `CorsProperties`                  | Complete | 2026-02-28 | `@ConfigurationProperties(prefix = "app.cors")`         |
| 1.2 | Добавить `app.cors` в `application.yaml`  | Complete | 2026-02-28 | Dev origins: localhost:3000, localhost:5173             |
| 1.3 | Создать `CorsConfig` (WebMvcConfigurer)   | Complete | 2026-02-28 | `addCorsMappings` + `corsConfigurationSource` bean      |
| 1.4 | Зарегистрировать `CorsProperties` как бин | Complete | 2026-02-28 | Авто-регистрация через `@ConfigurationPropertiesScan`   |
| 1.5 | Unit-тест `CorsConfigTest`                | Complete | 2026-02-28 | 4 теста — origins, methods, credentials, wildcard paths |
| 1.6 | WebMvc-тест preflight OPTIONS             | Complete | 2026-02-28 | 2 теста — allowed origin и disallowed origin            |
| 1.7 | Прогон тестов с `test` профилем           | Complete | 2026-02-28 | 6/6 PASSED                                              |

## Progress Log

### 2026-02-28

- Тикет создан
- Определён подход: `CorsProperties` + `CorsConfig` (WebMvcConfigurer)
- Задокументированы subtasks и план реализации

### 2026-02-28 (реализация)

- Создан `CorsProperties` — `@ConfigurationProperties(prefix = "app.cors")` с полями `allowedOrigins`,
  `allowedMethods`, `allowedHeaders`, `allowCredentials`, `maxAge`
- Создан `CorsConfig` — `@Configuration` + `WebMvcConfigurer.addCorsMappings()` + `CorsConfigurationSource` бин
- `application.yaml` дополнен секцией `app.cors` с dev origins (localhost:3000, localhost:5173)
- `application-test.yaml` дополнен секцией `app.cors` для тестового профиля
- `CorsProperties` авто-регистрируется через `@ConfigurationPropertiesScan` в `BikeRentalApplication`
- Написан `CorsConfigTest` с 4 unit-тестами: origins, methods, credentials, wildcard paths
- Написан `CorsPreflightTest` с 2 WebMvc-тестами: preflight с разрешённым origin и запрос с запрещённым origin
- Все 6 тестов прошли: `BUILD SUCCESSFUL`

