# Product Context

<!--
Why this project exists.
Problems it solves.
How it should work.
User experience goals.

This file should contain:
- Problem statement
- Target users
- User workflows
- Feature requirements
- User experience principles
- Business rules
-->

## Problem Statement

### Current Situation

An equipment rental service (bicycles, scooters) operates successfully from a physical location in a park on a bike
path. The service maintains a regularly updated and well-maintained equipment fleet, but currently relies on *
*paper-based accounting and manual processes** for:

- Rental tracking and record-keeping
- Customer information management
- Financial transaction recording
- Equipment status monitoring
- Maintenance scheduling

### Problems This Causes

1. **Financial Errors**: Regular mistakes and discrepancies in rental accounting and cash management
2. **Equipment Tracking Issues**: Inability to accurately track equipment utilization and wear
3. **Customer Loss**: No online booking or convenient digital services compared to competitors
4. **Limited Analytics**: Lack of transparent financial reporting and business insights
5. **Maintenance Challenges**: Difficulty planning technical maintenance based on actual usage data
6. **Operational Inefficiency**: Time-consuming manual processes reduce staff productivity

### Business Impact

Without digitalization, the business faces:

- Reduced profitability due to financial errors and inefficiencies
- Loss of competitive advantage as competitors offer convenient online services
- Inability to implement customer loyalty programs
- Limited capacity to scale operations
- Risk of customer attrition to more modern competitors

### What This System Solves

BikeRental provides a **comprehensive digital management system** that:

- **Eliminates paper-based processes** with automated rental workflows
- **Ensures financial accuracy** through automated calculations and audit trails
- **Enables data-driven decisions** with real-time analytics and reporting
- **Improves customer experience** with quick service and transparent pricing
- **Optimizes equipment utilization** through usage tracking and predictive maintenance
- **Increases competitive positioning** by modernizing operations

## Target Users

### Primary Users

**1. Rental Operators**

- **Who**: Front-line staff managing day-to-day rental operations
- **Needs**:
    - Fast customer lookup (by last 4 digits of phone)
    - Quick rental creation and processing
    - Easy equipment identification (by serial number)
    - Simple return processing with automatic cost calculation
- **Goals**:
    - Serve customers quickly without errors
    - Minimize manual data entry
    - Clear understanding of rental status at all times

**2. Equipment Rental Customers**

- **Who**: Park visitors wanting to rent bikes, scooters, or similar equipment
- **Needs**:
    - Quick rental process
    - Transparent pricing
    - Fair treatment (forgiveness for minor delays)
    - Ability to change equipment if unsatisfied
- **Goals**:
    - Enjoy equipment without complications
    - Understand costs clearly
    - Trust the service

### Secondary Users

**3. Technical Personnel**

- **Who**: Staff responsible for equipment maintenance and repair
- **Needs**:
    - Equipment usage tracking
    - Maintenance scheduling based on actual wear
    - Notification of reported issues
    - Equipment status management
- **Goals**:
    - Keep equipment in good working condition
    - Prevent breakdowns through predictive maintenance
    - Optimize maintenance schedules

**4. Accountants/Finance Staff**

- **Who**: Staff handling financial reporting and reconciliation
- **Needs**:
    - Accurate financial transaction records
    - Cash register reconciliation
    - Financial reports for bookkeeping
    - Export capabilities for accounting systems (1C)
- **Goals**:
    - Maintain accurate financial records
    - Comply with accounting standards
    - Reconcile daily operations

**5. System Administrators**

- **Who**: Staff managing system configuration and users
- **Needs**:
    - User management
    - Tariff configuration
    - Business rules adjustment
    - System monitoring and audit
- **Goals**:
    - Keep system running smoothly
    - Adjust settings as business needs change
    - Ensure security and compliance

### User Personas

**Maria - Rental Operator (Primary Persona)**

- Age: 28
- Experience: 2 years at the rental service
- Tech-savvy: Medium
- Pain Points: Slow customer lookup with paper records, calculation errors, difficulty tracking who has what equipment
- Motivation: Serve customers quickly and accurately to maintain good reputation

**Ivan - Weekend Customer**

- Age: 35
- Frequency: Occasional (5-10 times per season)
- Preference: Quick process, fair pricing, no complications
- Pain Point: Doesn't want disputes over minor delays or incorrect charges

