package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Sort;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper
public interface PageMapper {

    default PageRequest toPageRequest(Pageable pageable) {
        return new PageRequest(
                pageable.getPageSize(),
                pageable.getPageNumber(),
                toDomainSort(pageable.getSort()));
    }

    default org.springframework.data.domain.PageRequest toSpring(PageRequest pageRequest) {
        return org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                toSpringSort(pageRequest.sort()));
    }

    default <T> Page<? extends T> toDomain(org.springframework.data.domain.Page<T> input) {
        var pageRequest = new PageRequest(
                input.getSize(),
                input.getNumber(),
                toDomainSort(input.getSort()));
        return new Page<>(input.getContent(), input.getTotalElements(), pageRequest);
    }

    default Sort toDomainSort(org.springframework.data.domain.Sort sort) {
        if (sort.isUnsorted()) {
            return Sort.unsorted();
        }
        var orders = sort.stream()
                .map(order -> new Sort.Order(order.getProperty(), toDomainDirection(order.getDirection())))
                .toList();
        return new Sort(orders);
    }

    default org.springframework.data.domain.Sort toSpringSort(Sort sort) {
        if (!sort.isSorted()) {
            return org.springframework.data.domain.Sort.unsorted();
        }
        var orders = sort.orders().stream()
                .map(order -> new org.springframework.data.domain.Sort.Order(
                        toSpringDirection(order.direction()), order.property()))
                .toList();
        return org.springframework.data.domain.Sort.by(orders);
    }

    default Sort.Direction toDomainDirection(org.springframework.data.domain.Sort.Direction direction) {
        return direction.isDescending() ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    default org.springframework.data.domain.Sort.Direction toSpringDirection(Sort.Direction direction) {
        return direction.isDescending()
                ? org.springframework.data.domain.Sort.Direction.DESC
                : org.springframework.data.domain.Sort.Direction.ASC;
    }
}
