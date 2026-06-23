package com.github.jenkaby.bikerental.identity.web;

import com.github.jenkaby.bikerental.identity.application.usecase.CreateUserCommand;
import com.github.jenkaby.bikerental.identity.application.usecase.UserAccountUseCase;
import com.github.jenkaby.bikerental.identity.application.usecase.UserWithTemporaryPassword;
import com.github.jenkaby.bikerental.identity.domain.model.Role;
import com.github.jenkaby.bikerental.identity.domain.model.User;
import com.github.jenkaby.bikerental.identity.web.dto.CreateUserRequest;
import com.github.jenkaby.bikerental.identity.web.dto.UserCreationResponse;
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

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest(controllers = UserAccountController.class)
class UserAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserAccountUseCase userAccount;

    @MockitoBean
    private IdentityWebMapper mapper;

    @Nested
    class PostUsers {

        @Nested
        class ShouldReturn201 {

            @Test
            void whenRequestIsValid() throws Exception {
                var request = new CreateUserRequest("j.doe", "j.doe@example.com", "John Doe", Set.of(Role.OPERATOR), null);
                given(mapper.toCreateCommand(any())).willReturn(mock(CreateUserCommand.class));
                given(userAccount.createUser(any()))
                        .willReturn(new UserWithTemporaryPassword(mock(User.class), "TempPass123"));
                given(mapper.toCreationResponse(any()))
                        .willReturn(new UserCreationResponse(mock(UserResponse.class), "TempPass123"));

                mockMvc.perform(post("/api/auth/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.temporaryPassword").value("TempPass123"));

                verify(userAccount).createUser(any());
            }

            @ParameterizedTest(name = "[{index}] password=\"{0}\"")
            @ValueSource(strings = {
                    "Abcdef12",
                    "Abcdefghijklmno12345"
            })
            void whenAdminSuppliesPasswordAtPolicyBoundary(String password) throws Exception {
                var request = new CreateUserRequest("j.doe", "j.doe@example.com", "John Doe", Set.of(Role.OPERATOR), password);
                given(mapper.toCreateCommand(any())).willReturn(mock(CreateUserCommand.class));
                given(userAccount.createUser(any()))
                        .willReturn(new UserWithTemporaryPassword(mock(User.class), password));
                given(mapper.toCreationResponse(any()))
                        .willReturn(new UserCreationResponse(mock(UserResponse.class), password));

                mockMvc.perform(post("/api/auth/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());

                verify(userAccount).createUser(any());
            }
        }

        @Nested
        class ShouldReturn400 {

            @ParameterizedTest
            @ValueSource(strings = {"   ", "\t"})
            @NullAndEmptySource
            void whenUsernameIsBlank(String username) throws Exception {
                var request = new CreateUserRequest(username, "j.doe@example.com", "John Doe", Set.of(Role.OPERATOR), null);

                mockMvc.perform(post("/api/auth/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("username"));

                verify(userAccount, never()).createUser(any());
            }

            @ParameterizedTest
            @ValueSource(strings = {"not-an-email", "j.doe@", "@example.com"})
            void whenEmailIsInvalid(String email) throws Exception {
                var request = new CreateUserRequest("j.doe", email, "John Doe", Set.of(Role.OPERATOR), null);

                mockMvc.perform(post("/api/auth/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("email"));

                verify(userAccount, never()).createUser(any());
            }

            @ParameterizedTest(name = "[{index}] password=\"{0}\"")
            @ValueSource(strings = {
                    "short",
                    "1234567",
                    "Abc1",
                    "abcdefghij",
                    "1234567890",
                    "Abcdefghij1234567890X"
            })
            void whenPasswordViolatesPolicy(String password) throws Exception {
                var request = new CreateUserRequest("j.doe", "j.doe@example.com", "John Doe", Set.of(Role.OPERATOR), password);

                mockMvc.perform(post("/api/auth/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("password"));

                verify(userAccount, never()).createUser(any());
            }

            @Test
            void whenRolesAreMissing() throws Exception {
                var request = new CreateUserRequest("j.doe", "j.doe@example.com", "John Doe", Set.of(), null);

                mockMvc.perform(post("/api/auth/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("roles"));

                verify(userAccount, never()).createUser(any());
            }
        }
    }
}
