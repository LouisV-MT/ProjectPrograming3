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

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository, CategoryRepository categoryRepository, CuisineRepository cuisineRepository, ImageStorageService imageStorageService) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.categoryRepository = categoryRepository;
        this.cuisineRepository = cuisineRepository;
        this.imageStorageService = imageStorageService;
    }

    public List<Recipe> filterRecipes(String category, String cuisine) {
        if ((category == null || category.isEmpty()) && (cuisine == null || cuisine.isEmpty())) {
            return findAll();
        }
        return recipeRepository.filterRecipes(category, cuisine);
    }

    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> findRecipeById(Integer id) {
        return recipeRepository.findById(id);
    }

    public List<Recipe> searchRecipes(String searchText) {
        return recipeRepository.findByNameContainingIgnoreCase(searchText);
    }

    public Optional<Recipe> findRandomRecipe() {
        return recipeRepository.findRandom();
    }

    public void addPresignedUrlsToRecipes(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            String imageUrl = recipe.getImageUrl();
            if (imageUrl == null || imageUrl.isBlank()) {
                continue;
            }
            if (imageUrl.contains(".s3.")) {
                try {
                    String objectKey = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
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
    public void deleteRecipe(Integer id) {
        Recipe recipe = recipeRepository.findById(id).orElse(null);
        if (recipe != null) {
            String imageUrl = recipe.getImageUrl();
            if (imageUrl != null && imageUrl.contains(".s3.")) {
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

    public void deleteRecipeImage(String imageUrl) {
        if (imageUrl != null && imageUrl.contains(".s3.")) {
            try {
                String objectKey = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                imageStorageService.delete(objectKey);
            } catch (Exception e) {
                System.err.println("Failed to delete image: " + imageUrl);
                e.printStackTrace();
            }
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
    public Recipe save(Recipe recipeFromForm, String newCategoryName, String newCuisineName) {

        final Recipe recipeToSave;

        if (recipeFromForm.getId() != null) {
            recipeToSave = recipeRepository.findById(recipeFromForm.getId())
                    .orElseThrow(() -> new IllegalStateException("Recipe not found for update."));

            recipeToSave.setName(recipeFromForm.getName());
            recipeToSave.setInstructions(recipeFromForm.getInstructions());
            recipeToSave.setVideoUrl(recipeFromForm.getVideoUrl());
            recipeToSave.setSourceUrl(recipeFromForm.getSourceUrl());
            if (recipeFromForm.getImageUrl() != null) {
                recipeToSave.setImageUrl(recipeFromForm.getImageUrl());
            }
            recipeToSave.setAuthor(recipeFromForm.getAuthor());
        } else {
            recipeToSave = recipeFromForm;
        }

        if (recipeFromForm.getCategory() != null && recipeFromForm.getCategory().getId() == null && newCategoryName != null && !newCategoryName.trim().isEmpty()) {
            Category category = categoryRepository.findByNameIgnoreCase(newCategoryName.trim())
                    .orElseGet(() -> categoryRepository.save(new Category(newCategoryName.trim())));
            recipeToSave.setCategory(category);
        } else {
            recipeToSave.setCategory(recipeFromForm.getCategory());
        }

        if (recipeFromForm.getCuisine() != null && recipeFromForm.getCuisine().getId() == null && newCuisineName != null && !newCuisineName.trim().isEmpty()) {
            Cuisine cuisine = cuisineRepository.findByNameIgnoreCase(newCuisineName.trim())
                    .orElseGet(() -> cuisineRepository.save(new Cuisine(newCuisineName.trim())));
            recipeToSave.setCuisine(cuisine);
        } else {
            recipeToSave.setCuisine(recipeFromForm.getCuisine());
        }

        List<RecipeIngredient> formIngredients = new ArrayList<>(recipeFromForm.getRecipeIngredients());
        recipeToSave.getRecipeIngredients().clear();
        recipeRepository.flush();

        for (RecipeIngredient formRi : formIngredients) {
            Ingredient formIngredient = formRi.getIngredient();
            if (formIngredient != null && formIngredient.getName() != null && !formIngredient.getName().trim().isEmpty()) {

                Ingredient ingredientToUse = ingredientRepository
                        .findByNameIgnoreCase(formIngredient.getName().trim())
                        .orElseGet(() -> ingredientRepository.save(new Ingredient(formIngredient.getName().trim())));

                RecipeIngredient newRi = new RecipeIngredient();
                newRi.setId(new RecipeIngredientId());
                newRi.setIngredient(ingredientToUse);
                newRi.setMeasurement(formRi.getMeasurement());
                newRi.setRecipe(recipeToSave);

                recipeToSave.getRecipeIngredients().add(newRi);
            }
        }

        return recipeRepository.save(recipeToSave);
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

    public Optional<Category> findCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    public Optional<Cuisine> findCuisineById(Integer id) {
        return cuisineRepository.findById(id);
    }
}