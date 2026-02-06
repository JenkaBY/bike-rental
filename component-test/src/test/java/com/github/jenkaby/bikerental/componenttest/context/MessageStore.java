package com.github.jenkaby.bikerental.componenttest.context;

import com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;


@Getter
@Component
@Slf4j
public class MessageStore {

    private Map<Class<?>, List<BikeRentalEvent>> otherEvents = new ConcurrentHashMap<>();

    public void clear() {
        otherEvents.clear();
    }

    public void handleEvent(BikeRentalEvent event) {
        Class<?> eventClass = event.getClass();
        otherEvents.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(event);
        log.debug("[TEST]Stored domain event: {}", eventClass.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public <T extends BikeRentalEvent> List<T> getEvents(Class<T> eventClass) {
        return (List<T>) otherEvents.getOrDefault(eventClass, List.of());
    }

    private void debug() {
        otherEvents.forEach((key, value) -> {
            log.info("[TEST]Event type: {}, received events: {}", key.getSimpleName(), value.size());
        });
    }

    public <T extends BikeRentalEvent> List<T> getEventsFor(Class<T> clazz, Set<Object> ids, Function<T, Object> idExtractor) {
        return this.getEvents(clazz).stream()
                .filter(e -> ids.contains(idExtractor.apply(e)))
                .toList();
    }
}
