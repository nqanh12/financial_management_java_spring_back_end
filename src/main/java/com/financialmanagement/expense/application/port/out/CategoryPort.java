package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.category.CategoryResponse;
import com.financialmanagement.expense.application.dto.category.CreateCategoryRequest;
import com.financialmanagement.expense.application.dto.category.UpdateCategoryRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryPort {

    List<CategoryResponse> listByUser(UUID userId);

    Optional<CategoryResponse> findByUserAndId(UUID userId, UUID categoryId);

    CategoryResponse create(UUID userId, CreateCategoryRequest request);

    CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request);

    void softDelete(UUID userId, UUID categoryId);
}
