package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profiles_user_id", columnNames = {"user_id"})
})
@Getter @Setter
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


    private String name;
    private String surname;


    @Column(length = 2000)
    private String description; // bio or additional info


    private String avatarUrl; // URL to avatar image
}
