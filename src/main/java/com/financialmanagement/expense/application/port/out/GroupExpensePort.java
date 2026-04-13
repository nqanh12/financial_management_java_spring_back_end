package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.group.CreateExpenseGroupRequest;
import com.financialmanagement.expense.application.dto.group.CreateSharedExpenseRequest;
import com.financialmanagement.expense.application.dto.group.ExpenseGroupResponse;
import com.financialmanagement.expense.application.dto.group.SharedExpenseResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupExpensePort {

    ExpenseGroupResponse createGroup(UUID creatorUserId, CreateExpenseGroupRequest request);

    List<ExpenseGroupResponse> listGroupsForUser(UUID userId);

    Optional<ExpenseGroupResponse> getGroup(UUID userId, UUID groupId);

    void addMember(UUID actorUserId, UUID groupId, UUID memberUserId);

    List<SharedExpenseResponse> listSharedExpenses(UUID userId, UUID groupId);

    SharedExpenseResponse createSharedExpense(UUID userId, UUID groupId, CreateSharedExpenseRequest request);

    Optional<SharedExpenseResponse> getSharedExpense(UUID userId, UUID sharedExpenseId);
}
