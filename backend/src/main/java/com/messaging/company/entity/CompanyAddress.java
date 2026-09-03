package com.messaging.company.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "company_addresses", indexes = {
        @Index(name = "idx_company_address_company_id", columnList = "company_id")
})
public class CompanyAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @Setter
    private Company company;

    @Column(name = "address_line_1", nullable = false, length = 255)
    @Setter
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    @Setter
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    @Setter
    private String city;

    @Column(name = "state", length = 100)
    @Setter
    private String state;

    @Column(name = "postal_code", length = 30)
    @Setter
    private String postalCode;

    @Column(name = "country", nullable = false, length = 100)
    @Setter
    private String country;
}
