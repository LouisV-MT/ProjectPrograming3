package org.example.recipeapp.service;

import org.example.recipeapp.domain.*;
import org.example.recipeapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;
    private final CuisineRepository cuisineRepository;
    private final ImageStorageService imageStorageService;

    public List<Recipe> filterRecipes(String category, String cuisine){
        if ((category == null || category.isEmpty()) && (cuisine == null || cuisine.isEmpty())) {
            return findAll();
        }
        return recipeRepository.filterRecipes(category, cuisine);
    }

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository, CategoryRepository categoryRepository, CuisineRepository cuisineRepository, ImageStorageService imageStorageService) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.categoryRepository = categoryRepository;
        this.cuisineRepository = cuisineRepository;
        this.imageStorageService = imageStorageService;
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

    public void addPresignedUrlsToRecipes(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            String imageUrl = recipe.getImageUrl();
            if (imageUrl == null || imageUrl.isBlank()){
                continue;
            }
            if (recipe.getImageUrl() != null && recipe.getImageUrl().contains(".s3.")) {
                try {
                    String objectKey = recipe.getImageUrl().substring(recipe.getImageUrl().lastIndexOf('/') + 1);
                    String presignedUrl = imageStorageService.generatePresignedUrl(objectKey);
                    recipe.setPresignedImageUrl(presignedUrl);
                } catch (Exception e) {
                    System.err.println("Error generating presigned URL for recipe " + recipe.getId() + ": " + e.getMessage());
                    recipe.setPresignedImageUrl(null);
                }
                } else {
                recipe.setPresignedImageUrl(imageUrl);
            }
        }
    }

    @Transactional
    public void deleteRecipe(Integer id){
        Recipe recipe= recipeRepository.findById(id).orElse(null);
        if(recipe != null){
            String imageUrl = recipe.getImageUrl();
            if (imageUrl == null && imageUrl.contains(".s3.")) {
                try {
                    String objectKey = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                    imageStorageService.delete(objectKey);
                } catch (Exception e) {
                    System.err.println("Error deleting image from S3: " + e.getMessage());
                }
            }
            recipeRepository.delete(recipe);
        }
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
                    ri.setId(new RecipeIngredientId());
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
    public List<String> parseInstructions(String instructions) {
        if (instructions == null || instructions.isBlank()) {
            return Collections.emptyList();
        }

        String stepPattern = "(?i)\\s*(step|\\d+)[\\s.:?-]*\\d*";

        boolean looksLikeSteps = instructions.matches("(?s).*\\n\\s*\\d+\\..*") ||
                instructions.toLowerCase().contains("step");

        if (looksLikeSteps) {
            return Arrays.stream(instructions.split(stepPattern))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        return Arrays.stream(instructions.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
