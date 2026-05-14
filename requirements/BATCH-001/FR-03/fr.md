# User Story: FR-03 — Backward Compatibility of Existing Single-Fetch Endpoints

## 1. Description

**As a** consumer of the existing equipment and customer APIs  
**I want to** be guaranteed that introducing the batch endpoints does not alter the behaviour of any existing endpoint  
**So that** no currently working client integration breaks after the batch endpoints are deployed

## 2. Context & Business Rules

* **Trigger:** The batch endpoints described in FR-01 and FR-02 are deployed to any environment.
* **Rules Enforced:**
    * The following endpoints must retain their existing request signature, validation logic, response schema, and
      HTTP status codes without any change:
        * `GET /api/equipments/{id}` — returns a single equipment record by numeric ID
        * `GET /api/equipments` — returns a paginated, filterable equipment list (including the `q`, `status`, and
          `type` parameters)
        * `GET /api/equipments/by-uid/{uid}` — returns equipment by UID string
        * `GET /api/equipments/by-serial/{serialNumber}` — returns equipment by serial number
        * `GET /api/customers/{id}` — returns a single customer record by UUID
        * `GET /api/customers` — returns up to 10 customers optionally filtered by the `phone` query parameter
    * None of the above endpoints may accept or react to an `ids` query parameter as a result of this change.
    * The OpenAPI documentation entries for the above endpoints must remain unchanged.

## 3. Non-Functional Requirements (NFRs)

* **Performance:** No regression in response time for the existing endpoints.
* **Security/Compliance:** No change to the validation, authentication, or authorisation behaviour of the existing
  endpoints.
* **Usability/Other:** N/A

## 4. Acceptance Criteria (BDD)

**Scenario 1: Existing equipment single-fetch continues to work**

* **Given** an equipment record with ID `1` exists
* **When** `GET /api/equipments/1` is called after the batch endpoint is deployed
* **Then** the response is `200 OK` with the same equipment object structure as before the deployment

**Scenario 2: Existing equipment list search continues to work**

* **Given** multiple equipment records exist
* **When** `GET /api/equipments?status=AVAILABLE&page=0&size=10` is called after the batch endpoint is deployed
* **Then** the response is `200 OK` with a paginated result, identical in structure to the pre-deployment response

**Scenario 3: Existing customer single-fetch continues to work**

* **Given** a customer record with UUID `aaaaaaaa-0000-0000-0000-000000000001` exists
* **When** `GET /api/customers/aaaaaaaa-0000-0000-0000-000000000001` is called after the batch endpoint is deployed
* **Then** the response is `200 OK` with the same customer object structure as before the deployment

**Scenario 4: Existing customer phone-search continues to work**

* **Given** customers with phone numbers containing `9161` exist
* **When** `GET /api/customers?phone=9161` is called after the batch endpoint is deployed
* **Then** the response is `200 OK` with a list of matching customers, identical in structure to the pre-deployment
  response

**Scenario 5: Existing endpoints ignore an unexpected `ids` parameter**

* **Given** any of the existing single-fetch or list endpoints listed above
* **When** the endpoint is called with an `ids` query parameter appended (e.g., `GET /api/equipments/1?ids=2,3`)
* **Then** the response behaves as if `ids` was not provided; no error is raised due to the extra parameter

## 5. Out of Scope

* Changes to command (POST, PUT, PATCH) endpoints in either the equipment or customer module.
* The tariff, rental, and finance modules.
* Regression testing of the batch endpoints themselves (covered by FR-01 and FR-02).
