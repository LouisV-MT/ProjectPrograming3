package org.example.recipeapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MealDBDto {
    private List<Meal> meals;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meal {
        @JsonProperty("idMeal")
        public String id;

        @JsonProperty("strMeal")
        public String name;

        @JsonProperty("strCategory")
        public String category;

        @JsonProperty("strArea")
        public String area;

        @JsonProperty("strInstructions")
        public String instructions;

        @JsonProperty("strMealThumb")
        public String imageUrl;

        @JsonProperty("strYoutube")
        public String videoUrl;

        @JsonProperty("strSource")
        public String sourceUrl;

        @JsonProperty("strIngredient1")
        public String ingredient1;

        @JsonProperty("strIngredient2")
        public String ingredient2;

        @JsonProperty("strIngredient3")
        public String ingredient3;

        @JsonProperty("strIngredient4")
        public String ingredient4;

        @JsonProperty("strIngredient5")
        public String ingredient5;

        @JsonProperty("strIngredient6")
        public String ingredient6;

        @JsonProperty("strIngredient7")
        public String ingredient7;

        @JsonProperty("strIngredient8")
        public String ingredient8;

        @JsonProperty("strIngredient9")
        public String ingredient9;

        @JsonProperty("strIngredient10")
        public String ingredient10;

        @JsonProperty("strIngredient11")
        public String ingredient11;

        @JsonProperty("strIngredient12")
        public String ingredient12;

        @JsonProperty("strIngredient13")
        public String ingredient13;

        @JsonProperty("strIngredient14")
        public String ingredient14;

        @JsonProperty("strIngredient15")
        public String ingredient15;

        @JsonProperty("strIngredient16")
        public String ingredient16;

        @JsonProperty("strIngredient17")
        public String ingredient17;

        @JsonProperty("strIngredient18")
        public String ingredient18;

        @JsonProperty("strIngredient19")
        public String ingredient19;

        @JsonProperty("strIngredient20")
        public String ingredient20;

        @JsonProperty("strMeasure1")
        public String measure1;

        @JsonProperty("strMeasure2")
        public String measure2;

        @JsonProperty("strMeasure3")
        public String measure3;

        @JsonProperty("strMeasure4")
        public String measure4;

        @JsonProperty("strMeasure5")
        public String measure5;

        @JsonProperty("strMeasure6")
        public String measure6;

        @JsonProperty("strMeasure7")
        public String measure7;

        @JsonProperty("strMeasure8")
        public String measure8;

        @JsonProperty("strMeasure9")
        public String measure9;

        @JsonProperty("strMeasure10")
        public String measure10;

        @JsonProperty("strMeasure11")
        public String measure11;

        @JsonProperty("strMeasure12")
        public String measure12;

        @JsonProperty("strMeasure13")
        public String measure13;

        @JsonProperty("strMeasure14")
        public String measure14;

        @JsonProperty("strMeasure15")
        public String measure15;

        @JsonProperty("strMeasure16")
        public String measure16;

        @JsonProperty("strMeasure17")
        public String measure17;

        @JsonProperty("strMeasure18")
        public String measure18;

        @JsonProperty("strMeasure19")
        public String measure19;

        @JsonProperty("strMeasure20")
        public String measure20;
    }
}
