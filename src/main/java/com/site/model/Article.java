package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles", indexes = {
        @Index(name = "idx_articles_user_id", columnList = "user_id"),
        @Index(name = "idx_articles_created_at", columnList = "created_at")
})
@Getter @Setter
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // author


    private String title;


    @Column(columnDefinition = "jsonb")
    private String content; // JSON content stored as text/jsonb


    private String imgUrl; // main image URL


    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    private Instant modifiedAt;


    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }


    @PreUpdate
    public void preUpdate() {
        modifiedAt = Instant.now();
    }
}
