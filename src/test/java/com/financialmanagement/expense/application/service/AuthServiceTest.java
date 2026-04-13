package com.financialmanagement.expense.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financialmanagement.expense.application.dto.auth.OauthLoginExchangePayload;
import com.financialmanagement.expense.application.port.out.JwtAccessTokenPort;
import com.financialmanagement.expense.application.port.out.OAuthLoginExchangeStore;
import com.financialmanagement.expense.domain.exception.BusinessRuleException;
import com.financialmanagement.expense.domain.model.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private OAuthLoginExchangeStore oauthLoginExchangeStore;

    @Mock
    private JwtAccessTokenPort jwtAccessTokenPort;

    @InjectMocks
    private AuthService authService;

    @Test
    void exchangeOAuthCode_invalid_throws() {
        when(oauthLoginExchangeStore.consume("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.exchangeOAuthCode("bad"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    void exchangeOAuthCode_valid_returnsToken() {
        UUID id = UUID.randomUUID();
        var payload = new OauthLoginExchangePayload(id, "a@b.com", UserRole.USER);
        when(oauthLoginExchangeStore.consume("code-1")).thenReturn(Optional.of(payload));
        when(jwtAccessTokenPort.createAccessToken(eq(id), eq("a@b.com"), eq(UserRole.USER)))
                .thenReturn("jwt-here");
        when(jwtAccessTokenPort.getAccessTokenTtlSeconds()).thenReturn(3600L);

        var res = authService.exchangeOAuthCode("code-1");

        assertThat(res.accessToken()).isEqualTo("jwt-here");
        assertThat(res.expiresIn()).isEqualTo(3600L);
        verify(oauthLoginExchangeStore).consume("code-1");
    }
}
