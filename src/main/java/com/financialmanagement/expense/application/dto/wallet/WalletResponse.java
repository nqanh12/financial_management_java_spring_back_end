package com.financialmanagement.expense.application.dto.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String name,
        String currency,
        String description,
        BigDecimal openingBalance,
        BigDecimal currentBalance,
        String groupKey,
        String iconKey,
        Instant createdAt,
        Instant updatedAt) {
}
