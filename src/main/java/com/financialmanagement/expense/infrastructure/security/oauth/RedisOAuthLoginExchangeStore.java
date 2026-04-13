package com.financialmanagement.expense.infrastructure.security.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialmanagement.expense.application.dto.auth.OauthLoginExchangePayload;
import com.financialmanagement.expense.application.port.out.OAuthLoginExchangeStore;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@RequiredArgsConstructor
public class RedisOAuthLoginExchangeStore implements OAuthLoginExchangeStore {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration exchangeTtl;

    private static String key(String code) {
        return "oauth:exchange:" + code;
    }

    @Override
    public String createCode(OauthLoginExchangePayload payload) {
        String code = UUID.randomUUID().toString();
        try {
            stringRedisTemplate
                    .opsForValue()
                    .set(key(code), objectMapper.writeValueAsString(payload), exchangeTtl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize OAuth exchange payload", e);
        }
        return code;
    }

    @Override
    public Optional<OauthLoginExchangePayload> consume(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String json = stringRedisTemplate.opsForValue().getAndDelete(key(code));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, OauthLoginExchangePayload.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize OAuth exchange payload", e);
            return Optional.empty();
        }
    }
}
