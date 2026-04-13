package com.financialmanagement.expense.application.service;

import com.financialmanagement.expense.application.dto.budget.BudgetAlertResponse;
import com.financialmanagement.expense.application.dto.budget.BudgetResponse;
import com.financialmanagement.expense.application.dto.budget.UpsertBudgetRequest;
import com.financialmanagement.expense.application.port.out.BudgetPort;
import com.financialmanagement.expense.application.port.out.CategoryPort;
import com.financialmanagement.expense.application.port.out.ReportingCachePort;
import com.financialmanagement.expense.domain.exception.BusinessRuleException;
import com.financialmanagement.expense.domain.model.CategoryType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetPort budgetPort;
    private final CategoryPort categoryPort;
    private final ReportingCachePort reportingCachePort;

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(UUID userId) {
        return budgetPort.listByUser(userId);
    }

    @Transactional(readOnly = true)
    public Page<BudgetResponse> listPaged(UUID userId, Pageable pageable) {
        return budgetPort.listByUserPaged(userId, pageable);
    }

    @Transactional
    public BudgetResponse upsert(UUID userId, UpsertBudgetRequest request) {
        var cat = categoryPort
                .findByUserAndId(userId, request.categoryId())
                .orElseThrow(() -> new com.financialmanagement.expense.domain.exception.ResourceNotFoundException(
                        "Category not found"));
        if (cat.type() != CategoryType.EXPENSE) {
            throw new BusinessRuleException("Budgets apply to EXPENSE categories only");
        }
        BudgetResponse b = budgetPort.upsert(userId, request);
        reportingCachePort.evictUserReports(userId);
        return b;
    }

    @Transactional
    public void delete(UUID userId, UUID budgetId) {
        budgetPort.softDelete(userId, budgetId);
        reportingCachePort.evictUserReports(userId);
    }

    @Transactional(readOnly = true)
    public List<BudgetAlertResponse> listAlerts(UUID userId) {
        return budgetPort.listAlerts(userId);
    }

    @Transactional(readOnly = true)
    public Page<BudgetAlertResponse> listAlertsPaged(UUID userId, Pageable pageable) {
        return budgetPort.listAlertsPaged(userId, pageable);
    }
}