**Sergey - Equipment Technician**

- Age: 42
- Experience: 10 years in bike mechanics
- Tech-savvy: Low
- Pain Point: Doesn't know which equipment needs maintenance until it breaks
- Motivation: Prevent equipment failures, optimize his workload

## User Workflows

### 1. Complete Rental Process (Primary Workflow)

**Actor**: Rental Operator + Customer

**Scenario**: Customer arrives and wants to rent a bicycle for 2 hours

**Steps**:

1. **Customer Search**
    - Operator asks for phone number
    - Searches by last 4 digits (quick partial match)
    - System shows matching customers

2. **Customer Creation** (if new customer)
    - Enter phone number
    - Optionally add name
    - Save with minimal information
    - Can complete profile later

3. **Create Rental Record**
    - Select found/created customer
    - Choose equipment by serial number (easier than scanning)
    - System shows equipment status (must be AVAILABLE)

4. **Tariff Selection**
    - Operator selects rental duration (1 hour, 2 hours, day)
    - System automatically selects appropriate tariff based on equipment type
    - System calculates estimated cost
    - Displays cost to customer

5. **Prepayment**
    - Operator enters payment amount
    - Selects payment method (cash/card)
    - System generates receipt
    - Records payment in financial log

6. **Start Rental**
    - Operator activates rental
    - System sets start time (current time)
    - System changes equipment status to RENTED
    - System calculates expected return time
    - Customer receives equipment

7. **Return Process**
    - Customer returns with equipment
   - Operator scans tag with phone (or enters serial number)
    - System identifies active rental
    - System calculates actual duration
    - System applies business rules:
        - Rounds to 5-minute increments
        - Forgives up to 7 minutes of delay
        - If > 7 minutes late, rounds to 10 minutes minimum
    - System calculates final cost
    - If additional payment needed, operator collects
    - System generates final receipt
    - Equipment status changes to AVAILABLE

**Success Criteria**:

- Complete process in under 5 minutes for returning customers
- Zero calculation errors
- Clear cost breakdown shown at each step

### 2. Early Return or Equipment Exchange

**Actor**: Rental Operator + Dissatisfied Customer

**Scenario**: Customer doesn't like the bicycle and wants to exchange or get refund (within 10 minutes)

**Steps**:

1. Customer returns equipment quickly (within 10 minutes of start)
2. Operator processes return
3. System checks elapsed time
4. If ≤ 10 minutes:
    - **Option A - Full Refund**: System processes complete refund, rental cancelled
    - **Option B - Equipment Exchange**: Transfer payment to new rental, no additional charge
5. If > 10 minutes: Normal return process with regular charges

**Success Criteria**:

- Fair treatment of customers with genuine issues
- Clear 10-minute policy communicated
- Smooth exchange process without operator confusion

### 3. Operator Daily Workflow

**Actor**: Rental Operator

**Morning**:

1. Log into system
2. Review dashboard showing:
    - Available equipment count by type
    - Any equipment in maintenance
    - Previous day's revenue
3. Open cash register shift with opening balance
4. Ready to serve customers

**During Day**:

1. Process rentals continuously
2. Handle returns
3. Occasionally check dashboard for overdue rentals
4. Respond to customer inquiries about pricing

**Evening**:

1. Process remaining returns
2. Close cash register shift
3. Count physical cash
4. Enter actual cash amount
5. System shows expected vs actual (reconciliation)
6. If discrepancy, add note
7. Generate shift report
8. Log out

**Success Criteria**:

- Clear visibility of daily operations
- Simple shift management
- Accurate cash reconciliation

### 4. Maintenance Scheduling (Technical Staff)

**Actor**: Equipment Technician

**Workflow**:

1. Check dashboard for maintenance notifications
2. Review equipment due for scheduled maintenance
3. Review equipment with reported issues
4. Plan maintenance work
5. Take equipment offline (status → MAINTENANCE)
6. Perform maintenance work
7. Record maintenance details in system
8. Return equipment to service (status → AVAILABLE)

**Success Criteria**:

- Proactive maintenance based on usage hours
- Clear notifications for overdue maintenance
- Easy status management

### 5. Financial Reporting (Accountant)

**Actor**: Accountant

**Monthly Workflow**:

