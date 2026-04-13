package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.infrastructure.persistence.entity.CategoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findByUser_IdAndDeletedAtIsNullOrderByNameAsc(UUID userId);

    Optional<CategoryEntity> findByIdAndUser_IdAndDeletedAtIsNull(UUID id, UUID userId);
}
