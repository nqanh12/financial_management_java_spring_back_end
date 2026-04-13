package com.financialmanagement.expense.presentation.rest;

import com.financialmanagement.expense.application.dto.auth.OauthExchangeRequest;
import com.financialmanagement.expense.application.dto.auth.OauthExchangeResponse;
import com.financialmanagement.expense.application.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @SecurityRequirements
    @PostMapping("/oauth-exchange")
    public OauthExchangeResponse oauthExchange(@Valid @RequestBody OauthExchangeRequest request) {
        return authService.exchangeOAuthCode(request.code());
    }
}
