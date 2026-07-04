<task_file_template>

# Task 009: Add a sha256(byte[]) overload to ContentHasher

> **Applied Skill:** `java-best-practices` — no duplication of the digest boilerplate; the existing `sha256(String)`
> delegates to the new `byte[]` overload. Zero inline comments.

## 1. Objective

Add a `sha256(byte[])` overload so the signing service can hash the rendered PDF bytes, and route the existing
`sha256(String)` through it to avoid duplicating the digest logic.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/ContentHasher.java`
* **Action:** Modify Existing File

## 3. Code Implementation

Replace the ENTIRE body of the class (the two methods currently present) so the file reads EXACTLY:

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
        return sha256(content.getBytes(StandardCharsets.UTF_8));
    }

    public String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
