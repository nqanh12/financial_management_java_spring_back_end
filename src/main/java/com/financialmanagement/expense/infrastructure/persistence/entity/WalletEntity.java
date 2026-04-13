package com.financialmanagement.expense.infrastructure.persistence.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "wallets")
public class WalletEntity extends BaseAuditableSoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(length = 500)
    private String description;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "group_key", nullable = false, length = 64)
    private String groupKey = "CASH";

    @Column(name = "icon_key", length = 128)
    private String iconKey;
}
