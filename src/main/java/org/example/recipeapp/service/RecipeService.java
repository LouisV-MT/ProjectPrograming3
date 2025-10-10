package org.example.recipeapp.service;

import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public List<Recipe> filterRecipes(String category, String cuisine){
        if ((category == null || category.isEmpty()) && (cuisine == null || cuisine.isEmpty())) {
            return findAll();
        }
        return recipeRepository.filterRecipes(category, cuisine);
    }

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
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

    public  Recipe save(Recipe recipe){
        return recipeRepository.save(recipe);
    }

    public List<String> findAllCategories() {
        return recipeRepository.findAllCategories();
    }

    public List<String> findAllCuisines() {
        return recipeRepository.findAllCuisines();
    }

    public List<Recipe> findByAuthor(User currentUser) {
        return recipeRepository.findAllByAuthor(currentUser);
    }
}
