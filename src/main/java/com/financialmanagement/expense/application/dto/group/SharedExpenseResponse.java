package com.financialmanagement.expense.application.dto.group;

import com.financialmanagement.expense.domain.model.SplitType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SharedExpenseResponse(
        UUID id,
        UUID groupId,
        BigDecimal totalAmount,
        UUID paidByUserId,
        SplitType splitType,
        LocalDate expenseDate,
        String note,
        List<MemberSplitResponse> allocations) {
}
