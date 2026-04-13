package com.financialmanagement.expense.application.dto.user;

import com.financialmanagement.expense.domain.model.UserRole;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String avatarUrl,
        UserRole role) {
}