1. Log into system
2. Generate reports:
    - Revenue report for the period
    - Financial reconciliation report
    - Cash register summary by operator
3. Review financial transaction details
4. Export to Excel for accounting system import
5. Reconcile with bank statements
6. Archive reports

**Success Criteria**:

- Complete financial data available
- Easy export in standard formats
- Detailed transaction-level audit trail

## Feature Requirements

### Core Features (Must Have)

**Customer Management**

- ✅ Quick search by partial phone number (last 4 digits)
- ✅ Minimal customer creation (phone only initially)
- ✅ Full customer profile with optional details
- ✅ Customer rental history view
- ✅ Customer statistics and loyalty status

**Equipment Management**

- ✅ Equipment catalog with types (bike, scooter, etc.)
- ✅ Equipment identification by serial number
- ✅ Real-time status tracking (available/rented/maintenance/decommissioned)
- ✅ Usage hour accumulation
- ✅ Maintenance scheduling

**Rental Operations**

- ✅ Step-by-step rental creation workflow
- ✅ Automatic tariff selection based on equipment type and duration
- ✅ Prepayment recording with receipt
- ✅ Rental activation with time tracking
- ✅ Automatic cost calculation with business rules
- ✅ 10-minute cancellation window with full refund

**Financial Management**

- ✅ Payment acceptance (cash, card)
- ✅ Refund processing with reason tracking
- ✅ Cash register management per operator
- ✅ Shift opening/closing with reconciliation
- ✅ Financial transaction history per rental

**Tariff & Pricing**

- ✅ Tariff catalog by equipment type
- ✅ Multiple rental periods (1h, 2h, day, week)
- ✅ Automatic tariff selection
- ✅ Base price + overtime pricing
- ✅ Configurable business rules (rounding, forgiveness)

**Reporting**

- ✅ Real-time operator dashboard
- ✅ Revenue reports by period
- ✅ Equipment utilization reports
- ✅ Customer analytics
- ✅ Financial reconciliation for accounting

**Administration**

- ✅ User management with roles (operator/admin)
- ✅ System settings configuration
- ✅ Audit logging
- ✅ Backup and restore

### Important Features (Should Have)

**Enhanced Customer Experience**

- Equipment replacement workflow
- Customer satisfaction tracking
- Rental history with detailed breakdown

**Advanced Equipment Management**

- Predictive maintenance alerts
- Equipment issue reporting during return
- Utilization optimization recommendations

**Financial Features**

- Multiple payment methods support
- Detailed cost breakdown display
- Export to accounting systems (1C)

**Reporting & Analytics**

- Customer segmentation
- Peak time analysis
- Revenue forecasting

### Nice to Have (Future)

**Customer Portal**

- Online booking
- Account self-management
- Loyalty program features

**Mobile Experience**

- Native mobile app for operators
- Faster NFC/QR scanning
- Offline mode support

**Advanced Features**

- Smart equipment locks integration
- Real-time equipment GPS tracking
- Weather-based pricing adjustments
- Integration with payment gateways

## User Experience Goals

### Speed & Efficiency

**Goal**: Minimize time spent on each transaction

- Customer lookup in < 1 second
- Complete rental creation in < 3 minutes for returning customers
- Return processing in < 2 minutes including cost calculation
- Dashboard loads in < 1 second

### Clarity & Transparency

**Goal**: Users always understand what's happening

- Clear step-by-step rental creation process
- Cost breakdown visible at each stage
- Status indicators use colors (green=available, red=overdue)
- Error messages in plain language, not technical jargon

### Error Prevention

**Goal**: Make it hard to make mistakes

- Prevent selecting rented equipment
- Warn before actions that can't be undone
- Validate input before submission
- Guide users through multi-step processes

### Minimal Training Required

**Goal**: Operators can start quickly with minimal training

- Intuitive interface following common patterns
- Most common tasks require 1-2 clicks
- Contextual help available
- Consistent layouts across all screens

### Feedback & Confirmation

**Goal**: Users always know the result of their actions

- Success messages after operations
- Visual confirmation of status changes
- Receipt generation after payments
- Clear error recovery guidance

### Accessibility

**Goal**: System works for users with varying technical skills

- Large touch targets for mobile use
- High contrast for outdoor use
- Simple navigation structure
- Keyboard shortcuts for power users

