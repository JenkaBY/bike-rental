/**
 * Finance module (finance)
 *
 * <p>Purpose:
 * <ul>
 *   <li>Record and manage payments related to rentals and direct sales.</li>
 *   <li>Provide a clean, well-tested API for recording payments, querying payment history
 *       and publishing payment events for other modules to consume.</li>
 * </ul>
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Domain: immutable Payment aggregate (id, rentalId, amount, paymentType, paymentMethod,
 *       createdAt, operatorId, receiptNumber).</li>
 *   <li>Application: use-cases for recording and querying payments (RecordPayment, GetPaymentById,
 *       GetPaymentsByRentalId).</li>
 *   <li>Infrastructure: JPA adapters, MapStruct mappers and Liquibase migrations for the
 *       `payments` table.</li>
 *   <li>Web: command and query controllers under web.command and web.query packages.
 *       Controllers validate requests and return RESTful responses.</li>
 *   <li>Events: publish domain events such as {@code PaymentReceived} after successful persistence.
 * </li>
 * </ul>
 *
 * <p>Architecture & patterns:
 * <ul>
 *   <li>Hexagonal / Ports & Adapters: interfaces (use-cases, repositories) are defined in the
 *       application/domain layers; adapters live in infrastructure.</li>
 *   <li>Domain-Driven Design: Payment is an aggregate root kept framework-free.</li>
 *   <li>CQRS: separate command controllers (state changes) from query controllers (read operations).</li>
 *   <li>TDD: tests are added first for domain and service layers, followed by WebMvc and component tests.</li>
 * </ul>
 *
 * <p>References:
 * <ul>
 *   <li>Memory bank task: memory-bank/tasks/US-FN-001-payment-acceptance.md</li>
 *   <li>Shared Money value object: com.github.jenkaby.bikerental.shared.domain.model.vo.Money</li>
 *   <li>Typical patterns: see docs/backend-architecture.md</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 *   <li>The current implementation assumes a single row per recorded payment (immutable record).
 *   </li>
 *   <li>Versioning of payments is not required; refunds are recorded as separate payment records
 *       (handled in a separate story if needed).</li>
 * </ul>
 *
 * @since 2026-02-03
 */
@org.springframework.modulith.ApplicationModule(displayName = "Finance module")
package com.github.jenkaby.bikerental.finance;