package org.example.recipeapp.service;

import org.example.recipeapp.domain.NutritionInfo;
import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.dto.NutritionixRequestDto;
import org.example.recipeapp.dto.NutritionixResponseDto;
import org.example.recipeapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.stream.Collectors;

@Service
public class NutritionService {

    private final WebClient nutritionixWebClient;
    private final RecipeRepository recipeRepository;

    @Value("${nutritionix.api.id}")
    private String nutritionixAppId;

    @Value("${nutritionix.api.key}")
    private String nutritionixAppKey;

    public NutritionService(@Qualifier("nutritionixWebClient") WebClient nutritionixWebClient,
                            RecipeRepository recipeRepository) {
        this.nutritionixWebClient = nutritionixWebClient;
        this.recipeRepository = recipeRepository;
    }


    @Transactional
    public void updateRecipeWithNutrition(Recipe recipe) {
        System.out.println("Fetching nutrition data for recipe: " + recipe.getName());

        if (recipe.getRecipeIngredients() == null || recipe.getRecipeIngredients().isEmpty()) {
            System.out.println("WARN: No ingredients for recipe '" + recipe.getName() + "', skipping nutrition analysis.");
            return;
        }


        String query = recipe.getRecipeIngredients().stream()
                .map(ri -> ri.getMeasurement() + " " + ri.getIngredient().getName())
                .collect(Collectors.joining(","));

        System.out.println("Fetching nutrition data for query: \n" + query);
        NutritionixRequestDto requestBody = new NutritionixRequestDto(query);

        try {
            NutritionixResponseDto nutritionDto = nutritionixWebClient.post()
                    .uri("/v2/natural/nutrients")
                    .header("x-app-id", nutritionixAppId)
                    .header("x-app-key", nutritionixAppKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(NutritionixResponseDto.class)
                    .block();

            if (nutritionDto != null && nutritionDto.getFoods() != null) {
                System.out.println("Successfully fetched nutrition data for recipe: " + recipe.getName());

                double totalCalories = 0;
                double totalProtein = 0;
                double totalFat = 0;
                double totalCarbs = 0;

                for (NutritionixResponseDto.Food food : nutritionDto.getFoods()) {
                    totalCalories += food.getCalories() != null ? food.getCalories() : 0;
                    totalProtein += food.getProtein() != null ? food.getProtein() : 0;
                    totalFat += food.getTotalFat() != null ? food.getTotalFat() : 0;
                    totalCarbs += food.getTotalCarbohydrate() != null ? food.getTotalCarbohydrate() : 0;
                }


                NutritionInfo nutritionInfo = recipe.getNutritionInfo();
                if (nutritionInfo == null) {
                    nutritionInfo = new NutritionInfo();
                    nutritionInfo.setRecipe(recipe);
                }

                nutritionInfo.setCalories((int) Math.round(totalCalories));
                nutritionInfo.setProtein(totalProtein);
                nutritionInfo.setFat(totalFat);
                nutritionInfo.setCarbs(totalCarbs);

                recipe.setNutritionInfo(nutritionInfo);
                recipeRepository.save(recipe);

                System.out.println("Successfully saved nutrition info for recipe: " + recipe.getName());
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch nutrition data for recipe '" + recipe.getName() + "': " + e.getMessage());
        }
    }
}