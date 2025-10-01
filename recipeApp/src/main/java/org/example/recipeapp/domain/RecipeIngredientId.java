package org.example.recipeapp.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class RecipeIngredientId implements Serializable {
    private Integer recipeId;
    private Integer ingredientId;
}