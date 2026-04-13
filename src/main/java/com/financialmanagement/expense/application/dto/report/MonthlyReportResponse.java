package com.financialmanagement.expense.application.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReportResponse(
        String yearMonth,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        List<CategoryBreakdownRow> breakdownByCategory,
        List<DailyAggregateRow> dailyAggregates) {
}
