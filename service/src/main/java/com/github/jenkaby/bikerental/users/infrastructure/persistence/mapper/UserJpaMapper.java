package com.github.jenkaby.bikerental.users.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.users.domain.model.User;
import com.github.jenkaby.bikerental.users.infrastructure.persistence.entity.UserJpaEntity;
import com.github.jenkaby.bikerental.shared.mapper.EmailAddressMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = EmailAddressMapper.class)
public interface UserJpaMapper {

    User toDomain(UserJpaEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserJpaEntity toEntity(User user);
}
