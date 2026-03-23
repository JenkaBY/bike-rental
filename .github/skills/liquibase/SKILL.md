---
name: liquibase
description: Create and manage database schema changes using Liquibase. Use when asked to create tables, modify schemas, add indexes, create constraints, manage database migrations, provision table with initial data, or version database changes.
---

# Liquibase Database Migration Skill

Manage database schema changes using Liquibase with XML-based changelogs in a Spring Boot application.

## When to Use This Skill

- Creating new database tables
- Modifying existing table structures (add/drop columns, change types)
- Creating indexes for performance optimization
- Adding or modifying constraints (primary keys, foreign keys, unique, not null and others)
- provisioning initial data (inserts/updates)
- Managing database migrations and versioning
- Rollback database changes
- Generating database documentation
- Viewing or analyzing migration history

## Prerequisites

- Spring Boot application with `spring-boot-starter-liquibase` dependency or liquibase gradle/maven plugin
- Liquibase configured in `application.yaml` with:
    - `spring.liquibase.change-log: classpath:/db/changelog/db.changelog-master.xml`

## Project Structure

```
service/src/main/resources/db/changelog/
├── db.changelog-master.xml                  # Master changelog file
├── data/                                    # Data to be provisioned changelogs
│    ├── data/                               # csv files for initial data
│    │  └── {tableName}.csv                  # Individual table records in CSV format
│    └── {tableName}-provisoning.xml         # Individual changelog files for provisioning
└── v1/                                      # Version-specific changelogs
    └── customers.create-table.xml                # Individual changelog files
```

## Step-by-Step Workflows

### 1. Create a New Table

**Step 1**: Create a new XML file in the appropriate version folder (e.g., `v1/`)

- liquibase changelog id naming conventions: {create|update}-table.{table-name}-{short-description}
  File naming convention: `{table}.{action:create|update|delete-table}_{specific-action}.xml`
- Examples: `rentals.create.xml`, `rentals.update-table_add-column-status.xml`

**Step 2**: Add the changeset with proper structure:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="{table}.{action:create|update|delete-table}_{specific-action}" author="{author_name}">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="{table_name}"/>
            </not>
        </preConditions>
        
        <createTable tableName="{table_name}">
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <!-- Example: Time columns MUST use TIMESTAMP WITH TIME ZONE -->
            <column name="created_at" type="TIMESTAMP WITH TIME ZONE">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP WITH TIME ZONE"/>
            <!-- Additional columns -->
        </createTable>

        <!-- Optional: Add indexes -->
        <createIndex tableName="{table_name}" indexName="idx_{table_name}_{column}">
            <column name="{column}"/>
        </createIndex>
    </changeSet>

</databaseChangeLog>
```

**Step 3**: Include the new changelog in `db.changelog-master.xml`:

```xml

<include relativeToChangelogFile="true" file="v1/{table}.{action:create|update|delete-table}_{specific-action}.xml"/>
```

**Step 4**: Run the application or tests to apply migrations

### 2. Add Columns to Existing Table

**Step 1**: Create a new changeset file (e.g., `v1/{table}.update-table_add-column-{column}.xml`)

**Step 2**: Define the column addition:

```xml

<changeSet id="{table}.update-table_add-column-{column}" author="{author_name}">
    <preConditions onFail="MARK_RAN">
        <not>
            <tableExists tableName="{table_name}"/>
        </not>
    </preConditions>
    
    <addColumn tableName="{table_name}">
        <column name="{column_name}" type="{data_type}">
            <constraints nullable="{true|false}"/>
        </column>
    </addColumn>
</changeSet>
```

**Step 3**: Include in master changelog and apply

### 3. Create Foreign Key Relationship

```xml

<changeSet id="{source_table}.update-table_add_fk_{target_table}" author="{author_name}">
    <preConditions onFail="MARK_RAN">
        <not>
            <tableExists tableName="{table_name}"/>
        </not>
    </preConditions>
    <addForeignKeyConstraint
            baseTableName="{source_table}"
            baseColumnNames="{source_column}"
            constraintName="fk_{source_table}_{target_table}"
            referencedTableName="{target_table}"
            referencedColumnNames="{target_column}"
            onDelete="CASCADE"
            onUpdate="CASCADE"/>
</changeSet>
```

### 4. Create Index for Performance

```xml

