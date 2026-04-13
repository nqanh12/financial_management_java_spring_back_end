package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.budget.BudgetAlertResponse;
import com.financialmanagement.expense.application.dto.budget.BudgetResponse;
import com.financialmanagement.expense.application.dto.budget.UpsertBudgetRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BudgetPort {

    List<BudgetResponse> listByUser(UUID userId);

    Page<BudgetResponse> listByUserPaged(UUID userId, Pageable pageable);

    BudgetResponse upsert(UUID userId, UpsertBudgetRequest request);

    void softDelete(UUID userId, UUID budgetId);

    Optional<BudgetResponse> findByUserCategoryMonth(UUID userId, UUID categoryId, String yearMonth);

    BigDecimal sumExpenseForCategoryInMonth(UUID userId, UUID categoryId, LocalDate monthStart, LocalDate monthEnd);

    BudgetAlertResponse saveAlert(
            UUID userId, UUID categoryId, String yearMonth, BigDecimal spent, BigDecimal limit, String message);

    List<BudgetAlertResponse> listAlerts(UUID userId);

    Page<BudgetAlertResponse> listAlertsPaged(UUID userId, Pageable pageable);
}
