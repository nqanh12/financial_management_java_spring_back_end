package com.financialmanagement.expense.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialmanagement.expense.application.port.out.OAuthLoginExchangeStore;
import com.financialmanagement.expense.infrastructure.security.oauth.InMemoryOAuthLoginExchangeStore;
import com.financialmanagement.expense.infrastructure.security.oauth.RedisOAuthLoginExchangeStore;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class OAuthLoginExchangeConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    OAuthLoginExchangeStore oauthLoginExchangeStoreRedis(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.oauth2.exchange-ttl:PT2M}") Duration exchangeTtl) {
        return new RedisOAuthLoginExchangeStore(stringRedisTemplate, objectMapper, exchangeTtl);
    }

    @Bean
    @ConditionalOnMissingBean(OAuthLoginExchangeStore.class)
    OAuthLoginExchangeStore oauthLoginExchangeStoreInMemory(
            @Value("${app.oauth2.exchange-ttl:PT2M}") Duration exchangeTtl) {
        return new InMemoryOAuthLoginExchangeStore(exchangeTtl);
    }
}
