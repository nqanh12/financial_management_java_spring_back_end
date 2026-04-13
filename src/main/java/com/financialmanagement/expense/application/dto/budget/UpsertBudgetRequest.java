package com.financialmanagement.expense.application.dto.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record UpsertBudgetRequest(
        @NotNull UUID categoryId,
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}") String yearMonth,
        @NotNull @DecimalMin("0.0001") BigDecimal limitAmount) {
}
