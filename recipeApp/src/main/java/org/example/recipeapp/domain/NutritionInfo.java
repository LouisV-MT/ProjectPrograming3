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
    private Integer recipeId;


    @MapsId
    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    private Integer calories;

    private Double totalWeight;

    private Double protein;

    private Double fat;

    private Double carbs;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name="recipe_health_labels",joinColumns = @JoinColumn(name="nutrition_info_recipe_id"))
    @Column(name="health_label")
    private Set<String> healthLabels = new HashSet<>();

}
