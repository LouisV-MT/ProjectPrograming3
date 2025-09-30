package org.example.recipeapp.repository;

import org.example.recipeapp.domain.FavoriteRecipe;
import org.example.recipeapp.domain.FavoriteRecipeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRecipeRepository extends JpaRepository<FavoriteRecipe, FavoriteRecipeId> {
}
