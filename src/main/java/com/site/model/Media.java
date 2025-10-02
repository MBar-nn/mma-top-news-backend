package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "media", uniqueConstraints = {
        @UniqueConstraint(name = "uk_media_article_position", columnNames = {"article_id", "position"})
}, indexes = {@Index(name = "idx_media_article_id", columnList = "article_id")})
@Getter @Setter
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;


    @Column(nullable = false)
    private String url;


    private String alt;


    @Column(nullable = false)
    private Integer position; // unique per article
}