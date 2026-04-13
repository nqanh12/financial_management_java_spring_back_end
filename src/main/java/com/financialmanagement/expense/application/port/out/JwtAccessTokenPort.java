package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.domain.model.UserRole;
import java.util.UUID;

public interface JwtAccessTokenPort {

    String createAccessToken(UUID userId, String email, UserRole role);

    long getAccessTokenTtlSeconds();
}
