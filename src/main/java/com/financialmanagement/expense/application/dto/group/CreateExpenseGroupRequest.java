package com.financialmanagement.expense.application.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExpenseGroupRequest(@NotBlank @Size(max = 255) String name) {
}
