package com.site.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weight_categories")
@Getter @Setter
public class WeightCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weightCategoryId;


    private String name;
    private Double limitKg;


    @OneToMany(mappedBy = "weightCategory")
    private List<Ranking> rankings = new ArrayList<>();
}
