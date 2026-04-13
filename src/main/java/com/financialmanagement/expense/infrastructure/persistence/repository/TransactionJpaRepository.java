package com.financialmanagement.expense.infrastructure.persistence.repository;

import com.financialmanagement.expense.domain.model.TransactionDirection;
import com.financialmanagement.expense.infrastructure.persistence.entity.TransactionEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findByWallet_User_IdAndDeletedAtIsNullAndWallet_DeletedAtIsNullOrderByTransactionDateDesc(
            UUID userId);

    Page<TransactionEntity> findByWallet_User_IdAndDeletedAtIsNullAndWallet_DeletedAtIsNull(UUID userId, Pageable pageable);

    Optional<TransactionEntity> findByIdAndWallet_User_IdAndDeletedAtIsNullAndWallet_DeletedAtIsNull(
            UUID id, UUID userId);

    @Query(
            """
            SELECT COALESCE(SUM(CASE WHEN t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.IN
                THEN t.amount
                WHEN t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.OUT
                THEN -t.amount
                END), 0)
            FROM TransactionEntity t
            WHERE t.wallet.id = :walletId AND t.deletedAt IS NULL
            """)
    BigDecimal sumSignedAmountForWallet(@Param("walletId") UUID walletId);

    @Query(
            """
            SELECT t.wallet.id as walletId, COALESCE(SUM(CASE
                WHEN t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.IN
                    THEN t.amount
                WHEN t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.OUT
                    THEN -t.amount
                END), 0) as netAmount
            FROM TransactionEntity t
            WHERE t.deletedAt IS NULL AND t.wallet.id IN :walletIds
            GROUP BY t.wallet.id
            """)
    List<WalletNetRow> sumSignedAmountGroupedByWallet(@Param("walletIds") Collection<UUID> walletIds);

    interface WalletNetRow {
        UUID getWalletId();

        BigDecimal getNetAmount();
    }

    @Query(
            """
            SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t
            JOIN t.wallet w
            WHERE w.user.id = :userId
            AND t.deletedAt IS NULL AND w.deletedAt IS NULL
            AND t.transactionDate >= :fromInclusive AND t.transactionDate <= :toInclusive
            AND t.direction = :direction
            """)
    BigDecimal sumByUserAndDateRangeAndDirection(
            @Param("userId") UUID userId,
            @Param("fromInclusive") LocalDate fromInclusive,
            @Param("toInclusive") LocalDate toInclusive,
            @Param("direction") TransactionDirection direction);

    @Query(
            """
            SELECT c.id as categoryId, c.name as categoryName,
            COALESCE(SUM(CASE WHEN t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.IN
                THEN t.amount ELSE 0 END), 0) as totalIncome,
            COALESCE(SUM(CASE WHEN t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.OUT
                THEN t.amount ELSE 0 END), 0) as totalExpense
            FROM TransactionEntity t
            JOIN t.category c
            JOIN t.wallet w
            WHERE w.user.id = :userId
            AND t.deletedAt IS NULL AND w.deletedAt IS NULL AND c.deletedAt IS NULL
            AND t.transactionDate >= :fromInclusive AND t.transactionDate <= :toInclusive
            GROUP BY c.id, c.name
            ORDER BY c.name
            """)
    List<CategoryBreakdownProjection> breakdownByCategory(
            @Param("userId") UUID userId,
            @Param("fromInclusive") LocalDate fromInclusive,
            @Param("toInclusive") LocalDate toInclusive);

    @Query(
            """
            SELECT t.transactionDate as day,
            COALESCE(SUM(CASE WHEN t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.IN
                THEN t.amount ELSE 0 END), 0) as income,
            COALESCE(SUM(CASE WHEN t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.OUT
                THEN t.amount ELSE 0 END), 0) as expense
            FROM TransactionEntity t
            JOIN t.wallet w
            WHERE w.user.id = :userId
            AND t.deletedAt IS NULL AND w.deletedAt IS NULL
            AND t.transactionDate >= :fromInclusive AND t.transactionDate <= :toInclusive
            GROUP BY t.transactionDate
            ORDER BY t.transactionDate
            """)
    List<DailyAggregateProjection> dailyAggregates(
            @Param("userId") UUID userId,
            @Param("fromInclusive") LocalDate fromInclusive,
            @Param("toInclusive") LocalDate toInclusive);

    @Query(
            """
            SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t
            JOIN t.wallet w
            WHERE w.user.id = :userId AND t.category.id = :categoryId
            AND t.direction = com.financialmanagement.expense.domain.model.TransactionDirection.OUT
            AND t.deletedAt IS NULL AND w.deletedAt IS NULL
            AND t.transactionDate >= :fromInclusive AND t.transactionDate <= :toInclusive
            """)
    BigDecimal sumOutForCategoryInRange(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("fromInclusive") LocalDate fromInclusive,
            @Param("toInclusive") LocalDate toInclusive);

    interface CategoryBreakdownProjection {
        UUID getCategoryId();

        String getCategoryName();

        BigDecimal getTotalIncome();

        BigDecimal getTotalExpense();
    }

    interface DailyAggregateProjection {
        LocalDate getDay();

        BigDecimal getIncome();

        BigDecimal getExpense();
    }
}
