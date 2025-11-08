package com.site.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "expired_refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiredRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)   // relacja wiele-do-jednego
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt = OffsetDateTime.now();

    @Column(name = "expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @Column(name = "invalidated_at")
    private OffsetDateTime invalidatedAt = OffsetDateTime.now();

    @Column(name = "reason")
    private String reason = "expired";
}