## Business Rules

### Customer Management

**Rule 1: Partial Phone Search**

- Search works with last 4 digits minimum
- Results show matching customers sorted by recent activity
- Duplicate prevention based on exact phone match

**Rule 2: Minimal Customer Creation**

- Phone number is the only required field
- Customer can be created during rental process
- Full profile can be completed later

### Equipment Management

**Rule 3: Status Transitions**

- Equipment can only be rented if status is AVAILABLE
- Status automatically changes to RENTED when rental starts
- Status automatically returns to AVAILABLE on return
- MAINTENANCE status blocks rental until cleared by technician
- DECOMMISSIONED status is permanent (soft delete)

**Rule 4: Serial Number Uniqueness**

- Each equipment has unique serial number
- Serial number never changes
- UID is optional but must be unique if provided

### Rental Operations

**Rule 5: Rental Status Flow**

- DRAFT → ACTIVE → COMPLETED/CANCELLED
- Cannot delete ACTIVE rentals (must complete or cancel)
- Cannot modify COMPLETED rentals (immutable)

**Rule 6: Prepayment Requirement**

- Rental cannot be activated without prepayment
- Prepayment must be > 0
- Prepayment amount recorded for refund calculations

**Rule 7: Start Time**

- Automatically set to current time when rental activates
- Can be manually adjusted for advance bookings (within 7 days)
- Cannot be changed after return processing starts

### Pricing & Cost Calculation

**Rule 8: Time Rounding**

- All durations rounded to 5-minute increments
- Always round UP (23 minutes = 25 minutes billable)
- Applied to both planned and actual duration

**Rule 9: Forgiveness Threshold**

- Customer forgiven if returned within 7 minutes of expected time
- 1-7 minutes late = no additional charge
- Forgiveness applies even for multiple small delays

**Rule 10: Overtime Calculation**

- If > 7 minutes late, round first delay to 10 minutes
- After 10 minutes, continue with 5-minute increments
- Examples:
    - 8 minutes late → 10 minutes charge
    - 12 minutes late → 15 minutes charge
    - 23 minutes late → 25 minutes charge

**Rule 11: Overtime Rate**

- Use `extraTimePricePer5Min` from selected tariff
- Apply to all overtime minutes (not just excess)
- Calculate: `(overtime_minutes / 5) * price_per_5min`

**Rule 12: Automatic Tariff Selection**

- Match by equipment type AND rental duration
- If multiple tariffs match, select lowest price
- If no exact duration match, select next larger period
- Tariff must be active and valid for rental date

### Cancellation & Refunds

**Rule 13: 10-Minute Cancellation Window**

- Full refund available if rental cancelled within 10 minutes of start
- Window calculated from actual start time (not creation time)
- Both cancellation and equipment exchange allowed

**Rule 14: Refund Processing**

- Refund amount = prepayment - actual usage cost (if any)
- Refund must reference original payment
- Refund reason required for audit trail
- Refund processed in same method as original payment when possible

### Financial Management

**Rule 15: Cash Register Rules**

- One shift per operator at a time
- Opening balance must be recorded
- All cash transactions auto-recorded during shift
- Closing requires physical count entry
- Discrepancy logged but shift can close

**Rule 16: Payment Recording**

- Every payment gets unique receipt number
- Payment amount must be positive
- Payment method must be specified
- All payments linked to rental for audit trail

### Maintenance

**Rule 17: Scheduled Maintenance**

- Equipment requires maintenance every X hours (configurable by type)
- System calculates next maintenance date automatically
- Warning when maintenance due soon (7 days)
- Critical equipment blocked when maintenance overdue

**Rule 18: Usage Tracking**

- Usage hours accumulated after each completed rental
- Rental count incremented on completion
- Data used for maintenance scheduling and analytics

### Security & Audit

**Rule 19: Audit Logging**

- All significant actions logged automatically
- Logs are immutable (cannot be deleted or modified)
- Include: user, timestamp, action, affected entity
- Retained for minimum 1 year

**Rule 20: Role-Based Access**

- OPERATOR: Can perform all operational tasks
- ADMIN: All operations + system administration
- No operations allowed on expired accounts
- Failed login attempts logged

These business rules are enforced in the domain layer and tested thoroughly to ensure consistency and correctness across
the entire system.
