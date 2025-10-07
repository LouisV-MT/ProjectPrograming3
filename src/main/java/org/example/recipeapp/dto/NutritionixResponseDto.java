package org.example.recipeapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NutritionixResponseDto {
    private List<Food> foods;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Food {
        @JsonProperty("food_name")
        private String foodName;

        @JsonProperty("nf_calories")
        private Double calories;

        @JsonProperty("nf_protein")
        private Double protein;

        @JsonProperty("nf_total_fat")
        private Double totalFat;

        @JsonProperty("nf_total_carbohydrate")
        private Double totalCarbohydrate;
    }
}

