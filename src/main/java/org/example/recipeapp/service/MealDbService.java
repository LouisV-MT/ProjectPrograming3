package org.example.recipeapp.service;

import jakarta.transaction.Transactional;
import org.example.recipeapp.domain.*;
import org.example.recipeapp.dto.MealDBDto;
import org.example.recipeapp.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;


@Service
public class MealDbService {
    private final WebClient mealDbWebClient;
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final CuisineRepository cuisineRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public MealDbService(@Qualifier("mealDbWebClient") WebClient mealDbWebClient, RecipeRepository recipeRepository, CategoryRepository categoryRepository, CuisineRepository cuisineRepository, IngredientRepository ingredientRepository, RecipeIngredientRepository recipeIngredientRepository) {
        this.mealDbWebClient = mealDbWebClient;
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.cuisineRepository = cuisineRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    @Transactional
    public Recipe importRecipeById(String mealId, User author) {
        //double check to see if the recipe already exist
        if (recipeRepository.existsByExternalId(mealId)) {
            System.out.println("WARN: Recipe with external id " + mealId + " already exists");
            return null;
        }
        try {
            MealDBDto detailedRecipeDto = mealDbWebClient.get()
                    .uri("/lookup.php?i={id}", mealId)
                    .retrieve()
                    .bodyToMono(MealDBDto.class)
                    .block();
            if (detailedRecipeDto != null && detailedRecipeDto.getMeals() != null && !detailedRecipeDto.getMeals().isEmpty()) {
                return saveRecipeFromMealDb(detailedRecipeDto.getMeals().getFirst(), author);
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to import recipe with ID " + mealId + ": " + e.getMessage());
        }
        return null;
    }

    private Recipe saveRecipeFromMealDb(MealDBDto.Meal mealData, User author) {
        Category category = categoryRepository.findByNameIgnoreCase(mealData.getCategory()).orElseGet(() -> categoryRepository.save(new Category(mealData.getCategory())));
        Cuisine cuisine = cuisineRepository.findByNameIgnoreCase(mealData.getArea()).orElseGet(() -> cuisineRepository.save(new Cuisine(mealData.getArea())));

        Recipe recipe = new Recipe();
        recipe.setExternalId(mealData.getId());
        recipe.setName(mealData.getName());
        recipe.setCategory(category);
        recipe.setCuisine(cuisine);
        recipe.setInstructions(mealData.getInstructions());
        recipe.setImageUrl(mealData.getImageUrl());
        recipe.setVideoUrl(mealData.getVideoUrl());
        recipe.setSourceUrl(mealData.getSourceUrl());
        recipe.setAuthor(author);

        for (int i = 1; i <= 20; i++) {
            try {
                Field ingredientField = mealData.getClass().getField("ingredient" + i);
                Field measureField = mealData.getClass().getField("measure" + i);
                String ingredientName = (String) ingredientField.get(mealData);
                String measurement = (String) measureField.get(mealData);

                if (ingredientName != null && !ingredientName.isBlank()) {
                    System.out.println("Processing ingredient: " + ingredientName);
                    Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(ingredientName)
                            .orElseGet(() -> ingredientRepository.save(new Ingredient(ingredientName.trim())));


                    RecipeIngredient recipeIngredient = new RecipeIngredient();
                    recipeIngredient.setId(new RecipeIngredientId());
                    recipeIngredient.setRecipe(recipe);
                    recipeIngredient.setIngredient(ingredient);
                    recipeIngredient.setMeasurement(measurement);
                    recipe.getRecipeIngredients().add(recipeIngredient);

                }
            } catch (NoSuchFieldException e) {
                break;
            } catch (Exception e) {
                System.err.println("An error occurred while processing ingredients: " + e.getMessage());
                break;
            }
        }
        Recipe finalRecipe= recipeRepository.save(recipe);
        System.out.println("Saved recipe: " + finalRecipe.getName());
        return finalRecipe;
    }
}
