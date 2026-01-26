# Project Brief

<!-- 
Foundation document that shapes all other files.
Defines core requirements and goals.
Source of truth for project scope.

This file should contain:
- Project name and overview
- Core business objectives
- Key stakeholders
- High-level requirements
- Success criteria
- Project constraints and boundaries
-->

## Project Name

**BikeRental** - Equipment Rental Management System

## Overview

BikeRental is a comprehensive digital system for managing the rental of equipment (bicycles, scooters, and similar
equipment) designed to replace paper-based manual processes with an automated, efficient solution. The system is built
as a modular monolithic application using Spring Modulith, providing strict module boundaries, event-driven
communication, and the ability to refactor into microservices if needed.

**Target Users:** Equipment rental service operating from a physical location in a park with high foot traffic, serving
walk-in customers with a regularly maintained and updated equipment fleet.

**Current Situation:** The service currently operates primarily offline with minimal digital infrastructure, using
paper-based accounting and manual processes for rental tracking, booking, and customer interactions.

**Solution Approach:** A backend-focused system that automates rental operations, customer management, financial
transactions, equipment lifecycle management, and provides comprehensive reporting and analytics capabilities.

## Core Objectives

1. **Automate Rental Operations**
    - Eliminate paper-based processes and manual data entry
    - Implement digital rental workflow from customer registration to equipment return
    - Enable quick customer identification via partial phone number search (last 4 digits)
    - Automate cost calculations with business rules (5-minute increments, forgiveness threshold, overtime charges)

2. **Improve Financial Accuracy**
    - Eliminate errors in financial transactions through automated calculations
    - Provide transparent financial analytics and reporting
    - Implement cash register management for operator accountability
    - Enable financial reconciliation for accounting compliance

3. **Optimize Equipment Management**
    - Track equipment status in real-time (available, rented, maintenance, decommissioned)
    - Monitor equipment usage hours and rental counts
    - Schedule preventive maintenance based on usage data
    - Identify underutilized equipment for fleet optimization

4. **Enhance Customer Experience**
    - Simplify rental process with quick customer lookup and creation
    - Implement automatic tariff selection based on equipment type and duration
    - Support equipment replacement within 10-minute window with full refund
    - Enable NFC tag scanning for quick equipment identification during returns

5. **Enable Data-Driven Decisions**
    - Provide comprehensive reporting on revenue, equipment utilization, and customer analytics
    - Implement real-time operator dashboard for operational oversight
    - Generate customer statistics and loyalty segmentation
    - Support export capabilities for external analysis (Excel/PDF)

## Key Stakeholders

### Primary Stakeholders

- **Business Owners** - Service profitability, competitive positioning, business growth
- **Rental Operators** - Daily operational efficiency, ease of use, customer service quality
- **Customers** - Rental convenience, transparent pricing, service reliability

### Secondary Stakeholders

- **Technical Personnel** - Equipment maintenance scheduling, issue tracking, workload management
- **Accounting/Finance** - Financial accuracy, reporting compliance, reconciliation processes
- **System Administrators** - User management, security, system configuration, audit oversight

### Roles in System

- **Operator** - Creates/manages rentals, processes returns, handles payments
- **Technician** - Records maintenance, updates equipment status, responds to technical issues
- **Accountant** - Reviews financial reports, reconciles cash registers, exports data
- **Administrator** - Manages users/roles, configures business rules and tariffs, reviews audit logs

## High-Level Requirements

### Functional Requirements

**Customer Management**

- Partial phone number search (4 digits)
- Quick customer creation with minimal data
- Full customer profile management
- Rental history and statistics per customer
- Loyalty status calculation (new/regular/loyal)

**Equipment Management**

- Equipment catalog with types, models, serial numbers, NFC UIDs
- Real-time status tracking (available/rented/maintenance/decommissioned)
- Serial number and NFC-based equipment lookup
- Usage hour accumulation and rental count tracking
- Maintenance scheduling and notification system

**Rental Process**

- Draft rental creation with step-by-step workflow
- Customer, equipment, and tariff selection
- Automatic tariff matching based on equipment type and duration
- Prepayment recording with receipt generation
- Rental activation with status changes
- Equipment return with NFC scanning
- Duration calculation with 5-minute rounding
- Cost calculation with forgiveness and overtime rules
- Early return/cancellation with refund within 10-minute window

**Financial Management**

- Payment acceptance (cash, card) with receipt generation
- Refund processing with reason tracking
- Cash register management per operator shift
- Rental financial history with transaction details
- Financial reconciliation reports

**Tariff & Pricing**

- Tariff catalog by equipment type and rental period (1h, 2h, day, week)
- Base price and extra time pricing (per 5-minute intervals)
- Automatic tariff selection during rental creation
- Forgiveness rule (up to 7 minutes free)
- Overtime calculation with special rounding (8-10 min → 10 min)
- Configurable business rules (increment, forgiveness threshold, rounding)

**Reporting & Analytics**

