package com.github.jenkaby.bikerental.users.web;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.users.application.usecase.UserAccountUseCase;
import com.github.jenkaby.bikerental.users.web.dto.CreateUserRequest;
import com.github.jenkaby.bikerental.users.web.dto.UpdateUserRequest;
import com.github.jenkaby.bikerental.users.web.dto.UserCreationResponse;
import com.github.jenkaby.bikerental.users.web.dto.UserResponse;
import com.github.jenkaby.bikerental.users.web.mapper.UsersWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(path = "/api/auth/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = OpenApiConfig.Tags.USERS)
@Slf4j
@RequiredArgsConstructor
class UserAccountController {

    private final UserAccountUseCase userAccount;
    private final UsersWebMapper mapper;

    @PostMapping
    @Operation(summary = "Create account", description = "Creates a user account with a temporary password (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created",
                    content = @Content(schema = @Schema(implementation = UserCreationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserCreationResponse> create(@Valid @RequestBody CreateUserRequest request) {
        log.info("[POST] Creating account with username: {}", request.username());
        var result = userAccount.createUser(mapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCreationResponse(result));
    }

    @GetMapping
    @Operation(summary = "List accounts", description = "Returns all user accounts (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    })
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(mapper.toResponseList(userAccount.listUsers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account", description = "Returns a single user account (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account returned",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponse> get(@Parameter(description = "Account ID") @PathVariable("id") UUID id) {
        return ResponseEntity.ok(mapper.toResponse(userAccount.getUser(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account", description = "Updates roles, status and display name (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponse> update(@Parameter(description = "Account ID") @PathVariable("id") UUID id,
                                               @Valid @RequestBody UpdateUserRequest request) {
        log.info("[PUT] Updating account with id: {}", id);
        var user = userAccount.updateUser(id, mapper.toUpdateCommand(request));
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate account", description = "Disables a user account and revokes its sessions; accounts are never deleted (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deactivated",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponse> deactivate(@Parameter(description = "Account ID") @PathVariable("id") UUID id) {
        log.info("[POST] Deactivating account with id: {}", id);
        var user = userAccount.deactivateUser(id);
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset password", description = "Sets a temporary password, forces change, revokes sessions (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset — temporary password returned",
                    content = @Content(schema = @Schema(implementation = UserCreationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserCreationResponse> resetPassword(@Parameter(description = "Account ID") @PathVariable("id") UUID id) {
        log.info("[POST] Resetting password for account with id: {}", id);
        var result = userAccount.resetPassword(id);
        return ResponseEntity.ok(mapper.toCreationResponse(result));
    }
}
