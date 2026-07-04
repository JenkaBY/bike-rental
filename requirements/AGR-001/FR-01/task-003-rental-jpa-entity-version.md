<task_file_template>

# Task 003: Add @Version field to RentalJpaEntity

> **Applied Skill:** `spring-boot-data-ddd` — JPA aggregate root with `@Version` for optimistic
> locking; `java-style` — zero inline comments.

## 1. Objective

Add a JPA `@Version` field to `RentalJpaEntity` so Hibernate issues versioned UPDATEs
(`... WHERE id = ? AND version = ?`) and increments the counter on every row update.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/entity/RentalJpaEntity.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

No new imports are needed. The file already imports all JPA annotations via `import jakarta.persistence.*;`
(line 4), which covers `@Version` and `@Column`.

**Code to Add/Replace:**

* **Location:** Add the new field immediately BELOW the `id` field block and ABOVE the `customerId`
  field. The `id` block currently is:

  ```java
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
  ```

  Insert the snippet on the line directly after `private Long id;`.
* **Snippet:**

```java
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
```

After the edit the top of the class body must read:

```java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "customer_id")
    private UUID customerId;
```

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
