package com.financialmanagement.expense.application.dto.budget;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BudgetAlertResponse(
        UUID id,
        UUID categoryId,
        String yearMonth,
        BigDecimal spentAmount,
        BigDecimal limitAmount,
        String message,
        Instant createdAt) {
}
