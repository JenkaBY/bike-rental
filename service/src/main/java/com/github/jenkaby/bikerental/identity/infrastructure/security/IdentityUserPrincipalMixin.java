package com.github.jenkaby.bikerental.identity.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.UUID;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class IdentityUserPrincipalMixin {

    @JsonCreator
    IdentityUserPrincipalMixin(
            @JsonProperty("userId") UUID userId,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("enabled") boolean enabled,
            @JsonProperty("mustChangePassword") boolean mustChangePassword,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities) {
    }
}
