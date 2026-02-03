package com.github.jenkaby.bikerental.finance.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class TimestampReceiptNumberGenerationService implements ReceiptNumberGenerationService {

    private static final String RECEIPT_FORMAT = "RCP-%s-%s";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int MAX_RANDOM_SUFFIX = 10000;
    private final Random random = new SecureRandom();
    private final Clock clock;

    @Override
    public String generate() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        String ts = FORMATTER.format(now);
        int randomSuffix = random.nextInt(MAX_RANDOM_SUFFIX);
        String rand = Integer.toHexString(randomSuffix).toUpperCase();
        return RECEIPT_FORMAT.formatted(ts, rand);
    }
}
