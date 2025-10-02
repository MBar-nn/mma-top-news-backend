package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ranking_entries", indexes = {
        @Index(name = "idx_ranking_entries_ranking_id", columnList = "ranking_id"),
        @Index(name = "idx_ranking_entries_fighter_id", columnList = "fighter_id")
})
@Getter @Setter
public class RankingEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entryId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ranking_id", nullable = false)
    private Ranking ranking;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fighter_id", nullable = false)
    private Fighter fighter;


    @Column(nullable = false)
    private Integer rankPosition;


    private Integer prevRankPosition;
}
