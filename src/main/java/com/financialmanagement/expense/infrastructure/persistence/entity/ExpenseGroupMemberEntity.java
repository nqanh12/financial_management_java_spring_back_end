package com.financialmanagement.expense.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "expense_group_members")
public class ExpenseGroupMemberEntity {

    @EmbeddedId
    private ExpenseGroupMemberId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private ExpenseGroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class ExpenseGroupMemberId implements Serializable {
        private UUID groupId;
        private UUID userId;
    }
}
