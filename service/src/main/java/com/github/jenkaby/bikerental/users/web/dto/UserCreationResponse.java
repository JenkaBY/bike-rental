package com.github.jenkaby.bikerental.users.web.dto;

public record UserCreationResponse(
        UserResponse user,
        String temporaryPassword
) {
}
