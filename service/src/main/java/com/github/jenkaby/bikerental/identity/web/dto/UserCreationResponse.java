package com.github.jenkaby.bikerental.identity.web.dto;

public record UserCreationResponse(
        UserResponse user,
        String temporaryPassword
) {
}
