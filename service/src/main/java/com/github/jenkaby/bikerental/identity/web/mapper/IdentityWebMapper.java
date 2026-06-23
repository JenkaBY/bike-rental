package com.github.jenkaby.bikerental.identity.web.mapper;

import com.github.jenkaby.bikerental.identity.application.usecase.CreateUserCommand;
import com.github.jenkaby.bikerental.identity.application.usecase.UpdateUserCommand;
import com.github.jenkaby.bikerental.identity.application.usecase.UserWithTemporaryPassword;
import com.github.jenkaby.bikerental.identity.domain.model.User;
import com.github.jenkaby.bikerental.identity.web.dto.CreateUserRequest;
import com.github.jenkaby.bikerental.identity.web.dto.UpdateUserRequest;
import com.github.jenkaby.bikerental.identity.web.dto.UserCreationResponse;
import com.github.jenkaby.bikerental.identity.web.dto.UserResponse;
import com.github.jenkaby.bikerental.shared.mapper.EmailAddressMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = EmailAddressMapper.class)
public interface IdentityWebMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    UserCreationResponse toCreationResponse(UserWithTemporaryPassword result);

    CreateUserCommand toCreateCommand(CreateUserRequest request);

    UpdateUserCommand toUpdateCommand(UpdateUserRequest request);
}
