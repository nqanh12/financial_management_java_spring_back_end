package com.financialmanagement.expense.application.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OauthExchangeRequest(@NotBlank String code) {}
