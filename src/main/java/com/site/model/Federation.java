package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "federations")
@Getter @Setter
public class Federation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long federationId;


    private String name;
    private String abbreviation;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;


    @OneToMany(mappedBy = "federation")
    private List<Ranking> rankings = new ArrayList<>();
}
