package com.financialmanagement.expense.infrastructure.persistence.entity;

import com.financialmanagement.expense.domain.model.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity extends BaseAuditableSoftDeleteEntity {

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "google_sub", nullable = false, length = 255)
    private String googleSub;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;
}
