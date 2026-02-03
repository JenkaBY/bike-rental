# [US-TR-006] - Tariff Versioning

**Status:** Pending  
**Added:** 2026-02-03  
**Priority:** Medium  
**Module:** tariff  
**Dependencies:** US-TR-001

## Goal

Introduce an explicit versioning strategy for tariffs so that every change to a tariff preserves the previous state for
audit and billing traceability. This user story extracts the previously deferred subtask from US-TR-001.

## Acceptance Criteria

- The system stores tariff versions and exposes endpoints to read the current version, fetch a specific version, and
  list history.
- Updates and status changes (activate/deactivate) create a new tariff row (immutable versioning) while preserving
  previous rows.
- There is a stable business identifier (`root_id`) that groups versions.
- The `GET /api/tariffs` and `GET /api/tariffs/active` default to returning `is_current = true` rows.
- Existing behaviour for clients that use `id` continues to work (compatibility), but new APIs will expose `root_id`
  where appropriate.

## Implementation Plan (high level)

1. Add DB migration: add `root_id BIGINT`, `version BIGINT DEFAULT 1`, `is_current BOOLEAN DEFAULT true`.
2. Backfill `root_id = id` for existing rows and leave version=1.
3. Modify Create service to set `root_id` (either after insert using generated id or by generating a stable UUID/biz id
   before insert).
4. Modify Update/Activate/Deactivate services: read current row by `root_id` and `is_current = true`, insert new row
   with `version = prev.version + 1` and `is_current = true`, update prev row `is_current = false` in the same
   transaction.
5. Add repository methods: findByRootIdAndIsCurrentTrue, findByRootIdAndVersion, findHistoryByRootId.
6. Add Web endpoints for versioning: `GET /api/tariffs/{rootId}/history`,
   `GET /api/tariffs/{rootId}/versions/{version}`.
7. Add unit, WebMvc and component tests validating the versioning behavior.

## Notes

- This story should be implemented after US-TR-001 is marked complete and will be a non-breaking enhancement if `id`
  continues to be supported for read operations.
- An alternative approach (audit/history table) is possible but the chosen approach keeps tariff rows directly queryable
  for the current version and history.

## Estimation

- Dev: 2-3 days
- Tests: 1-2 days
- Migrations and coordination: 0.5 day


