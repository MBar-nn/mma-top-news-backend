package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fighters", indexes = {@Index(name = "idx_fighters_name", columnList = "name")})
@Getter @Setter
public class Fighter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fighterId;


    private String name; // name or nickname


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country; // nullable


    @OneToMany(mappedBy = "fighter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RankingEntry> rankingEntries = new ArrayList<>();
}