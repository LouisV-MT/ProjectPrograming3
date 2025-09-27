package org.example.recipeapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "meal_plan_recipes", schema = "recipe_app")
public class MealPlanRecipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private org.example.recipeapp.domain.MealPlan mealPlan;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "recipe_id", nullable = false)
    private org.example.recipeapp.domain.Recipe recipe;

    @NotNull
    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

}