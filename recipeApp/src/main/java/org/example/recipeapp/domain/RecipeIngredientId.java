package org.example.recipeapp.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class RecipeIngredientId implements Serializable {
    private static final long serialVersionUID = -6133446472293554319L;
}