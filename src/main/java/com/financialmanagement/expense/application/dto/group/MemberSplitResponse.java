package com.financialmanagement.expense.application.dto.group;

import java.math.BigDecimal;
import java.util.UUID;

public record MemberSplitResponse(UUID userId, BigDecimal amount) {
}
