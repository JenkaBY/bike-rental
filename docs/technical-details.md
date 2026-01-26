# Technical Details

## Overview

BikeRental is a modular monolithic system for managing the rental of equipment (bicycles, scooters), automating rental
operations, client management, financial transactions, and maintenance. The architecture is based on Spring Modulith,
providing strict module boundaries, event-driven communication, and the ability to refactor into microservices if
needed. The system is designed for operational efficiency, quality of service, and minimal infrastructure requirements.

## Technology Stack

- **Backend:** Java, Spring Boot 4, Spring Modulith
- **Security:** Spring Security, OAuth2 (Google Sign-In), JWT
- **Database:** PostgreSQL
- **Build/CI:** Gradle, GitHub Actions
- **Containerization:** Docker, Docker Compose
- **Reporting/Export:** Apache POI (Excel), iText/OpenPDF (PDF)
- **Frontend:** (Not covered in this document, but supports Web NFC API/QR for equipment identification)

## Core Modules

- **Customer:** Client profiles, search, and statistics
- **Equipment:** Equipment catalog, status management
- **Tariff:** Tariff directory, cost calculation
- **Rental:** Rental lifecycle management (creation, start, return, cancel)
- **Finance:** Payments, cash register, financial events
- **Reporting:** Analytics and reports (income, utilization, customer analytics)
- **Maintenance:** Maintenance records and schedules
- **Admin:** User management, system settings, audit log

## DB & DB Schema

- **Main Entities:**
  - Customer (id, phone, name, email, birth_date, created_at)
    - Equipment (id, serial_number, nfc_uid, type, status, commissioned_at, total_usage_hours)
    - EquipmentType (id, name, description)
    - Tariff (id, equipment_type_id, period, base_price, extra_time_price_per_5min, valid_from, valid_to, is_active)
    - Rental (id, customer_id, equipment_id, tariff_id, status, started_at, expected_return_at, actual_return_at,
      planned_minutes, actual_minutes)
    - Payment (id, rental_id, payment_type, payment_method, amount, created_at)
    - MaintenanceRecord (id, equipment_id, maintenance_type, description, started_at, completed_at)
    - AppUser (id, username, password_hash, role, is_active)
    - SystemSettings (key, value, updated_at)
    - AuditLog (id, user_id, event_type, entity_type, entity_id, changes, created_at)
- **Relationships:**
    - Customer has many Rentals
    - Equipment has many Rentals and MaintenanceRecords
    - Equipment belongs to EquipmentType
    - Tariff applies to EquipmentType
    - Rental uses Tariff and has many Payments
    - AppUser performs AuditLog actions

## Development Workflow

- TDD/BDD approach with integration tests
- Modular monolith with strict boundaries using Spring Modulith
- Event-driven communication between modules (Spring Application Events)
- REST API with role-based access (Operator/Admin)
- API versioning via Content-Type negotiation (e.g., application/vnd.bikerent.v1+json)
- CI/CD with GitHub Actions, single fat JAR deployment
- Local development with Docker Compose and PostgreSQL
- Automated modularity tests (Spring Modulith)

## Future Considerations

- Easy refactoring to microservices if scaling is required
- Integration of advanced analytics and reporting
- Expansion of NFC/QR and mobile features
- Enhanced security authentication
- Support for additional payment methods and integrations
- Internationalization and multi-language support