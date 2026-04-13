package com.financialmanagement.expense.application.dto.category;

import com.financialmanagement.expense.domain.model.CategoryType;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(UUID id, String name, CategoryType type, Instant createdAt, Instant updatedAt) {
}
