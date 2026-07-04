<task_file_template>

# Task 009: Create the ContentHasher application component

> **Applied Skill:** `spring-boot-best-practices` — small stateless `@Component`; `java-best-practices`
> — zero inline comments, `final` fields, no `null` contracts.

## 1. Objective

Provide a SHA-256 hex hasher used by the activation service to pin the content digest at activation.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/ContentHasher.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class ContentHasher {

    private static final String SHA_256 = "SHA-256";

    public String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
```

> `HexFormat.of().formatHex(...)` yields a 64-char lowercase hex string that fits the `content_sha256
> CHAR(64)` column exactly.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
