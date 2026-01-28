# [US-MT-004] - Уведомления о технических проблемах (Technical Issue Notifications)

- Dependencies: US-MT-001, US-RN-006
- User Story File: [docs/tasks/us/US-MT-004/us-mt-004.md](../../../docs/tasks/us/US-MT-004/us-mt-004.md)

## References

```
CREATE INDEX idx_notifications_read ON notifications(is_read, created_at);
CREATE INDEX idx_notifications_recipient ON notification_recipients(user_id);

);
    PRIMARY KEY (notification_id, user_id)
    user_id UUID REFERENCES app_users(id),
    notification_id UUID REFERENCES notifications(id),
CREATE TABLE notification_recipients (

);
    is_read BOOLEAN NOT NULL DEFAULT false
    created_at TIMESTAMP NOT NULL,
    message TEXT NOT NULL,
    title VARCHAR(255) NOT NULL,
    equipment_id UUID REFERENCES equipment(id),
    priority VARCHAR(10) NOT NULL,
    type VARCHAR(50) NOT NULL,
    id UUID PRIMARY KEY,
CREATE TABLE notifications (
```sql
**Database Schema:**

- `GET /api/notifications/unread-count` - Get unread count
- `PATCH /api/notifications/{id}/read` - Mark notification as read
- `GET /api/notifications` - Get user notifications
**API Endpoints:**

```

}
}
}
notificationService.sendNotification(notification);
notification.setRecipients(getTechnicians());

            );
                    eq.getSerialNumber(), earlyReturns, recentRentals)
                String.format("Equipment %s has %d early returns in %d rentals",
                "High Early Return Rate Detected",
                eq.getId(),
                NotificationPriority.MEDIUM,
                NotificationType.PATTERN_DETECTED,
            Notification notification = new Notification(
            
            (earlyReturns / (double) recentRentals) > 0.3) { // 30% threshold
        if (recentRentals >= 5 && 
        
        int earlyReturns = countEarlyReturns(eq.getId(), 30);
        int recentRentals = countRecentRentals(eq.getId(), 30); // Last 30 days
        // Check for high early return rate
    for (Equipment eq : equipment) {
    
    List<Equipment> equipment = equipmentRepository.findAll();

public void detectEquipmentProblems() {
@Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM

```java
**Pattern Detection:**

```

}
}
notificationService.sendNotification(notification);
notification.setRecipients(getTechnicians());

        );
            event.issueNotes()
            "Equipment Issue Reported",
            event.equipmentId(),
            NotificationPriority.MEDIUM,
            NotificationType.EQUIPMENT_ISSUE_REPORTED,
        Notification notification = new Notification(
    if (event.hasIssues()) {

public void onEquipmentIssueReported(EquipmentReturnCompleted event) {
@EventListener

) {}
String issueNotes // Optional issue description
EquipmentCondition condition,
LocalDateTime returnTime,
UUID rentalId,
public record CompleteRentalRequest(
// Add to RN-006 return process

```java
**Issue Reporting
During Return:**

```

}
}
notificationService.sendNotification(notification);
notification.setRecipients(getTechnicians());

        );
            "Equipment has been blocked due to overdue maintenance"
            "Critical: Maintenance Overdue",
            event.equipmentId(),
            NotificationPriority.HIGH,
            NotificationType.MAINTENANCE_OVERDUE,
        Notification notification = new Notification(
        // High priority notification
    public void onMaintenanceOverdue(MaintenanceOverdue event) {
    @EventListener
    
    }
        notificationService.sendNotification(notification);
        notification.setRecipients(getTechnicians());
        
        );
                equipment.getSerialNumber(), event.dueDate())
            String.format("Equipment %s requires maintenance by %s", 
            "Maintenance Due Soon",
            equipment.getId(),
            NotificationPriority.MEDIUM,
            NotificationType.MAINTENANCE_DUE,
        Notification notification = new Notification(
        
            .orElseThrow();
        Equipment equipment = equipmentRepository.findById(event.equipmentId())
    public void onMaintenanceDue(MaintenanceSoon event) {
    @EventListener

public class MaintenanceNotificationHandler {
@Component

```java
**Event Listeners:**

