package com.github.jenkaby.bikerental.users.application.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class PasswordGenerator {

    private static final String LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String ALPHABET = LETTERS + DIGITS;
    private static final int LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        List<Character> characters = new ArrayList<>(LENGTH);
        characters.add(LETTERS.charAt(secureRandom.nextInt(LETTERS.length())));
        characters.add(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        while (characters.size() < LENGTH) {
            characters.add(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        Collections.shuffle(characters, secureRandom);
        var builder = new StringBuilder(LENGTH);
        characters.forEach(builder::append);
        return builder.toString();
    }
}
