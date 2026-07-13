package com.github.jenkaby.bikerental.shared.domain.model.vo;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Sort(List<Order> orders) {

    private static final Sort UNSORTED = new Sort(List.of());

    public Sort {
        orders = List.copyOf(orders);
    }

    public static Sort unsorted() {
        return UNSORTED;
    }

    public static Sort by(@NonNull Order... orders) {
        Objects.requireNonNull(orders, "Orders must not be null");
        return new Sort(Arrays.asList(orders));
    }

    public boolean isSorted() {
        return !orders.isEmpty();
    }

    public enum Direction {
        ASC,
        DESC;

        public boolean isDescending() {
            return this == DESC;
        }
    }

    public record Order(String property, Direction direction) {

        public Order {
            Objects.requireNonNull(property, "Sort property must not be null");
            Objects.requireNonNull(direction, "Sort direction must not be null");
        }

        public static Order asc(String property) {
            return new Order(property, Direction.ASC);
        }

        public static Order desc(String property) {
            return new Order(property, Direction.DESC);
        }
    }
}
