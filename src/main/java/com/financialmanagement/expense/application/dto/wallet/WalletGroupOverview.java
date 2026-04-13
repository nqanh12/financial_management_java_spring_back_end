package com.financialmanagement.expense.application.dto.wallet;

import java.util.List;

public record WalletGroupOverview(
        String groupKey,
        List<WalletCurrencySummary> groupSummary,
        List<WalletOverviewItem> wallets) {
}
