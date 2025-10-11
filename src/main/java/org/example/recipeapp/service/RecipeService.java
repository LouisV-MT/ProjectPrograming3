package org.example.recipeapp.service;

import org.example.recipeapp.domain.*;
import org.example.recipeapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;
    private final CuisineRepository cuisineRepository;

    public List<Recipe> filterRecipes(String category, String cuisine){
        if ((category == null || category.isEmpty()) && (cuisine == null || cuisine.isEmpty())) {
            return findAll();
        }
        return recipeRepository.filterRecipes(category, cuisine);
    }

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository, CategoryRepository categoryRepository, CuisineRepository cuisineRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.categoryRepository = categoryRepository;
        this.cuisineRepository = cuisineRepository;
    }

    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> findRecipeById(Integer id){return recipeRepository.findById(id);}

    public List<Recipe> searchRecipes(String searchText){
        return recipeRepository.findByNameContainingIgnoreCase(searchText);
    }

    public Optional<Recipe> findRandomRecipe(){
        return recipeRepository.findRandom();
    }


    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Cuisine> findAllCuisines() {
        return cuisineRepository.findAll();
    }

    public List<Recipe> findByAuthor(User currentUser) {
        return recipeRepository.findAllByAuthor(currentUser);
    }
    @Transactional
    public Recipe save(Recipe recipe, String newCategoryName, String newCuisineName) {


        if (recipe.getCategory() != null && recipe.getCategory().getId() == null && newCategoryName != null && !newCategoryName.trim().isEmpty()) {
            Category category = categoryRepository.findByNameIgnoreCase(newCategoryName.trim())
                    .orElseGet(() -> categoryRepository.save(new Category(newCategoryName.trim())));
            recipe.setCategory(category);
        }


        if (recipe.getCuisine() != null && recipe.getCuisine().getId() == null && newCuisineName != null && !newCuisineName.trim().isEmpty()) {
            Cuisine cuisine = cuisineRepository.findByNameIgnoreCase(newCuisineName.trim())
                    .orElseGet(() -> cuisineRepository.save(new Cuisine(newCuisineName.trim())));
            recipe.setCuisine(cuisine);
        }


        List<RecipeIngredient> processedIngredients = new ArrayList<>();
        if (recipe.getRecipeIngredients() != null) {
            for (RecipeIngredient ri : recipe.getRecipeIngredients()) {
                Ingredient formIngredient = ri.getIngredient();
                if (formIngredient != null && formIngredient.getName() != null && !formIngredient.getName().trim().isEmpty()) {

                    Ingredient ingredientToUse = ingredientRepository
                            .findByNameIgnoreCase(formIngredient.getName().trim())
                            .orElseGet(() -> ingredientRepository.save(new Ingredient(formIngredient.getName().trim())));


                    ri.setIngredient(ingredientToUse);
                    ri.setRecipe(recipe);
                    processedIngredients.add(ri);
                }
            }
        }

        recipe.getRecipeIngredients().clear();
        recipe.getRecipeIngredients().addAll(processedIngredients);

        return recipeRepository.save(recipe);
    }
}
