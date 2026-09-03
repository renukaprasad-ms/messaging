package com.messaging.company.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "company_profiles", indexes = {
        @Index(name = "idx_company_profile_company_id", columnList = "company_id")
})
public class CompanyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    @Setter
    private Company company;

    @Column(name = "legal_name", nullable = false, length = 200)
    @Setter
    private String legalName;

    @Column(name = "website", length = 255)
    @Setter
    private String website;

    @Column(name = "business_email", length = 150)
    @Setter
    private String businessEmail;

    @Column(name = "business_phone", length = 50)
    @Setter
    private String businessPhone;

    @Column(name = "industry", length = 100)
    @Setter
    private String industry;

    @Column(name = "registration_number", length = 100)
    @Setter
    private String registrationNumber;

    @Column(name = "tax_id", length = 100)
    @Setter
    private String taxId;
}
