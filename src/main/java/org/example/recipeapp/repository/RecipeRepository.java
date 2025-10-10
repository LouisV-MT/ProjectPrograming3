package org.example.recipeapp.repository;

import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT DISTINCT c.name FROM categories c ORDER BY c.name", nativeQuery = true)
    List<String> findAllCategories();

    @Query(value = "SELECT DISTINCT c.name FROM cuisines c ORDER BY c.name", nativeQuery = true)
    List<String> findAllCuisines();

    @Query(value = "SELECT r.* FROM recipes r " +
            "LEFT JOIN categories cat ON r.category_id = cat.id " +
            "LEFT JOIN cuisines c ON r.cuisine_id = c.id " +
            "WHERE (:category IS NULL OR :category = '' OR cat.name = :category) " +
            "AND (:cuisine IS NULL OR :cuisine = '' OR c.name = :cuisine)",
            nativeQuery = true)
    List<Recipe> filterRecipes(@Param("category") String category, @Param("cuisine") String cuisine);

    List<Recipe> findAllByAuthor(User currentUser);
}
