package com.financialmanagement.expense.application.dto.wallet;

import java.math.BigDecimal;

public record WalletCurrencySummary(
        String currency,
        BigDecimal totalAssets,
        BigDecimal totalDebts,
        BigDecimal netAssets) {
}