```

}
}
}
// SMS integration
if (notification.getPriority() == NotificationPriority.HIGH) {
// Send SMS for high priority (optional)

        }
            emailService.send(emails, notification.getTitle(), notification.getMessage());
            List<String> emails = getUserEmails(notification.getRecipients());
        if (notification.getPriority() != NotificationPriority.LOW) {
        // Send email based on priority
        
        notificationRepository.save(notification);
        // Save in-app notification
    public void sendNotification(Notification notification) {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

public class NotificationService {
@Service

```java
**Notification Service:**

```

}
HIGH, MEDIUM, LOW
public enum NotificationPriority {

}
PATTERN_DETECTED
EQUIPMENT_ISSUE_REPORTED,
MAINTENANCE_OVERDUE,
MAINTENANCE_DUE,
public enum NotificationType {

}
private List<UUID> recipients; // User IDs
private boolean isRead;
private LocalDateTime createdAt;
private String message;
private String title;
private UUID equipmentId;
private NotificationPriority priority;
private NotificationType type;
private UUID id;
public class Notification {

```java
**Domain Model:**

## Technical Details

| 4.7 | Create tests                       | Not Started | 2026-01-26 |       |
| 4.6 | Add email/SMS integration          | Not Started | 2026-01-26 |       |
| 4.5 | Implement pattern detection        | Not Started | 2026-01-26 |       |
| 4.4 | Add issue reporting                | Not Started | 2026-01-26 |       |
| 4.3 | Add maintenance event listeners    | Not Started | 2026-01-26 |       |
| 4.2 | Implement notification service     | Not Started | 2026-01-26 |       |
| 4.1 | Create notification domain model   | Not Started | 2026-01-26 |       |
|-----|------------------------------------|-------------|------------|-------|
| ID  | Description                        | Status      | Updated    | Notes |

### Subtasks

**Overall Status:** Not Started - 0%

## Progress Tracking

- [ ] Write unit tests
- [ ] Create component tests
- [ ] Add database migration
- [ ] Create REST endpoints
- [ ] Add SMS integration (optional)
- [ ] Create in-app notification system
- [ ] Create email notification sender
- [ ] Implement early return pattern detection
- [ ] Add issue reporting during return
- [ ] Listen to maintenance events (MaintenanceDue, MaintenanceOverdue)
- [ ] Implement notification service
- [ ] Create Notification domain model

## Implementation Plan

- **Low**: Pattern detection, informational
- **Medium**: Upcoming maintenance, reported issues
- **High**: Overdue critical maintenance, safety issues
**Priority Levels:**

- **SMS**: For critical issues (optional)
- **Email**: For non-urgent issues
- **In-App**: Dashboard notifications
**Notification Channels:**

4. **Pattern Detection**: Frequent early returns indicate problems
3. **Operator Reports**: Issues flagged during return
2. **Overdue Maintenance**: Critical alerts for blocked equipment
1. **Scheduled Maintenance**: From US-MT-001 (maintenance scheduling)
**Notification Types:**

Notification system ensures timely response to equipment issues. Integrates multiple data sources to identify problems.

## Thought Process

**Связанные требования:** FR-MT-004

- Приоритизация уведомлений
- Уведомления в системе
- Email/SMS уведомления
**Критерии приемки:**

- Высокая частота ранних возвратов (признак проблемы)
- Оператор отметил неисправность при возврате
- Оборудование просрочило ТО
- Оборудование требует планового ТО
**Триггеры уведомлений:**

Система должна отправлять уведомления техническому персоналу о проблемах с оборудованием.
**Описание:**  

## User Story Details

**Чтобы** своевременно реагировать на неисправности
**Я хочу** получать уведомления о проблемах с оборудованием  
**Как** Технический персонал  

## Original Request

**Dependencies:** US-MT-001, US-RN-006
**Module:** maintenance  
**Priority:** Low  
**Updated:** 2026-01-26  
**Added:** 2026-01-26  
**Status:** Pending  

