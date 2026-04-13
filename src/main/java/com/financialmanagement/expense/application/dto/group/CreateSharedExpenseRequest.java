package com.financialmanagement.expense.application.dto.group;

import com.financialmanagement.expense.domain.model.SplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateSharedExpenseRequest(
        @NotNull @DecimalMin("0.0001") BigDecimal totalAmount,
        @NotNull UUID paidByUserId,
        @NotNull SplitType splitType,
        @NotNull LocalDate expenseDate,
        @Size(max = 2000) String note,
        List<MemberSplitRequest> customSplits) {
}
