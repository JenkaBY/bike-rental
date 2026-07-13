package com.github.jenkaby.bikerental.shared.domain.model.vo;

import java.util.Objects;

public record PageRequest(int size, int page, Sort sort) {

    private static final PageRequest SINGLE_PAGE = new PageRequest(1, 0, Sort.unsorted());

    public PageRequest {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid paging parameters");
        }
        Objects.requireNonNull(sort, "Sort must not be null");
    }

    public PageRequest(int size, int page) {
        this(size, page, Sort.unsorted());
    }

    public int offset() {
        return page * size;
    }

    public int limit() {
        return size;
    }

    public static PageRequest singleItem() {
        return SINGLE_PAGE;
    }
}
