package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.infrastructure.persistence.entity.SharedExpenseAllocationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedExpenseAllocationJpaRepository extends JpaRepository<SharedExpenseAllocationEntity, UUID> {

    void deleteBySharedExpense_Id(UUID sharedExpenseId);

    List<SharedExpenseAllocationEntity> findBySharedExpense_Id(UUID sharedExpenseId);
}
