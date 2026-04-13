package com.financialmanagement.expense.application.service;

import com.financialmanagement.expense.application.dto.auth.OauthExchangeResponse;
import com.financialmanagement.expense.application.port.out.JwtAccessTokenPort;
import com.financialmanagement.expense.application.port.out.OAuthLoginExchangeStore;
import com.financialmanagement.expense.domain.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuthLoginExchangeStore oauthLoginExchangeStore;
    private final JwtAccessTokenPort jwtAccessTokenPort;

    public OauthExchangeResponse exchangeOAuthCode(String code) {
        var payload = oauthLoginExchangeStore
                .consume(code)
                .orElseThrow(() -> new BusinessRuleException("Invalid or expired OAuth exchange code"));
        String token = jwtAccessTokenPort.createAccessToken(
                payload.userId(), payload.email(), payload.role());
        return new OauthExchangeResponse(token, jwtAccessTokenPort.getAccessTokenTtlSeconds());
    }
}
