package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Sort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("PageMapper Tests")
class PageMapperTest {

    private final PageMapper mapper = new PageMapper() {
    };

    @Test
    @DisplayName("toSpring should preserve ascending direction")
    void toSpring_ascendingDirection() {
        var pr = new PageRequest(10, 0, Sort.by(Sort.Order.asc("name")));

        var actual = mapper.toSpring(pr);

        assertThat(actual.getPageSize()).isEqualTo(10);
        assertThat(actual.getPageNumber()).isEqualTo(0);
        var order = actual.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(org.springframework.data.domain.Sort.Direction.ASC);
    }

    @Test
    @DisplayName("toSpring should preserve descending direction")
    void toSpring_descendingDirection() {
        var pr = new PageRequest(5, 2, Sort.by(Sort.Order.desc("name")));

        var actual = mapper.toSpring(pr);

        var order = actual.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    @Test
    @DisplayName("toSpring should preserve multiple sort fields with their directions and order")
    void toSpring_multipleFields() {
        var pr = new PageRequest(5, 0, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));

        var actual = mapper.toSpring(pr);

        assertThat(actual.getSort())
                .extracting(org.springframework.data.domain.Sort.Order::getProperty,
                        org.springframework.data.domain.Sort.Order::getDirection)
                .containsExactly(
                        tuple("createdAt", org.springframework.data.domain.Sort.Direction.DESC),
                        tuple("id", org.springframework.data.domain.Sort.Direction.ASC));
    }

    @Test
    @DisplayName("toSpring should map unsorted request to unsorted pageable")
    void toSpring_unsorted() {
        var pr = new PageRequest(5, 0, Sort.unsorted());

        var actual = mapper.toSpring(pr);

        assertThat(actual.getSort().isUnsorted()).isTrue();
    }

    @Test
    @DisplayName("toPageRequest should preserve every order and direction from the pageable")
    void toPageRequest_preservesAllOrders() {
        var pageable = org.springframework.data.domain.PageRequest.of(1, 20,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("createdAt"),
                        org.springframework.data.domain.Sort.Order.asc("id")));

        var actual = mapper.toPageRequest(pageable);

        assertThat(actual.size()).isEqualTo(20);
        assertThat(actual.page()).isEqualTo(1);
        assertThat(actual.sort().orders())
                .extracting(Sort.Order::property, Sort.Order::direction)
                .containsExactly(
                        tuple("createdAt", Sort.Direction.DESC),
                        tuple("id", Sort.Direction.ASC));
    }

    @Test
    @DisplayName("toDomain should expose all page and sort fields correctly")
    void toDomain_roundtrip() {
        var springRequest = mapper.toSpring(new PageRequest(2, 0, Sort.by(Sort.Order.desc("name"))));
        var contentPage = new org.springframework.data.domain.PageImpl<>(List.of("a", "b"), springRequest, 10);

        var domain = mapper.toDomain(contentPage);

        assertThat(domain.totalItems()).isEqualTo(10);
        assertThat(domain.pageRequest().size()).isEqualTo(2);
        assertThat(domain.pageRequest().page()).isEqualTo(0);
        assertThat(domain.pageRequest().sort().orders())
                .extracting(Sort.Order::property, Sort.Order::direction)
                .containsExactly(tuple("name", Sort.Direction.DESC));
        assertThat(domain.items()).hasSize(2);
        assertThat(domain.items().get(0)).isEqualTo("a");
        assertThat(domain.items().get(1)).isEqualTo("b");
    }
}
