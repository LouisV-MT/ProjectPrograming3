package org.example.recipeapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;


@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Getter
@Setter

public class RecipeIngredientId implements Serializable {
    private static final long serialVersionUID = -1998596979252132345L;

    @NotNull
    @Column(name = "recipe_id", nullable = false)
    private Integer recipeId;

    @NotNull
    @Column(name = "ingredient_id", nullable = false)
    private Integer ingredientId;


}