- Revenue reports by period, equipment type, tariff
- Equipment utilization reports with idle time analysis
- Customer analytics with top customers and segmentation
- Financial reconciliation for accounting
- Real-time operator dashboard with KPIs

**Administration**

- User management with role-based access control
- Permission management (operator/technician/accountant/admin)
- System settings configuration
- Backup and restore capabilities
- Comprehensive audit logging

### Non-Functional Requirements

**Performance**

- Equipment search response time < 1 second
- Rental return calculation < 2 seconds
- Dashboard refresh every 30 seconds
- Support 50+ concurrent rentals

**Security**

- Role-based access control (RBAC)
- JWT-based authentication
- OAuth2 support (Google Sign-In)
- Audit trail for all significant actions
- Secure password storage (bcrypt)

**Reliability**

- 99% uptime during business hours
- Automated database backups
- Transaction integrity for financial operations
- Event-driven architecture for loose coupling

**Usability**

- Intuitive operator interface for quick rental processing
- Mobile-friendly for NFC scanning
- Minimal clicks to complete common operations
- Clear error messages and validation

**Scalability**

- Modular architecture for easy feature addition
- Spring Modulith for potential microservices migration
- Efficient database queries with proper indexing
- Async processing for non-critical operations (audit logs, notifications)

## Success Criteria

The project will be considered successful when:

1. **Operational Efficiency**
    - Rental creation time reduced by 50% compared to manual process
    - Zero financial calculation errors in automated processes
    - Equipment return processing time < 2 minutes including cost calculation

2. **Financial Accuracy**
    - 100% accuracy in rental cost calculations
    - Complete audit trail for all financial transactions
    - Cash register discrepancies < 1% of daily revenue

3. **Equipment Optimization**
    - Real-time visibility of equipment status and location
    - 95% accuracy in maintenance scheduling predictions
    - Ability to identify underutilized equipment for fleet optimization

4. **Customer Satisfaction**
    - Rental process completion time < 5 minutes for returning customers
    - Zero disputes due to incorrect cost calculations
    - Support for 10-minute cancellation window with full refund

5. **Business Intelligence**
    - Daily revenue reports available in real-time
    - Equipment utilization reports for data-driven fleet decisions
    - Customer segmentation for targeted marketing
    - Complete financial reconciliation capability for accounting

6. **System Quality**
    - 99% system uptime during business hours
    - < 1 second response time for customer/equipment search
    - Complete test coverage for critical financial calculations
    - Zero data loss with automated backup strategy

## Constraints

### Technical Constraints

1. **Architecture**
    - Modular monolithic architecture using Spring Modulith
    - Single deployable JAR for simplified operations
    - PostgreSQL as primary database
    - Event-driven communication between modules

2. **Technology Stack**
    - Java 17+, Spring Boot 4
    - Spring Security with JWT/OAuth2
    - Apache POI for Excel, iText/OpenPDF for PDF exports
    - Docker Compose for local development

3. **Development Approach**
    - Test-Driven Development (TDD) methodology
    - Component tests for positive scenarios only
    - Unit tests for business logic validation
    - WebMvc tests for request validation

4. **Integration Points**
    - Web NFC API or QR code support for equipment scanning
    - Limited to backend implementation (frontend not in scope)
    - No real-time chat or notification infrastructure (email/SMS via external services)

### Business Constraints

1. **Operational**
    - Single physical location (no multi-location support in v1)
    - Walk-in customers only (no online booking in v1)
    - Operator-driven workflow (customers don't interact with system directly)
    - Russian language only (no internationalization in v1)

2. **Financial**
    - Cash and card payments only (no electronic wallets initially)
    - Single currency (RUB)
    - Manual 1C export (no real-time integration in v1)

3. **User Management**
    - Simple role hierarchy: Operator < Technician < Accountant < Admin
    - No customer self-service portal
    - No multi-tenancy support

### Resource Constraints

1. **Team & Timeline**
    - Incremental delivery across 8 phases
    - Phase 1 (Foundation) prioritized for core operations
    - Low-priority features deferred to later phases

2. **Infrastructure**
    - Minimal infrastructure requirements (single server deployment)
    - Docker-based deployment for portability
    - Standard PostgreSQL instance (no special clusters)

3. **Data Retention**
    - Audit logs: 1 year minimum
    - Financial records: Indefinite (compliance requirement)
    - Rental history: Indefinite
    - Soft deletes for critical data (no physical deletions)

### Scope Boundaries

**In Scope:**

- Complete rental lifecycle automation
- Financial transaction management
- Equipment lifecycle tracking
- Reporting and analytics
- User and security management

**Out of Scope (v1):**

- Customer-facing mobile app
- Online booking/reservation system
- Real-time notifications (push notifications)
- Multi-location support
- Advanced CRM features
- Loyalty program automation
- Integration with payment gateways (direct card processing)
- Real-time 1C integration

**Future Considerations:**

- Microservices migration if scaling required
- Customer self-service portal
- Online booking system
- Advanced analytics and ML-based predictions
- Mobile app for operators
- Integration with payment processors
- IoT integration for smart locks
