package org.example.recipeapp.service;

import org.example.recipeapp.domain.NutritionInfo;
import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.dto.EdamamDto;
import org.example.recipeapp.dto.EdamamRequestDto;
import org.example.recipeapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NutritionService {

    private final WebClient edamamWebClient;
    private final RecipeRepository recipeRepository;

    @Value("${edamam.api.id}")
    private String edamamAppId;

    @Value("${edamam.api.key}")
    private String edamamAppKey;

    public NutritionService(@Qualifier("edamamWebClient") WebClient edamamWebClient,
                            RecipeRepository recipeRepository) {
        this.edamamWebClient = edamamWebClient;
        this.recipeRepository = recipeRepository;
    }


    @Transactional
    public void updateRecipeWithNutrition(Recipe recipe) {
        System.out.println("Fetching nutrition data for recipe: " + recipe.getName());

        List<String> ingredientList = recipe.getRecipeIngredients().stream()
                .map(ri -> ri.getMeasurement() + " " + ri.getIngredient().getName())
                .collect(Collectors.toList());

        if (ingredientList.isEmpty()) {
            System.out.println("WARN: No ingredients for recipe '" + recipe.getName() + "', skipping nutrition analysis.");
            return;
        }

        EdamamRequestDto requestBody = new EdamamRequestDto(recipe.getName(), ingredientList);

        try {
            EdamamDto nutritionDto = edamamWebClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/nutrition-details")
                            .queryParam("app_id", edamamAppId)
                            .queryParam("app_key", edamamAppKey)
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(EdamamDto.class)
                    .block();

            if (nutritionDto != null) {
                NutritionInfo nutritionInfo = new NutritionInfo();
                nutritionInfo.setCalories(nutritionDto.getCalories());
                nutritionInfo.setTotalWeight(nutritionDto.getTotalWeight());

                if (nutritionDto.getHealthLabels() != null) {
                    nutritionInfo.setHealthLabels(new HashSet<>(nutritionDto.getHealthLabels()));
                }

                Map<String, EdamamDto.NutrientInfo> nutrients = nutritionDto.getTotalNutrients();
                if (nutrients != null) {
                    nutritionInfo.setFat(nutrients.containsKey("FAT") ? nutrients.get("FAT").getQuantity() : 0.0);
                    nutritionInfo.setCarbs(nutrients.containsKey("CHOCDF") ? nutrients.get("CHOCDF").getQuantity() : 0.0);
                    nutritionInfo.setProtein(nutrients.containsKey("PROCNT") ? nutrients.get("PROCNT").getQuantity() : 0.0);
                }

                recipe.setNutritionInfo(nutritionInfo);
                recipeRepository.save(recipe);

                System.out.println("Successfully saved nutrition info for recipe: " + recipe.getName());
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch nutrition data for recipe '" + recipe.getName() + "': " + e.getMessage());
        }
    }
}