package com.financialmanagement.expense.infrastructure.persistence.adapter;

import com.financialmanagement.expense.application.dto.budget.BudgetAlertResponse;
import com.financialmanagement.expense.application.dto.budget.BudgetResponse;
import com.financialmanagement.expense.application.dto.budget.UpsertBudgetRequest;
import com.financialmanagement.expense.application.port.out.BudgetPort;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import com.financialmanagement.expense.infrastructure.persistence.entity.BudgetAlertEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.BudgetEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.CategoryEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.UserEntity;
import com.financialmanagement.expense.infrastructure.persistence.mapper.BudgetAlertEntityMapper;
import com.financialmanagement.expense.infrastructure.persistence.mapper.BudgetEntityMapper;
import com.financialmanagement.expense.infrastructure.persistence.repository.BudgetAlertJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.BudgetJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.CategoryJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.TransactionJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.UserJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BudgetAdapter implements BudgetPort {

    private final BudgetJpaRepository budgetJpaRepository;
    private final BudgetAlertJpaRepository budgetAlertJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;
    private final BudgetEntityMapper budgetEntityMapper;
    private final BudgetAlertEntityMapper budgetAlertEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> listByUser(UUID userId) {
        return budgetJpaRepository.findByUser_IdAndDeletedAtIsNullOrderByYearMonthDesc(userId).stream()
                .map(budgetEntityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BudgetResponse> listByUserPaged(UUID userId, Pageable pageable) {
        return budgetJpaRepository.findByUser_IdAndDeletedAtIsNull(userId, pageable).map(budgetEntityMapper::toResponse);
    }

    @Override
    @Transactional
    public BudgetResponse upsert(UUID userId, UpsertBudgetRequest request) {
        UserEntity user = userJpaRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CategoryEntity category = categoryJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Optional<BudgetEntity> existing =
                budgetJpaRepository.findByUser_IdAndCategory_IdAndYearMonthAndDeletedAtIsNull(
                        userId, request.categoryId(), request.yearMonth());
        BudgetEntity e = existing.orElseGet(BudgetEntity::new);
        if (e.getId() == null) {
            e.setUser(user);
            e.setCategory(category);
            e.setYearMonth(request.yearMonth());
        }
        e.setLimitAmount(request.limitAmount());
        return budgetEntityMapper.toResponse(budgetJpaRepository.save(e));
    }

    @Override
    @Transactional
    public void softDelete(UUID userId, UUID budgetId) {
        BudgetEntity e = budgetJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        e.softDelete();
        budgetJpaRepository.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BudgetResponse> findByUserCategoryMonth(UUID userId, UUID categoryId, String yearMonth) {
        return budgetJpaRepository
                .findByUser_IdAndCategory_IdAndYearMonthAndDeletedAtIsNull(userId, categoryId, yearMonth)
                .map(budgetEntityMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumExpenseForCategoryInMonth(UUID userId, UUID categoryId, LocalDate monthStart, LocalDate monthEnd) {
        return transactionJpaRepository.sumOutForCategoryInRange(userId, categoryId, monthStart, monthEnd);
    }

    @Override
    @Transactional
    public BudgetAlertResponse saveAlert(
            UUID userId, UUID categoryId, String yearMonth, BigDecimal spent, BigDecimal limit, String message) {
        UserEntity user = userJpaRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CategoryEntity category = categoryJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        BudgetAlertEntity a = budgetAlertJpaRepository
                .findByUser_IdAndCategory_IdAndYearMonthAndDeletedAtIsNull(userId, categoryId, yearMonth)
                .orElseGet(BudgetAlertEntity::new);
        if (a.getId() == null) {
            a.setUser(user);
            a.setCategory(category);
            a.setYearMonth(yearMonth);
        }
        a.setSpentAmount(spent);
        a.setLimitAmount(limit);
        a.setMessage(message);
        return budgetAlertEntityMapper.toResponse(budgetAlertJpaRepository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetAlertResponse> listAlerts(UUID userId) {
        return budgetAlertJpaRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId).stream()
                .map(budgetAlertEntityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BudgetAlertResponse> listAlertsPaged(UUID userId, Pageable pageable) {
        return budgetAlertJpaRepository
                .findByUser_IdAndDeletedAtIsNull(userId, pageable)
                .map(budgetAlertEntityMapper::toResponse);
    }
}
