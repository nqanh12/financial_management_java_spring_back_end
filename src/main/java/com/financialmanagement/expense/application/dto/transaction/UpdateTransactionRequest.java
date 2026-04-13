package com.financialmanagement.expense.application.dto.transaction;

import com.financialmanagement.expense.domain.model.TransactionDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequest(
        @NotNull UUID walletId,
        @NotNull UUID categoryId,
        @NotNull @DecimalMin("0.0001") BigDecimal amount,
        @NotNull TransactionDirection direction,
        @NotNull LocalDate transactionDate,
        @Size(max = 2000) String note,
        @Size(max = 128) String externalReference) {
}
