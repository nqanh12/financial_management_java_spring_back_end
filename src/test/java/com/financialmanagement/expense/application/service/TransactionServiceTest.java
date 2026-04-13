package com.financialmanagement.expense.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financialmanagement.expense.application.dto.category.CategoryResponse;
import com.financialmanagement.expense.application.dto.transaction.CreateTransactionRequest;
import com.financialmanagement.expense.application.port.out.BudgetPort;
import com.financialmanagement.expense.application.port.out.CategoryPort;
import com.financialmanagement.expense.application.port.out.ReportingCachePort;
import com.financialmanagement.expense.application.port.out.TransactionPort;
import com.financialmanagement.expense.domain.exception.BusinessRuleException;
import com.financialmanagement.expense.domain.model.CategoryType;
import com.financialmanagement.expense.domain.model.TransactionDirection;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionPort transactionPort;

    @Mock
    CategoryPort categoryPort;

    @Mock
    BudgetPort budgetPort;

    @Mock
    ReportingCachePort reportingCachePort;

    @InjectMocks
    TransactionService transactionService;

    @Test
    void createRejectsIncomeCategoryWithOutDirection() {
        UUID user = UUID.randomUUID();
        UUID cat = UUID.randomUUID();
        UUID wallet = UUID.randomUUID();
        when(categoryPort.findByUserAndId(user, cat))
                .thenReturn(Optional.of(new CategoryResponse(
                        cat, "Salary", CategoryType.INCOME, Instant.now(), Instant.now())));
        CreateTransactionRequest req = new CreateTransactionRequest(
                wallet, cat, new BigDecimal("100.00"), TransactionDirection.OUT, LocalDate.now(), null, null);
        assertThatThrownBy(() -> transactionService.create(user, req)).isInstanceOf(BusinessRuleException.class);
        verify(transactionPort, never()).create(any(), any());
    }

    @Test
    void createAcceptsIncomeWithInDirection() {
        UUID user = UUID.randomUUID();
        UUID cat = UUID.randomUUID();
        UUID wallet = UUID.randomUUID();
        when(categoryPort.findByUserAndId(user, cat))
                .thenReturn(Optional.of(new CategoryResponse(
                        cat, "Salary", CategoryType.INCOME, Instant.now(), Instant.now())));
        CreateTransactionRequest req = new CreateTransactionRequest(
                wallet, cat, new BigDecimal("100.00"), TransactionDirection.IN, LocalDate.now(), null, null);
        when(transactionPort.create(eq(user), eq(req)))
                .thenReturn(new com.financialmanagement.expense.application.dto.transaction.TransactionResponse(
                        UUID.randomUUID(),
                        wallet,
                        cat,
                        req.amount(),
                        req.direction(),
                        req.transactionDate(),
                        req.note(),
                        req.externalReference(),
                        Instant.now(),
                        Instant.now()));
        transactionService.create(user, req);
        verify(transactionPort).create(eq(user), eq(req));
        verify(reportingCachePort).evictUserReports(user);
    }
}
