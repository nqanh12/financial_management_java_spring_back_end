package com.financialmanagement.expense.application.dto.group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExpenseGroupResponse(UUID id, String name, UUID createdByUserId, List<UUID> memberUserIds, Instant createdAt) {
}
