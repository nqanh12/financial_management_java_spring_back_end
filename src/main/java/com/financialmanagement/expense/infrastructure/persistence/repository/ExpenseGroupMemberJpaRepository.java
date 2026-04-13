package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.infrastructure.persistence.entity.ExpenseGroupMemberEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.ExpenseGroupMemberEntity.ExpenseGroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseGroupMemberJpaRepository extends JpaRepository<ExpenseGroupMemberEntity, ExpenseGroupMemberId> {
}
