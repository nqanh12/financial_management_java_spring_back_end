package com.financialmanagement.expense.application.dto.auth;

import com.financialmanagement.expense.domain.model.UserRole;
import java.util.UUID;

public record OauthLoginExchangePayload(UUID userId, String email, UserRole role) {}
