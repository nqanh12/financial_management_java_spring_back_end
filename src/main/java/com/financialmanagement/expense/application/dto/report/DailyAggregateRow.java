package com.financialmanagement.expense.application.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyAggregateRow(LocalDate date, BigDecimal income, BigDecimal expense) {
}
