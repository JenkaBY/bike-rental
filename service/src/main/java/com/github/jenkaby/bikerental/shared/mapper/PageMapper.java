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
        if (pageRequest.sortBy() == null) {
            return org.springframework.data.domain.PageRequest.of(
                    pageRequest.page(),
                    pageRequest.size(),
                    Sort.unsorted()
            );
        }

        // Normalize sortBy which may contain direction info in various formats
        String raw = pageRequest.sortBy().trim();
        String property = raw;
        Sort.Direction direction = Sort.Direction.ASC;

        // Support formats: "name: ASC", "name,asc", "name asc", or just "name"
        if (raw.contains(":")) {
            String[] parts = raw.split(":");
            property = parts[0].trim();
            if (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }
        } else if (raw.contains(",")) {
            String[] parts = raw.split(",");
            property = parts[0].trim();
            if (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }
        } else if (raw.toLowerCase().endsWith(" desc") || raw.toLowerCase().endsWith(" asc")) {
            int idx = raw.lastIndexOf(' ');
            property = raw.substring(0, idx).trim();
            String d = raw.substring(idx + 1).trim();
            if (d.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }
        }

        Sort sort = Sort.by(direction, property);
        return org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                sort
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
