package com.financialmanagement.expense.application.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryBreakdownRow(UUID categoryId, String categoryName, BigDecimal totalIncome, BigDecimal totalExpense) {
}
