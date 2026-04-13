package com.financialmanagement.expense.infrastructure.security;

import com.financialmanagement.expense.application.dto.auth.OauthLoginExchangePayload;
import com.financialmanagement.expense.application.dto.user.UserResponse;
import com.financialmanagement.expense.application.port.out.OAuthLoginExchangeStore;
import com.financialmanagement.expense.application.port.out.UserAccountPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserAccountPort userAccountPort;
    private final OAuthLoginExchangeStore oauthLoginExchangeStore;

    @Value("${app.oauth2.frontend-redirect-url:http://localhost:3000/oauth2/callback}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String sub = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");
        if (email == null) {
            email = oauthUser.getName();
        }
        UserResponse user = userAccountPort.upsertFromOAuth(email, sub, name, picture);
        var payload = new OauthLoginExchangePayload(user.id(), user.email(), user.role());
        String code = oauthLoginExchangeStore.createCode(payload);
        char sep = frontendRedirectUrl.contains("?") ? '&' : '?';
        String url = frontendRedirectUrl + sep + "oauth_code=" + code;
        getRedirectStrategy().sendRedirect(request, response, url);
    }
}
