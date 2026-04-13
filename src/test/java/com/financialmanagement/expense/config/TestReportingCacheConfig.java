package com.financialmanagement.expense.config;

import com.financialmanagement.expense.application.dto.report.DashboardSummaryResponse;
import com.financialmanagement.expense.application.dto.report.MonthlyReportResponse;
import com.financialmanagement.expense.application.port.out.ReportingCachePort;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestReportingCacheConfig {

    @Bean
    @Primary
    ReportingCachePort reportingCachePort() {
        return new ReportingCachePort() {
            @Override
            public Optional<DashboardSummaryResponse> getDashboard(UUID userId, String yearMonth) {
                return Optional.empty();
            }

            @Override
            public void putDashboard(UUID userId, String yearMonth, DashboardSummaryResponse value) {}

            @Override
            public Optional<MonthlyReportResponse> getMonthlyReport(UUID userId, String yearMonth) {
                return Optional.empty();
            }

            @Override
            public void putMonthlyReport(UUID userId, String yearMonth, MonthlyReportResponse value) {}

            @Override
            public void evictUserReports(UUID userId) {}
        };
    }
}
