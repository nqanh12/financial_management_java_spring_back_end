package com.financialmanagement.expense.application.dto.wallet;

import java.util.List;

public record WalletsOverviewResponse(
        List<WalletCurrencySummary> summary,
        List<WalletGroupOverview> groups) {
}
