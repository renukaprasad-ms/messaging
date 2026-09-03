package com.messaging.company.entity;

import com.messaging.role.entity.Role;
import com.messaging.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "company_memberships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_membership_user_company",
                        columnNames = {"company_id", "user_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_membership_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_membership_company_id",
                        columnList = "company_id"
                ),
                @Index(
                        name = "idx_membership_company_status",
                        columnList = "company_id,status"
                )
        }
)
public class CompanyMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false
    )
    private Company company;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
