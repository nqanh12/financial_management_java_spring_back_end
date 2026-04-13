package com.financialmanagement.expense.application.dto.budget;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BudgetResponse(
        UUID id, UUID categoryId, String yearMonth, BigDecimal limitAmount, Instant createdAt, Instant updatedAt) {
}
