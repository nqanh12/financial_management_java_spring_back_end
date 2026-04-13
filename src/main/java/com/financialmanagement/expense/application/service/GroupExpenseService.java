package com.financialmanagement.expense.application.service;

import com.financialmanagement.expense.application.dto.group.CreateExpenseGroupRequest;
import com.financialmanagement.expense.application.dto.group.CreateSharedExpenseRequest;
import com.financialmanagement.expense.application.dto.group.ExpenseGroupResponse;
import com.financialmanagement.expense.application.dto.group.SharedExpenseResponse;
import com.financialmanagement.expense.application.port.out.GroupExpensePort;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupExpenseService {

    private final GroupExpensePort groupExpensePort;

    @Transactional
    public ExpenseGroupResponse createGroup(UUID userId, CreateExpenseGroupRequest request) {
        return groupExpensePort.createGroup(userId, request);
    }

    @Transactional(readOnly = true)
    public List<ExpenseGroupResponse> listGroups(UUID userId) {
        return groupExpensePort.listGroupsForUser(userId);
    }

    @Transactional(readOnly = true)
    public ExpenseGroupResponse getGroup(UUID userId, UUID groupId) {
        return groupExpensePort
                .getGroup(userId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    @Transactional
    public void addMember(UUID userId, UUID groupId, UUID memberUserId) {
        groupExpensePort.addMember(userId, groupId, memberUserId);
    }

    @Transactional(readOnly = true)
    public List<SharedExpenseResponse> listShared(UUID userId, UUID groupId) {
        return groupExpensePort.listSharedExpenses(userId, groupId);
    }

    @Transactional
    public SharedExpenseResponse createShared(UUID userId, UUID groupId, CreateSharedExpenseRequest request) {
        return groupExpensePort.createSharedExpense(userId, groupId, request);
    }

    @Transactional(readOnly = true)
    public SharedExpenseResponse getShared(UUID userId, UUID sharedId) {
        return groupExpensePort
                .getSharedExpense(userId, sharedId)
                .orElseThrow(() -> new ResourceNotFoundException("Shared expense not found"));
    }
}
