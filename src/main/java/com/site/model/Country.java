package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "countries", indexes = {@Index(name = "idx_countries_iso", columnList = "iso_code")})
@Getter @Setter
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long countryId;


    private String name;


    @Column(length = 3)
    private String isoCode;
}
