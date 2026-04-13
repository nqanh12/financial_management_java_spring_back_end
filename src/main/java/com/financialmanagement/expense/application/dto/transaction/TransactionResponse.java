package com.financialmanagement.expense.application.dto.transaction;

import com.financialmanagement.expense.domain.model.TransactionDirection;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID walletId,
        UUID categoryId,
        BigDecimal amount,
        TransactionDirection direction,
        LocalDate transactionDate,
        String note,
        String externalReference,
        Instant createdAt,
        Instant updatedAt) {
}
