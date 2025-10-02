package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rankings", indexes = {
        @Index(name = "idx_rankings_federation", columnList = "federation_id"),
        @Index(name = "idx_rankings_weight_category", columnList = "weight_category_id")
})
@Getter @Setter
public class Ranking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rankingId;


    private String title;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "federation_id")
    private Federation federation; // nullable


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weight_category_id")
    private WeightCategory weightCategory; // nullable


    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    private Instant modifiedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by")
    private User modifiedBy; // who last updated positions


    @OneToMany(mappedBy = "ranking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RankingEntry> entries = new ArrayList<>();


    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
    @PreUpdate
    public void preUpdate() { modifiedAt = Instant.now(); }
}
