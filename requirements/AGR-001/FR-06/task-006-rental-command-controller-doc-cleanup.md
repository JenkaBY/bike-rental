<task_file_template>

# Task 006: Clean up stale activation docs/comments on RentalCommandController

> **Applied Skill:** `java-style.md` — zero inline comments; since both touched methods are being edited anyway, the
> two stray inline comments in the deprecated PATCH method are removed in the same change.
> `.claude/rules/hexagonal-boundaries.md` N/A (no boundary change, doc-only). Depends on Task 005 (so `/status`
> wording changes match the enum that now only allows `DRAFT`, `AWAITING_SIGNATURE`, `CANCELLED`).

## 1. Objective

Update two pieces of stale/misleading documentation on `RentalCommandController` that both currently claim the
deprecated JSON-Patch endpoint and/or the lifecycle endpoint support activating a rental via `status=ACTIVE`, which
is no longer true (fr.md §2, design.md §4). Also remove two stray inline comments in the deprecated PATCH method
body while it is being touched (`.claude/rules/java-style.md` — zero inline comments).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no import changes.

### Edit A — deprecated JSON-Patch endpoint javadoc + `@Operation` description

* **Location:** The javadoc block directly above `@PatchMapping(value = "/{id}")`, and the `@Operation` annotation
  immediately below it.

Replace this exact block:

```java
    /**
     * Draft Path: Updates rental using JSON Patch (RFC 6902).
     * Supports partial updates and rental activation.
     * <p>
     * Examples:
     * - Select customer: [{"op": "replace", "path": "/customerId", "value": "uuid"}]
     * - Select equipment: [{"op": "replace", "path": "/equipmentIds", "value": [123,125]}]
     * - Set duration: [{"op": "replace", "path": "/duration", "value": "120"}]
     * Note: startedAt is set automatically when rental is activated
     * - Combined update: [
     * {"op": "replace", "path": "/customerId", "value": "uuid"},
     * {"op": "replace", "path": "/equipmentIds", "value": [123,125]}
     * ]
     *
     * @param id      rental ID
     * @param request validated JSON Patch request
     * @return updated rental
     */
    @PatchMapping(value = "/{id}")
    @Operation(
            hidden = true,
            summary = "Update rental via JSON Patch (RFC 6902)",
            description = "Applies partial updates to a rental. Supported paths: /customerId, /equipmentIds, /duration, /status. Setting status=ACTIVE activates the rental.")
```

with:

```java
    /**
     * Draft Path: Updates rental using JSON Patch (RFC 6902).
     * Supports partial updates only.
     * <p>
     * Examples:
     * - Select customer: [{"op": "replace", "path": "/customerId", "value": "uuid"}]
     * - Select equipment: [{"op": "replace", "path": "/equipmentIds", "value": [123,125]}]
     * - Set duration: [{"op": "replace", "path": "/duration", "value": "120"}]
     * - Combined update: [
     * {"op": "replace", "path": "/customerId", "value": "uuid"},
     * {"op": "replace", "path": "/equipmentIds", "value": [123,125]}
     * ]
     *
     * @param id      rental ID
     * @param request validated JSON Patch request
     * @return updated rental
     */
    @PatchMapping(value = "/{id}")
    @Operation(
            hidden = true,
            summary = "Update rental via JSON Patch (RFC 6902)",
            description = "Applies partial updates to a rental. Supported paths: /customerId, /equipmentIds, /duration.")
```

### Edit B — remove the two stray inline comments in the deprecated PATCH method body

* **Location:** Inside `updateRental(Long id, RentalUpdateJsonPatchRequest request)` (the deprecated one, right below
  Edit A), and the `//    TODO remove` line directly above its `@Deprecated(forRemoval = true)` annotation.

Replace this exact block:

```java
    })
//    TODO remove
    @Deprecated(forRemoval = true)
    public ResponseEntity<RentalResponse> updateRental(
            @Parameter(description = "Rental ID", example = "1") @PathVariable(name = "id") @Positive Long id,
            @Valid @RequestBody RentalUpdateJsonPatchRequest request) {
        log.info("[PATCH] Updating rental {} with {} patch operations", id, request.getOperations().size());

        // Convert validated RentalUpdateJsonPatchRequest to Map for use case layer
        Map<String, Object> patch = commandMapper.toPatchMap(request);
```

with:

```java
    })
    @Deprecated(forRemoval = true)
    public ResponseEntity<RentalResponse> updateRental(
            @Parameter(description = "Rental ID", example = "1") @PathVariable(name = "id") @Positive Long id,
            @Valid @RequestBody RentalUpdateJsonPatchRequest request) {
        log.info("[PATCH] Updating rental {} with {} patch operations", id, request.getOperations().size());

        Map<String, Object> patch = commandMapper.toPatchMap(request);
```

Keep the `@Deprecated(forRemoval = true)` annotation itself — only the comment line above it is removed.

### Edit C — lifecycle endpoint `@Operation` description

* **Location:** The `@Operation` annotation above `updateLifecycle(...)` (the `PATCH /{rentalId}/lifecycles`
  endpoint).

Replace this exact line:

```java
    @Operation(summary = "Transition rental lifecycle status",
            description = "Transitions a rental to AWAITING_SIGNATURE, DRAFT, ACTIVE or CANCELLED status. The response carries the rental version used as the signing fencing token.")
```

with:

```java
    @Operation(summary = "Transition rental lifecycle status",
            description = "Transitions a rental to AWAITING_SIGNATURE, DRAFT or CANCELLED status. The response carries the rental version used as the signing fencing token.")
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