<changeSet id="{table_name}.update-table_create-idx-{column_name}" author="{author_name}">
    <preConditions onFail="MARK_RAN">
        <not>
            <tableExists tableName="{table_name}"/>
        </not>
    </preConditions>
    <createIndex tableName="{table_name}" indexName="idx_{table_name}_{column}">
        <column name="{column}"/>
    </createIndex>
</changeSet>
```

### 4. Preconditions Example

```xml

<changeSet author="copilot_agent" id="message_logs.create-table">
    <preConditions onFail="MARK_RAN">
        <not>
            <tableExists tableName="{table_name}"/>
        </not>
    </preConditions>
    <createTable tableName="{table_name}">
        <!--  omitted for brevity-->
    </createTable>
</changeSet>
```

## Naming Conventions

| Element           | Convention                                     | Example                                        |
|-------------------|------------------------------------------------|------------------------------------------------|
| ChangeSet ID      | `{table}.{action}-table_{specific-action}`     | `customers.create-table`                       |
| Table Name        | `{plural_noun}`                                | `customers`, `rentals`                         |
| Column Name       | `{snake_case}`                                 | `first_name`, `created_at`                     |
| Index Name        | `idx_{table}_{column}`                         | `idx_customers_phone`                          |
| Foreign Key       | `fk_{source}_{target}`                         | `fk_rentals_customers`                         |
| Unique Constraint | `uq_{table}_{column}`                          | `uq_customers_email`                           |
| File Name         | `{table}.{action}-table_{specific-action}.xml` | `customers.update-table_add-status-column.xml` |

## Best Practices

1. **One Logical Change Per File**: Keep each changeset file focused on a single logical change
2. **Use preconditions** Check existing structures before applying changes
3. **Unique ChangeSet IDs**: Use descriptive, unique IDs following the pattern
   `{table}.{action}-table_{specific-action}`
4. **Version Folders**: Organize changelogs by version (v1, v2, etc.) for better organization
5. **Sequential Ordering**: Add new includes at the bottom of `db.changelog-master.xml`
6. **Author Attribution**: Use consistent author name (e.g., "copilot", "ai_agent")
7. **Rollback Support**: Consider adding rollback sections for complex changes
8. **Index Creation**: Add indexes in the same changeset as table creation when possible
9. **Constraints**: Define primary keys, unique constraints, and not null constraints during table creation
10. **Time Column Types**: **ALWAYS** use `TIMESTAMP WITH TIME ZONE` for all time-related columns (both audit fields like `created_at`, `updated_at` and business fields like `started_at`, `expected_return_at`). This ensures proper timezone handling and prevents issues with data migration.

## Testing with Liquibase

### Component Tests Configuration

For component tests, Liquibase can be configured in two modes:

**Mode 1: Fresh Schema (Initial Development)**

```yaml
spring:
  liquibase:
    enabled: true
    drop-first: true  # Recreates schema each test run
```

**Mode 2: Persistent Schema (Fast Testing)**

```yaml
spring:
  liquibase:
    enabled: false  # Assumes DB schema already exists
```

**Workflow**:

1. When introducing new DB changes, set `enabled: true` and run tests once
2. After schema is applied, set `enabled: false` to speed up subsequent test runs
3. Only re-enable when new migrations are added

### Unit Tests Configuration

```yaml
spring:
  liquibase:
    enabled: false  # Unit tests don't need real DB migrations
```

## Troubleshooting

| Issue                        | Solution                                                                 |
|------------------------------|--------------------------------------------------------------------------|
| Changeset already executed   | Check `databasechangelog` table; create new changeset with different ID  |
| Foreign key constraint fails | Ensure referenced table exists first in master changelog order           |
| Index already exists         | Check if index was created in previous changeset; use unique index names |
| Liquibase disabled in tests  | Set `spring.liquibase.enabled=true` in test configuration                |
| Migrations not applied       | Verify `db.changelog-master.xml` path in `application.yaml`              |
| Column type mismatch         | Use correct Liquibase type that maps to PostgreSQL type                  |

## References

- [Liquibase Documentation](https://docs.liquibase.com/)
- [Liquibase XML Format](https://docs.liquibase.com/concepts/changelogs/xml-format.html)
- [Spring Boot Liquibase Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [PostgreSQL Data Types](https://www.postgresql.org/docs/current/datatype.html)
