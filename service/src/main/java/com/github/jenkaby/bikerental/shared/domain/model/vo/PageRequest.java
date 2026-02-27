package com.github.jenkaby.bikerental.shared.domain.model.vo;

import org.jspecify.annotations.Nullable;

public record PageRequest(int size, int page, @Nullable String sortBy) {

    private static final PageRequest SINGLE_PAGE = new PageRequest(1, 0, null);

    public PageRequest {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid paging parameters");
        }
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
