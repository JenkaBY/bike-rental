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
