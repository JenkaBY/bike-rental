package com.github.jenkaby.bikerental.shared.domain.model.vo;

import org.jspecify.annotations.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public record Page<T>(List<T> items, long totalItems, PageRequest pageRequest) {

    public Page(List<T> items, long totalItems, PageRequest pageRequest) {
        this.items = List.copyOf(items);
        this.totalItems = totalItems;
        this.pageRequest = pageRequest;
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalItems / pageRequest.limit());
    }

    public <U> Page<U> map(@NonNull Function<? super T, ? extends U> mapper) {
        Assert.notNull(mapper, "Mapper function must not be null");
        return new Page<>(getConvertedContent(mapper), totalItems, pageRequest);
    }

    private <U> List<U> getConvertedContent(Function<? super T, ? extends U> converter) {
        return this.items.stream().map(converter).collect(Collectors.toList());
    }

    public static <S> Page<S> empty(PageRequest pageRequest) {
        return new Page<S>(List.<S>of(), 0, pageRequest);
    }

    public static <T> Page<?> of(@NonNull T item, PageRequest pageRequest) {
        return new Page<>(List.of(item), 1, pageRequest);
    }
}
