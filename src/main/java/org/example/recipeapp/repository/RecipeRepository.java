package org.example.recipeapp.repository;

import org.example.recipeapp.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    Optional <Recipe> findByName(String name);
    List<Recipe>  findByCategoryName(String categoryName);
    boolean existsByExternalId(String externalId);

    List<Recipe> findByNameContainingIgnoreCase(String searchText);

    @Query(value = "SELECT * FROM recipes ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Recipe> findRandom();
}
