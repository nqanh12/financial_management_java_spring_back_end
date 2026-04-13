package com.financialmanagement.expense.application.dto.report;

import java.math.BigDecimal;

public record DashboardSummaryResponse(String yearMonth, BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal net) {
}
