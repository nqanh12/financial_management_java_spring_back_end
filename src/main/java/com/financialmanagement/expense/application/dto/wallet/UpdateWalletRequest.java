package com.financialmanagement.expense.application.dto.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateWalletRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(min = 3, max = 3) String currency,
        @Size(max = 500) String description,
        BigDecimal openingBalance,
        @Size(max = 64) String groupKey,
        @Size(max = 128) String iconKey) {
}
