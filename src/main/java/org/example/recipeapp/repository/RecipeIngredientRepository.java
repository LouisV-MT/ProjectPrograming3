package org.example.recipeapp.repository;

import org.example.recipeapp.domain.RecipeIngredient;
import org.example.recipeapp.domain.RecipeIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, RecipeIngredientId> {
}
