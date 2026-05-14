# Initial User Request — BATCH-001

## Original Request

> The Rental List page on the frontend displays rental cards. Each `Rental` entity contains
> references such as `equipmentIds: [1, 3]` and `customerId: 2`.
>
> Currently, to render each card fully, the frontend must call:
> - `GET /api/equipments/{id}` — once per equipment item
> - `GET /api/customers/{id}` — once per customer
>
> For a list of 20 rentals with multiple equipment items each, this results in N×K individual
> HTTP requests — causing performance degradation and excessive network load.
>
> **Chosen Solution:** Introduce batch fetch endpoints that accept multiple IDs via a query
> parameter `ids`, enabling the frontend to replace N×K individual calls with a fixed number
> of parallel batch requests:
> - `GET /api/equipments/batch?ids=1,2,3`
> - `GET /api/customers/batch?ids=uuid1,uuid2,uuid4`
>
> The existing single-resource endpoints must remain unchanged and fully functional.

## Scope

Backend API only. Frontend, Angular services, and UI components are out of scope.

## Clarifications Captured

| Question                              | Answer                                                               |
|---------------------------------------|----------------------------------------------------------------------|
| Customer ID format in batch parameter | Comma-separated UUIDs — matching existing `GET /api/customers/{id}`  |
| Equipment batch endpoint URL          | New dedicated path `GET /api/equipments/batch?ids=...`               |
| Customer batch endpoint URL           | New dedicated path `GET /api/customers/batch?ids=...`                |
| Response payload per item             | Full resource — same schema as the respective single-fetch endpoints |
| Maximum number of IDs per request     | 100; requests exceeding this limit must be rejected with HTTP 400    |
