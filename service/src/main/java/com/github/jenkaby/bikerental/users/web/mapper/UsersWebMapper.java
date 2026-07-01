package com.github.jenkaby.bikerental.users.web.mapper;

import com.github.jenkaby.bikerental.users.application.usecase.CreateUserCommand;
import com.github.jenkaby.bikerental.users.application.usecase.UpdateUserCommand;
import com.github.jenkaby.bikerental.users.application.usecase.UserWithTemporaryPassword;
import com.github.jenkaby.bikerental.users.domain.model.User;
import com.github.jenkaby.bikerental.users.web.dto.CreateUserRequest;
import com.github.jenkaby.bikerental.users.web.dto.UpdateUserRequest;
import com.github.jenkaby.bikerental.users.web.dto.UserCreationResponse;
import com.github.jenkaby.bikerental.users.web.dto.UserResponse;
import com.github.jenkaby.bikerental.shared.mapper.EmailAddressMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = EmailAddressMapper.class)
public interface UsersWebMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    UserCreationResponse toCreationResponse(UserWithTemporaryPassword result);

    CreateUserCommand toCreateCommand(CreateUserRequest request);

    UpdateUserCommand toUpdateCommand(UpdateUserRequest request);
}
