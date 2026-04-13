package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.infrastructure.persistence.entity.ExpenseGroupEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseGroupJpaRepository extends JpaRepository<ExpenseGroupEntity, UUID> {

    @Query(
            """
            SELECT DISTINCT g FROM ExpenseGroupEntity g
            JOIN g.members m
            WHERE g.deletedAt IS NULL AND m.user.id = :userId
            ORDER BY g.createdAt DESC
            """)
    List<ExpenseGroupEntity> findActiveGroupsForUser(@Param("userId") UUID userId);

    @Query(
            """
            SELECT DISTINCT g FROM ExpenseGroupEntity g
            LEFT JOIN FETCH g.members
            WHERE g.id = :id AND g.deletedAt IS NULL
            """)
    Optional<ExpenseGroupEntity> findByIdWithMembers(@Param("id") UUID id);
}
