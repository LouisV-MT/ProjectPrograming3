package org.example.recipeapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdamamDto {
    private int calories;
    private double totalWeight;
    private List<String> dietLabels;
    private List<String> healthLabels;
    private List<String> cautions;
    private Map<String,NutrientInfo> totalNutrients;
    private Map<String,NutrientInfo> totalNutrientsKCal;
    private Map<String,NutrientInfo> totalDaily;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NutrientInfo{
        private String label;
        private double quantity;
        private String unit;

    }
}
