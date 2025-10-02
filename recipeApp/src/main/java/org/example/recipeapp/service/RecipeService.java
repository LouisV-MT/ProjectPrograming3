package org.example.recipeapp.service;

import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final CuisineRepository cuisineRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final UserRepository userRepository;


    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository, CuisineRepository cuisineRepository, IngredientRepository ingredientRepository, RecipeIngredientRepository recipeIngredientRepository, UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.cuisineRepository = cuisineRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.userRepository = userRepository;
    }

    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }
}
