package com.financialmanagement.expense.infrastructure.persistence.entity;

import com.financialmanagement.expense.domain.model.SplitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shared_expenses")
public class SharedExpenseEntity extends BaseAuditableSoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroupEntity group;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paid_by_user_id", nullable = false)
    private UserEntity paidBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false, length = 32)
    private SplitType splitType;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(length = 2000)
    private String note;

    @OneToMany(mappedBy = "sharedExpense")
    private List<SharedExpenseAllocationEntity> allocations = new ArrayList<>();
}
