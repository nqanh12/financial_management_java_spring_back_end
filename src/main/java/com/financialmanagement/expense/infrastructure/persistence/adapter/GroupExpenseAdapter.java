package com.financialmanagement.expense.infrastructure.persistence.adapter;

import com.financialmanagement.expense.application.dto.group.CreateExpenseGroupRequest;
import com.financialmanagement.expense.application.dto.group.CreateSharedExpenseRequest;
import com.financialmanagement.expense.application.dto.group.ExpenseGroupResponse;
import com.financialmanagement.expense.application.dto.group.MemberSplitResponse;
import com.financialmanagement.expense.application.dto.group.SharedExpenseResponse;
import com.financialmanagement.expense.application.port.out.GroupExpensePort;
import com.financialmanagement.expense.domain.exception.BusinessRuleException;
import com.financialmanagement.expense.domain.exception.ForbiddenException;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import com.financialmanagement.expense.domain.model.EqualSplitCalculator;
import com.financialmanagement.expense.domain.model.SplitType;
import com.financialmanagement.expense.infrastructure.persistence.entity.ExpenseGroupEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.ExpenseGroupMemberEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.ExpenseGroupMemberEntity.ExpenseGroupMemberId;
import com.financialmanagement.expense.infrastructure.persistence.entity.SharedExpenseAllocationEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.SharedExpenseEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.UserEntity;
import com.financialmanagement.expense.infrastructure.persistence.repository.ExpenseGroupJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.ExpenseGroupMemberJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.SharedExpenseAllocationJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.SharedExpenseJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.UserJpaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GroupExpenseAdapter implements GroupExpensePort {

    private final ExpenseGroupJpaRepository expenseGroupJpaRepository;
    private final ExpenseGroupMemberJpaRepository expenseGroupMemberJpaRepository;
    private final SharedExpenseJpaRepository sharedExpenseJpaRepository;
    private final SharedExpenseAllocationJpaRepository sharedExpenseAllocationJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    @Transactional
    public ExpenseGroupResponse createGroup(UUID creatorUserId, CreateExpenseGroupRequest request) {
        UserEntity creator = userJpaRepository
                .findByIdAndDeletedAtIsNull(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ExpenseGroupEntity g = new ExpenseGroupEntity();
        g.setName(request.name());
        g.setCreatedBy(creator);
        g = expenseGroupJpaRepository.save(g);
        addMemberInternal(g, creator);
        return toGroupResponse(loadGroupWithMembers(g.getId()).orElse(g));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseGroupResponse> listGroupsForUser(UUID userId) {
        return expenseGroupJpaRepository.findActiveGroupsForUser(userId).stream()
                .map(this::toGroupResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExpenseGroupResponse> getGroup(UUID userId, UUID groupId) {
        return loadGroupWithMembers(groupId)
                .filter(g -> isMember(g, userId))
                .map(this::toGroupResponse);
    }

    @Override
    @Transactional
    public void addMember(UUID actorUserId, UUID groupId, UUID memberUserId) {
        ExpenseGroupEntity g = loadGroupWithMembers(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (!isMember(g, actorUserId)) {
            throw new ForbiddenException("Not a group member");
        }
        if (isMember(g, memberUserId)) {
            return;
        }
        UserEntity member = userJpaRepository
                .findByIdAndDeletedAtIsNull(memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        addMemberInternal(g, member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedExpenseResponse> listSharedExpenses(UUID userId, UUID groupId) {
        if (getGroup(userId, groupId).isEmpty()) {
            throw new ForbiddenException("Not a group member");
        }
        return sharedExpenseJpaRepository.findByGroupIdAndMember(groupId, userId).stream()
                .map(this::toSharedResponse)
                .toList();
    }

    @Override
    @Transactional
    public SharedExpenseResponse createSharedExpense(UUID userId, UUID groupId, CreateSharedExpenseRequest request) {
        ExpenseGroupEntity g = loadGroupWithMembers(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (!isMember(g, userId)) {
            throw new ForbiddenException("Not a group member");
        }
        if (!isMember(g, request.paidByUserId())) {
            throw new BusinessRuleException("Payer must be a group member");
        }
        List<UUID> memberIds = g.getMembers().stream()
                .map(m -> m.getUser().getId())
                .collect(Collectors.toCollection(ArrayList::new));
        memberIds = EqualSplitCalculator.mergeUnique(memberIds);

        Map<UUID, BigDecimal> splits;
        if (request.splitType() == SplitType.EQUAL) {
            splits = EqualSplitCalculator.equalSplit(memberIds, request.totalAmount());
        } else {
            if (request.customSplits() == null || request.customSplits().isEmpty()) {
                throw new BusinessRuleException("Custom splits required");
            }
            splits = new java.util.LinkedHashMap<>();
            BigDecimal sum = BigDecimal.ZERO;
            for (var row : request.customSplits()) {
                if (!isMember(g, row.userId())) {
                    throw new BusinessRuleException("Split user must be a group member: " + row.userId());
                }
                splits.put(row.userId(), row.amount());
                sum = sum.add(row.amount());
            }
            if (sum.compareTo(request.totalAmount()) != 0) {
                throw new BusinessRuleException("Custom splits must sum to total amount");
            }
        }

        UserEntity paidBy = userJpaRepository
                .findByIdAndDeletedAtIsNull(request.paidByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found"));

        SharedExpenseEntity se = new SharedExpenseEntity();
        se.setGroup(g);
        se.setTotalAmount(request.totalAmount());
        se.setPaidBy(paidBy);
        se.setSplitType(request.splitType());
        se.setExpenseDate(request.expenseDate());
        se.setNote(request.note());
        se = sharedExpenseJpaRepository.save(se);

        for (Map.Entry<UUID, BigDecimal> e : splits.entrySet()) {
            UserEntity u = userJpaRepository
                    .findByIdAndDeletedAtIsNull(e.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            SharedExpenseAllocationEntity a = new SharedExpenseAllocationEntity();
            a.setSharedExpense(se);
            a.setUser(u);
            a.setAmount(e.getValue());
            sharedExpenseAllocationJpaRepository.save(a);
        }

        return sharedExpenseJpaRepository
                .findByIdAndMemberWithAllocations(se.getId(), userId)
                .map(this::toSharedResponse)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SharedExpenseResponse> getSharedExpense(UUID userId, UUID sharedExpenseId) {
        return sharedExpenseJpaRepository
                .findByIdAndMemberWithAllocations(sharedExpenseId, userId)
                .map(this::toSharedResponse);
    }

    private Optional<ExpenseGroupEntity> loadGroupWithMembers(UUID groupId) {
        return expenseGroupJpaRepository.findByIdWithMembers(groupId);
    }

    private void addMemberInternal(ExpenseGroupEntity g, UserEntity user) {
        ExpenseGroupMemberEntity m = new ExpenseGroupMemberEntity();
        ExpenseGroupMemberId id = new ExpenseGroupMemberId();
        id.setGroupId(g.getId());
        id.setUserId(user.getId());
        m.setId(id);
        m.setGroup(g);
        m.setUser(user);
        expenseGroupMemberJpaRepository.save(m);
    }

    private boolean isMember(ExpenseGroupEntity g, UUID userId) {
        return g.getMembers().stream().anyMatch(m -> Objects.equals(m.getUser().getId(), userId));
    }

    private ExpenseGroupResponse toGroupResponse(ExpenseGroupEntity g) {
        List<UUID> ids = g.getMembers().stream().map(m -> m.getUser().getId()).toList();
        return new ExpenseGroupResponse(
                g.getId(), g.getName(), g.getCreatedBy().getId(), ids, g.getCreatedAt());
    }

    private SharedExpenseResponse toSharedResponse(SharedExpenseEntity s) {
        List<MemberSplitResponse> alloc =
                s.getAllocations() == null
                        ? List.of()
                        : s.getAllocations().stream()
                                .map(a -> new MemberSplitResponse(a.getUser().getId(), a.getAmount()))
                                .toList();
        return new SharedExpenseResponse(
                s.getId(),
                s.getGroup().getId(),
                s.getTotalAmount(),
                s.getPaidBy().getId(),
                s.getSplitType(),
                s.getExpenseDate(),
                s.getNote(),
                alloc);
    }
}
