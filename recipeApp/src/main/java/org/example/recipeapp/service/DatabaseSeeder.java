package org.example.recipeapp.service;

import jakarta.transaction.Transactional;
import org.example.recipeapp.repository.NutritionInfoRepository;
import org.example.recipeapp.domain.*;
import org.example.recipeapp.dto.EdamamDto;
import org.example.recipeapp.dto.EdamamRequestDto;
import org.example.recipeapp.dto.MealDBDto;
import org.example.recipeapp.repository.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Component
public class DatabaseSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    // Dependencies for TheMealDB
    private final WebClient mealDbWebClient;

    // Dependencies for Edamam
    private final WebClient edamamWebClient;
    @Value("${edamam.api.id}")
    private String edamamAppId;
    @Value("${edamam.api.key}")
    private String edamamAppKey;

    // Other dependencies
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final CuisineRepository cuisineRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final NutritionInfoRepository nutritionInfoRepository;

    public DatabaseSeeder(@Qualifier("mealDbWebClient") WebClient mealDbWebClient,
                          @Qualifier("edamamWebClient") WebClient edamamWebClient,
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository,
                          RecipeRepository recipeRepository,
                          CategoryRepository categoryRepository,
                          CuisineRepository cuisineRepository,
                          IngredientRepository ingredientRepository,
                          RecipeIngredientRepository recipeIngredientRepository,
                          NutritionInfoRepository nutritionInfoRepository) {
        this.mealDbWebClient = mealDbWebClient;
        this.edamamWebClient = edamamWebClient;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.cuisineRepository = cuisineRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.nutritionInfoRepository = nutritionInfoRepository;
    }

    @Override
    public void run(String... args)  {
        User adminUser= createAdminUserIfThereIsNone();
        if (recipeRepository.count() >0){
            log.info("There is already database. Skipping seeding");
            return;
        }

        log.info("Seeding database...");
        List<String> recipeIdsToImport= List.of(
                "52772", "52977", "53049", "52855", // Various
                "52978", "52874", "52834", "53026", // Beef
                "52959", "52819", "52944", "53043", // Seafood
                "52961", "52854", "52931", "52792", // Dessert
                "52803", "52893", "52904", "52785"  // Vegan/Vegetarian
        );

        recipeIdsToImport.forEach(id -> importRecipeById(id, adminUser));
        log.info("Database seeded successfully");
    }
    private User createAdminUserIfThereIsNone() {
        return userRepository.findByUsername("admin").orElseGet(() -> {
            log.info("Admin user not found, creating one...");
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@recipetoday.com");
            admin.setPassword(passwordEncoder.encode("adminpassword"));
            admin.setRole(Role.ADMIN);
            return userRepository.save(admin);
        });
    }


    @Transactional
    public void importRecipeById(String mealId, User author) {
        //double check to see if the recipe already exist
        if (recipeRepository.existsByExternalId(mealId)){
            log.warn("Recipe with external id {} already exists", mealId);
        return;
    }
        try{
            MealDBDto detailedRecipeDto = mealDbWebClient.get()
                    .uri("/lookup.php?i={id}",mealId)
                    .retrieve()
                    .bodyToMono(MealDBDto.class)
                    .block();
            if (detailedRecipeDto != null && detailedRecipeDto.getMeals() != null && !detailedRecipeDto.getMeals().isEmpty()) {
                saveRecipeFromMealDB(detailedRecipeDto.getMeals().get(0), author);
            }
        } catch (Exception e){
            log.error("Failed to import recipe with ID {}: {}", mealId, e.getMessage());
        }
    }

    @Transactional
    public void updateRecipeWithNutrition(Recipe recipe){
        log.info("Fetching nutrition info for recipe:{}", recipe.getName());

        List<String> ingredientsList = recipe.getRecipeIngredients().stream()
                .map(recipeIngredient -> recipeIngredient.getMeasurement()+" "+recipeIngredient.getIngredient().getName())
                .toList();

        if (ingredientsList.isEmpty()){
            log.warn("No ingredients found for recipe:{}", recipe.getName());
            return;
        }

        EdamamRequestDto requestBody=  new EdamamRequestDto(recipe.getName(), ingredientsList);
        EdamamDto nutritionDto= edamamWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/nutrition-details")
                        .queryParam("app_id",edamamAppId)
                        .queryParam("app_key",edamamAppKey)
                        .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(EdamamDto.class)
                .block();

        if (nutritionDto != null){
            NutritionInfo nutritionInfo= new NutritionInfo();
            nutritionInfo.setCalories(nutritionDto.getCalories());
            nutritionInfo.setTotalWeight(nutritionDto.getTotalWeight());
            nutritionInfo.setHealthLabels(new HashSet<>(nutritionDto.getHealthLabels()));

            Map<String,EdamamDto.NutrientInfo> nutrients= nutritionDto.getTotalNutrients();
            if (nutrients!=null){
                nutritionInfo.setProtein(nutrients.get("PROCNT").getQuantity());
                nutritionInfo.setFat(nutrients.get("FAT").getQuantity());
                nutritionInfo.setCarbs(nutrients.get("CHOCDF").getQuantity());
            }

            recipe.setNutritionInfo(nutritionInfo);
            recipeRepository.save(recipe);
            log.info("Recipe with ID {} updated with nutrition successfully", recipe.getName());

        }


    }
    private void saveRecipeFromMealDB(MealDBDto.Meal mealData, User author){
        Category category = categoryRepository.findCategoryByName(mealData.getCategory()).orElseGet(() -> new Category(mealData.getCategory()));
        Cuisine cuisine = cuisineRepository.findCuisineByName(mealData.getArea()).orElseGet(() -> new Cuisine(mealData.getArea()));

        Recipe recipe= new Recipe();
        recipe.setExternalId(mealData.getId());
        recipe.setName(mealData.getName());
        recipe.setCategory(category);
        recipe.setCuisine(cuisine);
        recipe.setInstructions(mealData.getInstructions());
        recipe.setImageUrl(mealData.getImageUrl());
        recipe.setVideoUrl(mealData.getVideoUrl());
        recipe.setSourceUrl(mealData.getSourceUrl());
        recipe.setAuthor(author);
        Recipe savedRecipe= recipeRepository.save(recipe);
        for (int i = 1; i <= 20; i++) {
            try {
                Field ingredientField = mealData.getClass().getField("ingredient" + i);
                Field measureField = mealData.getClass().getField("measure" + i);
                String ingredientName = (String) ingredientField.get(mealData);
                String measurement = (String) measureField.get(mealData);

                if (ingredientName != null && !ingredientName.isBlank()) {
                    Ingredient ingredient = ingredientRepository.findByName(ingredientName).orElseGet(() -> new Ingredient(ingredientName));
                    RecipeIngredient recipeIngredient = new RecipeIngredient();
                    recipeIngredient.setId(new RecipeIngredientId(savedRecipe.getId(), ingredient.getId()));
                    recipeIngredient.setRecipe(savedRecipe);
                    recipeIngredient.setIngredient(ingredient);
                    recipeIngredient.setMeasurement(measurement);
                    recipeIngredientRepository.save(recipeIngredient);
                }
            } catch (Exception e) {
                break;
            }
        }
        log.info("Saved recipe: {}", savedRecipe.getName());
        updateRecipeWithNutrition(savedRecipe);


    }

}
