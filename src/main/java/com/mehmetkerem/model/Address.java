package com.mehmetkerem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "addresses")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String country;

    private String city;

    private String district;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "user_id")
    private Long userId;
}
