package com.messaging.company.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "company_verifications", indexes = {
        @Index(name = "idx_company_verification_company_id", columnList = "company_id"),
        @Index(name = "idx_company_verification_status", columnList = "verification_status"),
        @Index(name = "idx_company_verification_risk_level", columnList = "risk_level")
})
public class CompanyVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    @Setter
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    @Setter
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    @Setter
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "email_verified", nullable = false)
    @Setter
    private boolean emailVerified;

    @Column(name = "phone_verified", nullable = false)
    @Setter
    private boolean phoneVerified;

    @Column(name = "website_verified", nullable = false)
    @Setter
    private boolean websiteVerified;

    @Column(name = "reviewed_at")
    @Setter
    private Instant reviewedAt;

    @Column(name = "rejection_reason", length = 1000)
    @Setter
    private String rejectionReason;
}
