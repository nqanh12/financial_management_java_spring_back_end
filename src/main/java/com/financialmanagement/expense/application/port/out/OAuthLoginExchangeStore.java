package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.auth.OauthLoginExchangePayload;
import java.util.Optional;

public interface OAuthLoginExchangeStore {

    /**
     * Persists payload and returns a single-use opaque code for the frontend to exchange for a JWT.
     */
    String createCode(OauthLoginExchangePayload payload);

    Optional<OauthLoginExchangePayload> consume(String code);
}
