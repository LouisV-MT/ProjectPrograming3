package org.example.recipeapp.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    @Qualifier("mealDbWebClient")
    public WebClient mealDbWebclient() {
        return WebClient.builder()
                .baseUrl("https://www.themealdb.com/api/json/v1/1")
                .build();
    }

    @Bean
    @Qualifier("edamamWebClient")
    public WebClient edamamWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.edamam.com/")
                .build();
    }

    @Bean
    @Qualifier("nutritionixWebClient")
    public WebClient nutritionixWebClient() {
        return WebClient.builder()
                .baseUrl("https://trackapi.nutritionix.com/")
                .build();
    }
}
