package com.financialmanagement.expense.config;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails fast in {@code prod} when OAuth/JWT secrets are missing or blank (never rely on baked-in defaults).
 */
public class ProdRequiredSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean prod =
                Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!prod) {
            return;
        }
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id", "");
        String clientSecret = environment.getProperty("spring.security.oauth2.client.registration.google.client-secret", "");
        String jwtSecret = environment.getProperty("app.jwt.secret", "");
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("prod profile requires non-blank GOOGLE_CLIENT_ID");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("prod profile requires non-blank GOOGLE_CLIENT_SECRET");
        }
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("prod profile requires non-blank JWT_SECRET");
        }
    }
}
