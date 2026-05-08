package com.github.jenkaby.bikerental.shared.web;

import com.github.jenkaby.bikerental.shared.config.DevClockConfig.SettableClock;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Profile({"dev", "test"})
@RestController
@RequestMapping("/api/dev/time")
public class TimeTravelController {

    private final SettableClock settableClock;
    private final long sseIntervalMs;

    public TimeTravelController(
            SettableClock settableClock,
            @Value("${app.dev.virtual-clock.sse-interval-ms:1000}") long sseIntervalMs
    ) {
        this.settableClock = settableClock;
        this.sseIntervalMs = sseIntervalMs;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimeResponse> setTime(@Valid @RequestBody SetTimeRequest request) {
        settableClock.setInstant(request.instant());
        return ResponseEntity.ok(new TimeResponse(settableClock.instant()));
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTime() {
        SseEmitter emitter = new SseEmitter(0L);
        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .data(new TimeEvent(settableClock.instant(), settableClock.isFixed())));
            } catch (IOException e) {
                emitter.completeWithError(e);
                executor.shutdown();
            }
        }, 0, sseIntervalMs, TimeUnit.MILLISECONDS);
        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(executor::shutdown);
        return emitter;
    }

    @DeleteMapping
    public ResponseEntity<Void> resetTime() {
        settableClock.reset();
        return ResponseEntity.noContent().build();
    }

    public record SetTimeRequest(
            @NotNull(message = "instant must not be null")
            Instant instant
    ) {
    }

    public record TimeResponse(@NotNull Instant instant) {
    }

    public record TimeEvent(@NotNull Instant instant, boolean fixed) {
    }
}
