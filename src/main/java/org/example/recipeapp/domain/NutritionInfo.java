package org.example.recipeapp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name="nutrition_info")
public class NutritionInfo {
    @Id
    @Column(name = "recipe_id")
    private Integer id;


    @MapsId
    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    private Integer calories;

    private Double totalWeight;

    private Double protein;

    private Double fat;

    private Double carbs;


}
