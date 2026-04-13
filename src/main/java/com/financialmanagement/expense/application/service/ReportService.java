package com.financialmanagement.expense.application.service;

import com.financialmanagement.expense.application.dto.report.DashboardSummaryResponse;
import com.financialmanagement.expense.application.dto.report.MonthlyReportResponse;
import com.financialmanagement.expense.application.port.out.ReportDataPort;
import com.financialmanagement.expense.application.port.out.ReportingCachePort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportDataPort reportDataPort;
    private final ReportingCachePort reportingCachePort;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboard(UUID userId, YearMonth yearMonth) {
        String key = yearMonth.toString();
        return reportingCachePort
                .getDashboard(userId, key)
                .orElseGet(() -> {
                    LocalDate start = yearMonth.atDay(1);
                    LocalDate end = yearMonth.atEndOfMonth();
                    BigDecimal income = reportDataPort.sumByDirection(userId, start, end, true);
                    BigDecimal expense = reportDataPort.sumByDirection(userId, start, end, false);
                    DashboardSummaryResponse dto =
                            new DashboardSummaryResponse(key, income, expense, income.subtract(expense));
                    reportingCachePort.putDashboard(userId, key, dto);
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse monthly(UUID userId, YearMonth yearMonth) {
        String key = yearMonth.toString();
        return reportingCachePort
                .getMonthlyReport(userId, key)
                .orElseGet(() -> {
                    LocalDate start = yearMonth.atDay(1);
                    LocalDate end = yearMonth.atEndOfMonth();
                    BigDecimal income = reportDataPort.sumByDirection(userId, start, end, true);
                    BigDecimal expense = reportDataPort.sumByDirection(userId, start, end, false);
                    MonthlyReportResponse dto = new MonthlyReportResponse(
                            key,
                            income,
                            expense,
                            reportDataPort.breakdownByCategory(userId, start, end),
                            reportDataPort.dailyAggregates(userId, start, end));
                    reportingCachePort.putMonthlyReport(userId, key, dto);
                    return dto;
                });
    }
}
