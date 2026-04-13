package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.user.UserResponse;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountPort {

    Optional<UserResponse> findByGoogleSub(String googleSub);

    Optional<UserResponse> findById(UUID id);

    UserResponse upsertFromOAuth(String email, String googleSub, String displayName, String avatarUrl);
}
