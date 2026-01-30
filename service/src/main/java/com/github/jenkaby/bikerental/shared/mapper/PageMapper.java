package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Mapper
public interface PageMapper {

    default PageRequest toPageRequest(Pageable pageable) {
        return new PageRequest(
                pageable.getPageSize(),
                pageable.getPageNumber(),
                pageable.getSort().isSorted() ?
                        pageable.getSort().stream()
                                .map(Sort.Order::getProperty)
                                .findFirst()
                                .orElse(null)
                        : null
        );
    }

    default org.springframework.data.domain.PageRequest toSpring(PageRequest pageRequest) {
        return org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                pageRequest.sortBy() != null ?
                        Sort.by(pageRequest.sortBy()) :
                        Sort.unsorted()
        );
    }

    default <T> Page<? extends T> toDomain(org.springframework.data.domain.Page<T> input) {
        var pageRequest = new PageRequest(input.getSize(), input.getNumber(),
                input.getSort().isSorted() ?
                        input.getSort().stream()
                                .map(Sort.Order::getProperty)
                                .findFirst()
                                .orElse(null)
                        : null);
        return new Page<>(input.getContent(), input.getTotalElements(), pageRequest);
    }
}
