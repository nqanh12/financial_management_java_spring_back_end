package com.financialmanagement.expense;

import com.financialmanagement.expense.config.TestReportingCacheConfig;
import com.financialmanagement.expense.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
@ActiveProfiles("test")
@Import(TestReportingCacheConfig.class)
class ExpenseApplicationTests extends PostgresIntegrationTest {

    @Test
    void contextLoads() {}
}
