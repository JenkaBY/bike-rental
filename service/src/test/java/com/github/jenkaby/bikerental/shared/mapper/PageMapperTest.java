package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageMapper Tests")
class PageMapperTest {

    private final PageMapper mapper = new PageMapper() {
    };

    @Test
    @DisplayName("toSpring should handle plain property sort")
    void toSpring_plainProperty() {
        var pr = new PageRequest(10, 0, "name");
        var spring = mapper.toSpring(pr);

        assertThat(spring.getPageSize()).isEqualTo(10);
        assertThat(spring.getPageNumber()).isEqualTo(0);
        assertThat(spring.getSort().isSorted()).isTrue();
        var order = spring.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("toSpring should handle 'name asc' format")
    void toSpring_nameAsc() {
        var pr = new PageRequest(20, 1, "name asc");
        var spring = mapper.toSpring(pr);

        assertThat(spring.getPageSize()).isEqualTo(20);
        assertThat(spring.getPageNumber()).isEqualTo(1);
        var order = spring.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("toSpring should handle 'name desc' format")
    void toSpring_nameDesc() {
        var pr = new PageRequest(5, 2, "name desc");
        var spring = mapper.toSpring(pr);

        var order = spring.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("toSpring should handle 'name: ASC' format")
    void toSpring_colonFormat() {
        var pr = new PageRequest(5, 0, "name: ASC");
        var spring = mapper.toSpring(pr);

        var order = spring.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("toSpring should handle 'name,desc' format")
    void toSpring_commaFormat() {
        var pr = new PageRequest(5, 0, "name,desc");
        var spring = mapper.toSpring(pr);

        var order = spring.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("toDomain should expose pageRequest fields correctly")
    void toDomain_roundtrip() {
        var contentPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of("a", "b"), mapper.toSpring(new PageRequest(2, 0, "name")), 10);
        var domain = mapper.toDomain(contentPage);

        assertThat(domain.totalItems()).isEqualTo(10);
        assertThat(domain.pageRequest().size()).isEqualTo(2);
        assertThat(domain.pageRequest().page()).isEqualTo(0);
        assertThat(domain.pageRequest().sortBy()).isEqualTo("name");
        assertThat(domain.items()).hasSize(2);
        assertThat(domain.items().get(0)).isEqualTo("a");
        assertThat(domain.items().get(1)).isEqualTo("b");
    }
}
