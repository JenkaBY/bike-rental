---
name: spring-boot-modulith
description: Spring Modulith 2.0 implementation for bounded contexts in Spring Boot 4. Use when structuring application modules, implementing @ApplicationModuleListener for event-driven communication, testing with Scenario API, enforcing module boundaries, or externalizing events to Kafka/AMQP. For modular monolith architecture decisions, see the domain-driven-design skill.
---

# Spring Modulith for Bounded Contexts

Implements DDD bounded contexts as application modules with enforced boundaries and event-driven communication.

## Core Concepts

| Concept                | Description                              |
|------------------------|------------------------------------------|
| **Application Module** | Package-based boundary = bounded context |
| **Module API**         | Types in base package (public)           |
| **Internal**           | Types in sub-packages (encapsulated)     |
| **Events**             | Cross-module communication mechanism     |

## Module Structure

```
src/main/java/
├── com.example/
│   └── Application.java              ← @SpringBootApplication
├── com.example.order/                ← Module: order
│   ├── OrderService.java             ← Public API
│   ├── OrderCreated.java             ← Public event
│   ├── package-info.java             ← @ApplicationModule config
│   └── internal/                     ← Encapsulated
│       ├── OrderRepository.java
│       └── OrderEntity.java
├── com.example.inventory/            ← Module: inventory
│   ├── InventoryService.java
│   └── internal/
└── com.example.shipping/             ← Module: shipping
```

Types in `com.example.order` = public API
Types in `com.example.order.internal` = hidden from other modules

## Quick Patterns

See [EXAMPLES.md](EXAMPLES.md) for complete working examples including:

- **Module Configuration** with @ApplicationModule
- **Event Publishing** with domain event records
- **Event Handling** with @ApplicationModuleListener (Java + Kotlin)
- **Module Verification Test** with PlantUML generation
- **Event Externalization** for Kafka/AMQP

## Spring Boot 4 / Modulith 2.0 Specifics

- **@ApplicationModuleListener** combines `@Async` + `@Transactional(REQUIRES_NEW)` +
  `@TransactionalEventListener(AFTER_COMMIT)`
- **Event Externalization** with `@Externalized` annotation for Kafka/AMQP
- **JDBC event log** ensures at-least-once delivery

## Detailed References

- **Examples**: See [EXAMPLES.md](EXAMPLES.md) for complete working code examples
- **Troubleshooting**: See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues and Boot 4 migration
- **Module Structure**: See [references/module-structure.md](references/module-structure.md) for package conventions,
  named interfaces, dependency rules
- **Event Patterns**: See [references/events.md](references/events.md) for publishing, handling, externalization,
  testing with Scenario API

## Anti-Pattern Checklist

| Anti-Pattern                         | Fix                                               |
|--------------------------------------|---------------------------------------------------|
| Direct bean injection across modules | Use events or expose API                          |
| Synchronous cross-module calls       | Use `@ApplicationModuleListener`                  |
| Module dependencies not declared     | Add `allowedDependencies` in `@ApplicationModule` |
| Missing verification test            | Add `ApplicationModules.verify()` test            |
| Internal types in public API         | Move to `.internal` sub-package                   |
| Events without data                  | Include all data handlers need                    |

## Critical Reminders

1. **One module = one bounded context** — Mirror DDD boundaries
2. **Events are the integration mechanism** — Not direct method calls
3. **Verify in CI** — `ApplicationModules.verify()` catches boundary violations
4. **Reference by ID** — Never direct object references across modules
5. **Transaction per module** — `@ApplicationModuleListener` ensures isolation
