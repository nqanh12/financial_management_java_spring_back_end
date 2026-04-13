package com.financialmanagement.expense.presentation.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialmanagement.expense.application.dto.auth.OauthLoginExchangePayload;
import com.financialmanagement.expense.application.port.out.OAuthLoginExchangeStore;
import com.financialmanagement.expense.config.TestReportingCacheConfig;
import com.financialmanagement.expense.domain.model.UserRole;
import com.financialmanagement.expense.support.PostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
@ActiveProfiles("test")
@Import(TestReportingCacheConfig.class)
class AuthOAuthExchangeIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OAuthLoginExchangeStore oauthLoginExchangeStore;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void oauthExchange_invalidCode_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth-exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("code", "no-such-code"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void oauthExchange_validCode_returnsToken() throws Exception {
        var payload = new OauthLoginExchangePayload(UUID.randomUUID(), "x@example.com", UserRole.USER);
        String code = oauthLoginExchangeStore.createCode(payload);

        mockMvc.perform(post("/api/v1/auth/oauth-exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("code", code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists());
    }
}
