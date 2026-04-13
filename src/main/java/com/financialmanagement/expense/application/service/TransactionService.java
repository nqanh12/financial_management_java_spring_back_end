package com.financialmanagement.expense.application.service;

import com.financialmanagement.expense.application.dto.transaction.CreateTransactionRequest;
import com.financialmanagement.expense.application.dto.transaction.TransactionResponse;
import com.financialmanagement.expense.application.dto.transaction.UpdateTransactionRequest;
import com.financialmanagement.expense.application.port.out.BudgetPort;
import com.financialmanagement.expense.application.port.out.CategoryPort;
import com.financialmanagement.expense.application.port.out.ReportingCachePort;
import com.financialmanagement.expense.application.port.out.TransactionPort;
import com.financialmanagement.expense.domain.exception.BusinessRuleException;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import com.financialmanagement.expense.domain.model.CategoryType;
import com.financialmanagement.expense.domain.model.TransactionDirection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionPort transactionPort;
    private final CategoryPort categoryPort;
    private final BudgetPort budgetPort;
    private final ReportingCachePort reportingCachePort;

    @Transactional(readOnly = true)
    public List<TransactionResponse> list(UUID userId) {
        return transactionPort.listByUser(userId);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> listPaged(UUID userId, Pageable pageable) {
        return transactionPort.listByUserPaged(userId, pageable);
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(UUID userId, UUID transactionId) {
        return transactionPort
                .findByUserAndId(userId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    @Transactional
    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        validateCategoryDirection(request.categoryId(), userId, request.direction());
        TransactionResponse t = transactionPort.create(userId, request);
        reportingCachePort.evictUserReports(userId);
        evaluateBudget(userId, request.categoryId(), request.transactionDate());
        return t;
    }

    @Transactional
    public TransactionResponse update(UUID userId, UUID transactionId, UpdateTransactionRequest request) {
        validateCategoryDirection(request.categoryId(), userId, request.direction());
        TransactionResponse t = transactionPort.update(userId, transactionId, request);
        reportingCachePort.evictUserReports(userId);
        evaluateBudget(userId, request.categoryId(), request.transactionDate());
        return t;
    }

    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        TransactionResponse existing = get(userId, transactionId);
        transactionPort.softDelete(userId, transactionId);
        reportingCachePort.evictUserReports(userId);
        evaluateBudget(userId, existing.categoryId(), existing.transactionDate());
    }

    private void validateCategoryDirection(UUID categoryId, UUID userId, TransactionDirection direction) {
        var cat = categoryPort
                .findByUserAndId(userId, categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (cat.type() == CategoryType.INCOME && direction != TransactionDirection.IN) {
            throw new BusinessRuleException("Income categories only allow IN direction");
        }
        if (cat.type() == CategoryType.EXPENSE && direction != TransactionDirection.OUT) {
            throw new BusinessRuleException("Expense categories only allow OUT direction");
        }
    }

    private void evaluateBudget(UUID userId, UUID categoryId, LocalDate txDate) {
        var cat = categoryPort.findByUserAndId(userId, categoryId).orElse(null);
        if (cat == null || cat.type() != CategoryType.EXPENSE) {
            return;
        }
        YearMonth ym = YearMonth.from(txDate);
        String key = ym.toString();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        budgetPort
                .findByUserCategoryMonth(userId, categoryId, key)
                .ifPresent(b -> {
                    BigDecimal spent = budgetPort.sumExpenseForCategoryInMonth(userId, categoryId, start, end);
                    if (spent.compareTo(b.limitAmount()) > 0) {
                        budgetPort.saveAlert(
                                userId,
                                categoryId,
                                key,
                                spent,
                                b.limitAmount(),
                                "Budget exceeded for category in " + key);
                    }
                });
    }
}
