package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.report.DashboardSummaryResponse;
import com.financialmanagement.expense.application.dto.report.MonthlyReportResponse;
import java.util.Optional;
import java.util.UUID;

public interface ReportingCachePort {

    Optional<DashboardSummaryResponse> getDashboard(UUID userId, String yearMonth);

    void putDashboard(UUID userId, String yearMonth, DashboardSummaryResponse value);

    Optional<MonthlyReportResponse> getMonthlyReport(UUID userId, String yearMonth);

    void putMonthlyReport(UUID userId, String yearMonth, MonthlyReportResponse value);

    void evictUserReports(UUID userId);
}
