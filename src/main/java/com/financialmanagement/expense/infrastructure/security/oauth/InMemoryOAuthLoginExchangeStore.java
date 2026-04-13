package com.financialmanagement.expense.infrastructure.security.oauth;

import com.financialmanagement.expense.application.dto.auth.OauthLoginExchangePayload;
import com.financialmanagement.expense.application.port.out.OAuthLoginExchangeStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOAuthLoginExchangeStore implements OAuthLoginExchangeStore {

    private final Duration exchangeTtl;
    private final Map<String, Holder> map = new ConcurrentHashMap<>();

    public InMemoryOAuthLoginExchangeStore(Duration exchangeTtl) {
        this.exchangeTtl = exchangeTtl;
    }

    @Override
    public String createCode(OauthLoginExchangePayload payload) {
        String code = UUID.randomUUID().toString();
        map.put(code, new Holder(payload, Instant.now().plus(exchangeTtl)));
        return code;
    }

    @Override
    public Optional<OauthLoginExchangePayload> consume(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        Holder h = map.remove(code);
        if (h == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(h.expiresAt)) {
            return Optional.empty();
        }
        return Optional.of(h.payload);
    }

    private record Holder(OauthLoginExchangePayload payload, Instant expiresAt) {}
}
