package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.infrastructure.persistence.entity.BudgetAlertEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetAlertJpaRepository extends JpaRepository<BudgetAlertEntity, UUID> {

    List<BudgetAlertEntity> findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

    Page<BudgetAlertEntity> findByUser_IdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Optional<BudgetAlertEntity> findByUser_IdAndCategory_IdAndYearMonthAndDeletedAtIsNull(
            UUID userId, UUID categoryId, String yearMonth);
}
