# [TECH-011] - Add Proprietary License to Project

**Status:** Completed  
**Added:** 2026-03-02  
**Updated:** 2026-03-02  
**Priority:** Low  

## Original Request

Добавить лицензию **All Rights Reserved** к проекту, чтобы закрыть код от использования
части или всего кода без согласия автора. Заголовки Copyright в исходных файлах не нужны —
достаточно одного файла `LICENSE` в корне проекта.

## Thought Process

### Анализ совместимости зависимостей

Перед выбором лицензии была проверена совместимость всех зависимостей проекта с закрытой лицензией:

| Зависимость         | Лицензия       | Совместима с проприетарной? |
|---------------------|----------------|-----------------------------|
| Spring Boot         | Apache 2.0     | ✅ Да                        |
| Spring Modulith     | Apache 2.0     | ✅ Да                        |
| Liquibase           | Apache 2.0     | ✅ Да                        |
| Lombok              | MIT            | ✅ Да                        |
| MapStruct           | Apache 2.0     | ✅ Да                        |
| SpringDoc OpenAPI   | Apache 2.0     | ✅ Да                        |
| PostgreSQL JDBC     | BSD 2-Clause   | ✅ Да                        |
| Cucumber            | MIT            | ✅ Да                        |
| JUnit 5             | EPL 2.0        | ✅ Да                        |
| Mockito             | MIT            | ✅ Да                        |
| Testcontainers      | MIT            | ✅ Да                        |
| ArchUnit            | Apache 2.0     | ✅ Да                        |
| uuid-creator        | MIT            | ✅ Да                        |

Все зависимости используют **permissive лицензии** (Apache 2.0, MIT, BSD, EPL-2.0).
Ни одна из них не является GPL/AGPL и не требует открытия исходного кода.
Применение закрытой лицензии **полностью совместимо** с текущим стеком.

### Выбор лицензии

Выбрана лицензия **All Rights Reserved** как наиболее простой и строгий вариант:
- Один файл `LICENSE` в корне проекта.
- Запрет воспроизведения, распространения и модификации без явного письменного разрешения автора.
- Не требует заголовков Copyright в каждом исходном файле.

## Implementation Plan

- [x] 1.1 Создать файл `LICENSE` в корне проекта с текстом All Rights Reserved
- [x] 1.2 Добавить секцию `## License` в `README.md` со ссылкой на `LICENSE`

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                          | Status   | Updated    | Notes                                       |
|-----|--------------------------------------|----------|------------|---------------------------------------------|
| 1.1 | Создать файл `LICENSE`               | Complete | 2026-03-02 | All Rights Reserved, автор `jenkaby`, 2026  |
| 1.2 | Добавить секцию License в `README.md`| Complete | 2026-03-02 | Ссылка на LICENSE в конце README            |

## Progress Log

### 2026-03-02

- Проведён анализ лицензий всех зависимостей — все permissive, проприетарная лицензия совместима
- Выбрана лицензия All Rights Reserved как наиболее простой вариант без заголовков в исходниках
- Создан файл `LICENSE` в корне проекта (Copyright (c) 2026 jenkaby)
- Добавлена секция `## License` в `README.md`
- Задача завершена

