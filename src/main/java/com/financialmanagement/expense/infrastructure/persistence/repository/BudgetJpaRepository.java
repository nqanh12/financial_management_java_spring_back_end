package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.infrastructure.persistence.entity.BudgetEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, UUID> {

    List<BudgetEntity> findByUser_IdAndDeletedAtIsNullOrderByYearMonthDesc(UUID userId);

    Page<BudgetEntity> findByUser_IdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Optional<BudgetEntity> findByUser_IdAndCategory_IdAndYearMonthAndDeletedAtIsNull(
            UUID userId, UUID categoryId, String yearMonth);

    Optional<BudgetEntity> findByIdAndUser_IdAndDeletedAtIsNull(UUID id, UUID userId);
}
