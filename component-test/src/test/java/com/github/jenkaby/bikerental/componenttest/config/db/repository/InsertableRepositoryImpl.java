package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@Transactional
public class InsertableRepositoryImpl<ENTITY, ID> implements InsertableRepository<ENTITY, ID> {

    private final JpaEntityInserter entityInserter;

    public InsertableRepositoryImpl(JpaEntityInserter entityInserter) {
        this.entityInserter = entityInserter;
    }

    @Override
    public <S extends ENTITY> S insert(S entity) {
        log.debug("Inserting entity: {}", entity);
        return entityInserter.insert(entity);
    }

    @Override
    public <S extends ENTITY> Iterable<S> insertAll(Iterable<S> entities) {
        log.debug("Inserting multiple entities");
        return entityInserter.insertAll(entities);
    }
}
