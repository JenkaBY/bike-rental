package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface InsertableRepository<ENTITY, ID> {

    <S extends ENTITY> S insert(S entity);

    <S extends ENTITY> Iterable<S> insertAll(Iterable<S> entities);
}
