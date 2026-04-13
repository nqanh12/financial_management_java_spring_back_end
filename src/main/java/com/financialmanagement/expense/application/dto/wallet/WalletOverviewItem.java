package com.financialmanagement.expense.application.dto.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletOverviewItem(
        UUID id,
        String name,
        String currency,
        BigDecimal currentBalance,
        BigDecimal openingBalance,
        String groupKey,
        String iconKey,
        BigDecimal displayExchangeRate,
        Instant createdAt) {
}
