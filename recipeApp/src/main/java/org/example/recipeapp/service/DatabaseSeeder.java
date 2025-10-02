package org.example.recipeapp.service;

import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.repository.RecipeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RecipeRepository recipeRepository;
    private final UserService userService;
    private final MealDbService mealDbService;
    private final NutritionService nutritionService;

    public DatabaseSeeder(RecipeRepository recipeRepository, UserService userService, MealDbService mealDbService, NutritionService nutritionService) {
        this.recipeRepository = recipeRepository;
        this.userService = userService;
        this.mealDbService = mealDbService;
        this.nutritionService = nutritionService;
    }

    @Override
    public void run(String... args) {
        if (recipeRepository.count() > 0) {
            System.out.println("Database already contains data. Skipping seeding.");
            return;
        }

        System.out.println("Starting database seeding...");
        User adminUser = userService.findOrCreateAdminUser();

        List<String> recipeIdsToImport = List.of(
                "52772", "52977", "53049", "52855", "52978", "52874", "52834", "53026",
                "52959", "52819", "52944", "53043", "52961", "52854", "52931", "52792",
                "52803", "52893", "52904", "52785"
        );

        recipeIdsToImport.forEach(id -> {

            Recipe newRecipe = mealDbService.importRecipeById(id, adminUser);

            if (newRecipe != null) {
                nutritionService.updateRecipeWithNutrition(newRecipe);
            }
        });

        System.out.println("Database seeding complete.");
    }
}