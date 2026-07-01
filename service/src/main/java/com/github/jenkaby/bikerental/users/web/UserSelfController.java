package com.github.jenkaby.bikerental.users.web;

import com.github.jenkaby.bikerental.shared.config.OpenApiConfig;
import com.github.jenkaby.bikerental.users.JwtProperties;
import com.github.jenkaby.bikerental.users.application.usecase.SelfServiceUseCase;
import com.github.jenkaby.bikerental.users.web.dto.ChangePasswordRequest;
import com.github.jenkaby.bikerental.users.web.dto.UserResponse;
import com.github.jenkaby.bikerental.users.web.mapper.UsersWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = OpenApiConfig.Tags.USERS)
@Slf4j
@RequiredArgsConstructor
class UserSelfController {

    private final SelfServiceUseCase selfService;
    private final UsersWebMapper mapper;
    private final JwtProperties jwtProperties;

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Returns the profile of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        var userId = currentUserId(jwt);
        log.info("[GET] Fetching profile for user {}", userId);
        var user = selfService.getById(userId);
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @PostMapping("/password")
    @Operation(summary = "Change own password", description = "Changes the authenticated user's password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Wrong current password or password policy violated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        var userId = currentUserId(jwt);
        log.info("[POST] Changing password for user {}", userId);
        selfService.changeOwnPassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString(jwtProperties.userIdClaim()));
    }
}
