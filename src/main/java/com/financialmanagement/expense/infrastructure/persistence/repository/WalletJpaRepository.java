package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.infrastructure.persistence.entity.WalletEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, UUID> {

    List<WalletEntity> findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

    Optional<WalletEntity> findByIdAndUser_IdAndDeletedAtIsNull(UUID id, UUID userId);
}
