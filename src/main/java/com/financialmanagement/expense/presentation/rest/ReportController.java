package com.financialmanagement.expense.presentation.rest;

import com.financialmanagement.expense.application.dto.report.DashboardSummaryResponse;
import com.financialmanagement.expense.application.dto.report.MonthlyReportResponse;
import com.financialmanagement.expense.application.service.ReportService;
import com.financialmanagement.expense.infrastructure.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reports")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public DashboardSummaryResponse dashboard(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(required = false) String month) {
        YearMonth ym = month != null && !month.isBlank() ? YearMonth.parse(month) : YearMonth.now();
        return reportService.dashboard(principal.userId(), ym);
    }

    @GetMapping("/monthly")
    public MonthlyReportResponse monthly(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(required = false) String month) {
        YearMonth ym = month != null && !month.isBlank() ? YearMonth.parse(month) : YearMonth.now();
        return reportService.monthly(principal.userId(), ym);
    }
}
