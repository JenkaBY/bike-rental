package com.github.jenkaby.bikerental.componenttest.config.db.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class InsertableRepositoryImpl<ENTITY, ID> implements InsertableRepository<ENTITY, ID> {

    private final EntityManager entityManager;

    public InsertableRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <S extends ENTITY> S insert(S entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public <S extends ENTITY> Iterable<S> insertAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(insert(entity));
        }
        return result;
    }
}
