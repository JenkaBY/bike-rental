# [US-AD-006] - Резервное копирование и восстановление (Backup and Restore)

**Status:** Pending  
**Added:** 2026-01-21  
**Updated:** 2026-01-26  
**Priority:** Low  
**Module:** admin  
**Dependencies:** None

## Original Request

**Как** Администратор  
**Я хочу** создавать резервные копии данных и восстанавливать их  
**Чтобы** обеспечить сохранность данных

## User Story Details

**Описание:**  
Система должна поддерживать резервное копирование данных.

**Функции:**

- Ручное создание резервной копии
- Автоматическое резервное копирование (настраивается)
- Восстановление из резервной копии
- Экспорт данных

**Критерии приемки:**

- Создание полной копии БД
- Восстановление с проверкой целостности
- Уведомления об успехе/ошибке
- Хранение копий в безопасном месте

**Связанные требования:** FR-AD-006

## Thought Process

Backup and restore is critical for data safety but has lower priority in initial implementation. Considerations:

1. **PostgreSQL Native Tools**: Use pg_dump/pg_restore for database backups
2. **Scheduling**: Use Spring @Scheduled for automatic backups
3. **Storage**: Store backups in secure location (filesystem or cloud)
4. **Integrity**: Verify backup integrity after creation
5. **Rotation**: Implement backup rotation policy (keep last N backups)

**Architecture Decisions:**

- Use PostgreSQL native tools (pg_dump) via ProcessBuilder
- Schedule backups using Spring @Scheduled
- Store metadata about backups in database
- Implement backup rotation (e.g., keep last 30 days)
- Consider compression for backup files
- Async operations with progress tracking

**Security Considerations:**

- ADMIN role required for backup/restore operations
- Backup files contain sensitive data - encrypt or restrict access
- Database credentials must be securely managed
- Restore operation is destructive - require confirmation

**Implementation Strategy:**

```
BackupService
├── createBackup() - Execute pg_dump
├── scheduleAutoBackup() - @Scheduled task
├── listBackups() - Get available backups
├── restoreBackup() - Execute pg_restore
└── verifyBackupIntegrity() - Validate backup file
```

## Implementation Plan

- [ ] Create Backup domain model (metadata)
- [ ] Implement backup repository for metadata
- [ ] Create BackupService with pg_dump integration
- [ ] Implement restore functionality with pg_restore
- [ ] Add scheduled backup task (@Scheduled)
- [ ] Implement backup rotation policy
- [ ] Create REST endpoints for backup operations
- [ ] Add backup integrity verification
- [ ] Implement progress tracking for long operations
- [ ] Add notification service for backup results
- [ ] Create component tests for backup scenarios
- [ ] Write unit tests for backup logic
- [ ] Document backup/restore procedures
- [ ] Add database migration for backup metadata table

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                | Status      | Updated    | Notes |
|-----|----------------------------|-------------|------------|-------|
| 1.1 | Create domain model        | Not Started | 2026-01-26 |       |
| 1.2 | Implement backup service   | Not Started | 2026-01-26 |       |
| 1.3 | Implement restore service  | Not Started | 2026-01-26 |       |
| 1.4 | Add scheduled backups      | Not Started | 2026-01-26 |       |
| 1.5 | Create REST endpoints      | Not Started | 2026-01-26 |       |
| 1.6 | Implement backup rotation  | Not Started | 2026-01-26 |       |
| 1.7 | Add integrity verification | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, low priority for initial phases
- Should be implemented after core functionality is stable

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.admin
├── web.command
│   ├── BackupController
│   ├── dto.CreateBackupRequest
│   └── dto.BackupResponse
├── application
│   ├── usecase.CreateBackupUseCase
│   ├── usecase.RestoreBackupUseCase
│   ├── service.BackupService
│   └── service.BackupScheduler
├── domain
│   ├── model.BackupMetadata
│   └── repository.BackupRepository
└── infrastructure
    └── backup (pg_dump/pg_restore integration)
```

**API Endpoints:**

- `POST /api/admin/backups` - Create manual backup (ADMIN only)
- `GET /api/admin/backups` - List available backups (ADMIN only)
- `GET /api/admin/backups/{id}` - Get backup details (ADMIN only)
- `POST /api/admin/backups/{id}/restore` - Restore from backup (ADMIN only)
- `DELETE /api/admin/backups/{id}` - Delete backup (ADMIN only)
- `GET /api/admin/backups/config` - Get backup configuration (ADMIN only)
- `PUT /api/admin/backups/config` - Update backup configuration (ADMIN only)

**Database Schema:**

```sql
CREATE TABLE backup_metadata
(
    id            UUID PRIMARY KEY,
    filename      VARCHAR(255) NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    file_size     BIGINT,
    created_at    TIMESTAMP    NOT NULL,
    created_by    UUID,
    backup_type   VARCHAR(20)  NOT NULL, -- MANUAL, SCHEDULED
    status        VARCHAR(20)  NOT NULL, -- IN_PROGRESS, COMPLETED, FAILED
    error_message TEXT,
    checksum      VARCHAR(64)            -- For integrity verification
);

CREATE INDEX idx_backup_created ON backup_metadata (created_at DESC);
```

**Configuration:**

```yaml
bikerental:
  backup:
    enabled: true
    schedule: "0 0 2 * * ?" # Daily at 2 AM
    retention-days: 30
    storage-path: "/var/backups/bikerental"
    compression: true
```

**Backup Service Example:**

```java

@Service
public class BackupService {

    @Scheduled(cron = "${bikerental.backup.schedule}")
    public void scheduledBackup() {
        createBackup(BackupType.SCHEDULED);
    }

    public BackupMetadata createBackup(BackupType type) {
        String filename = generateFilename();
        ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-h", dbHost,
                "-U", dbUser,
                "-F", "c",  // Custom format
                "-f", filename,
                dbName
        );
        // Execute and track progress
    }
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-AD-006/us-ad-006.md](../../../docs/tasks/us/US-AD-006/us-ad-006.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- PostgreSQL Documentation: pg_dump and pg_restore
- Consider: Future cloud storage integration (S3, Azure Blob)
