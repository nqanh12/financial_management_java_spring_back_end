package com.financialmanagement.expense.application.dto.group;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record MemberSplitRequest(@NotNull UUID userId, @NotNull @DecimalMin("0") BigDecimal amount) {
}
