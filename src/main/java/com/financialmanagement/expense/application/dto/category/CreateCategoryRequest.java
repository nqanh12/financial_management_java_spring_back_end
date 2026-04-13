package com.financialmanagement.expense.application.dto.category;

import com.financialmanagement.expense.domain.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 255) String name, @NotNull CategoryType type) {
}
