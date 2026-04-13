package com.financialmanagement.expense.application.dto.group;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddGroupMemberRequest(@NotNull UUID userId) {
}
