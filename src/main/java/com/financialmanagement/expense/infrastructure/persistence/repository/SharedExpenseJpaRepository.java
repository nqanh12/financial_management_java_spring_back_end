package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.infrastructure.persistence.entity.SharedExpenseEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SharedExpenseJpaRepository extends JpaRepository<SharedExpenseEntity, UUID> {

    @Query(
            """
            SELECT s FROM SharedExpenseEntity s
            JOIN s.group g
            JOIN g.members m
            WHERE s.id = :id AND s.deletedAt IS NULL AND m.user.id = :userId
            """)
    Optional<SharedExpenseEntity> findByIdAndMember(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query(
            """
            SELECT s FROM SharedExpenseEntity s
            JOIN FETCH s.allocations
            JOIN s.group g
            JOIN g.members m
            WHERE s.id = :id AND s.deletedAt IS NULL AND m.user.id = :userId
            """)
    Optional<SharedExpenseEntity> findByIdAndMemberWithAllocations(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query(
            """
            SELECT DISTINCT s FROM SharedExpenseEntity s
            LEFT JOIN FETCH s.allocations
            JOIN s.group g
            JOIN g.members m
            WHERE g.id = :groupId AND s.deletedAt IS NULL AND m.user.id = :userId
            ORDER BY s.expenseDate DESC
            """)
    List<SharedExpenseEntity> findByGroupIdAndMember(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
}
