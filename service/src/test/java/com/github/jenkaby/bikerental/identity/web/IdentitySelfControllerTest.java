package com.github.jenkaby.bikerental.identity.web;

import com.github.jenkaby.bikerental.identity.application.config.JwtProperties;
import com.github.jenkaby.bikerental.identity.application.usecase.SelfServiceUseCase;
import com.github.jenkaby.bikerental.identity.domain.model.User;
import com.github.jenkaby.bikerental.identity.web.dto.ChangePasswordRequest;
import com.github.jenkaby.bikerental.identity.web.dto.UserResponse;
import com.github.jenkaby.bikerental.identity.web.mapper.IdentityWebMapper;
import com.github.jenkaby.bikerental.support.web.ApiTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = IdentitySelfController.class)
class IdentitySelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SelfServiceUseCase selfService;

    @MockitoBean
    private IdentityWebMapper mapper;

    @MockitoBean
    private JwtProperties jwtProperties;

    @Nested
    class GetMe {

        @Test
        void returnsCurrentUser() throws Exception {
            var userId = UUID.randomUUID();
            given(jwtProperties.userIdClaim()).willReturn("uid");
            given(selfService.getById(userId)).willReturn(mock(User.class));
            given(mapper.toResponse(any())).willReturn(mock(UserResponse.class));

            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(builder -> builder.claim("uid", userId.toString()))))
                    .andExpect(status().isOk());

            verify(selfService).getById(userId);
        }
    }

    @Nested
    class ChangePassword {

        @Test
        void returns204WhenRequestIsValid() throws Exception {
            var userId = UUID.randomUUID();
            given(jwtProperties.userIdClaim()).willReturn("uid");
            var request = new ChangePasswordRequest("CurrentPass1", "NewPass12345");

            mockMvc.perform(post("/api/auth/password")
                            .with(jwt().jwt(builder -> builder.claim("uid", userId.toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(selfService).changeOwnPassword(eq(userId), eq("CurrentPass1"), eq("NewPass12345"));
        }

        @ParameterizedTest(name = "[{index}] newPassword=\"{0}\"")
        @ValueSource(strings = {
                "short",
                "1234567",
                "Abc1",
                "abcdefghij",
                "1234567890",
                "Abcdefghij1234567890X"
        })
        @NullAndEmptySource
        void returns400WhenNewPasswordIsInvalid(String newPassword) throws Exception {
            var request = new ChangePasswordRequest("CurrentPass1", newPassword);

            mockMvc.perform(post("/api/auth/password")
                            .with(jwt().jwt(builder -> builder.claim("uid", UUID.randomUUID().toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(selfService, never()).changeOwnPassword(any(), any(), any());
        }

        @ParameterizedTest(name = "[{index}] newPassword=\"{0}\"")
        @ValueSource(strings = {
                "Abcdef12",
                "Abcdefghijklmno12345"
        })
        void returns204WhenNewPasswordIsAtPolicyBoundary(String newPassword) throws Exception {
            var userId = UUID.randomUUID();
            given(jwtProperties.userIdClaim()).willReturn("uid");
            var request = new ChangePasswordRequest("CurrentPass1", newPassword);

            mockMvc.perform(post("/api/auth/password")
                            .with(jwt().jwt(builder -> builder.claim("uid", userId.toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(selfService).changeOwnPassword(eq(userId), eq("CurrentPass1"), eq(newPassword));
        }
    }
}
