package com.financialmanagement.expense.presentation.rest;

import com.financialmanagement.expense.application.dto.user.UserResponse;
import com.financialmanagement.expense.application.service.UserProfileService;
import com.financialmanagement.expense.infrastructure.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @Operation(summary = "Current user profile")
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal JwtUserPrincipal principal) {
        UUID userId = principal.userId();
        return userProfileService.get(userId);
    }
}
