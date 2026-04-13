package com.financialmanagement.expense.application.service;

import com.financialmanagement.expense.application.dto.category.CategoryResponse;
import com.financialmanagement.expense.application.dto.category.CreateCategoryRequest;
import com.financialmanagement.expense.application.dto.category.UpdateCategoryRequest;
import com.financialmanagement.expense.application.port.out.CategoryPort;
import com.financialmanagement.expense.application.port.out.ReportingCachePort;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryPort categoryPort;
    private final ReportingCachePort reportingCachePort;

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(UUID userId) {
        return categoryPort.listByUser(userId);
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID userId, UUID categoryId) {
        return categoryPort
                .findByUserAndId(userId, categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
        CategoryResponse c = categoryPort.create(userId, request);
        reportingCachePort.evictUserReports(userId);
        return c;
    }

    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        CategoryResponse c = categoryPort.update(userId, categoryId, request);
        reportingCachePort.evictUserReports(userId);
        return c;
    }

    @Transactional
    public void delete(UUID userId, UUID categoryId) {
        categoryPort.softDelete(userId, categoryId);
        reportingCachePort.evictUserReports(userId);
    }
}
