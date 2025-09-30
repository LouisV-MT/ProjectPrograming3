package org.example.recipeapp.service;

import jakarta.transaction.Transactional;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.repository.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private static final Logger log= (Logger) LoggerFactory.getLogger(DatabaseSeeder.class);
    private final WebClient webClient;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final CuisineRepository cuisineRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public DatabaseSeeder(@Qualifier("mealDBWebClient") WebClient webClient, PasswordEncoder passwordEncoder, UserRepository userRepository, RecipeRepository recipeRepository, CategoryRepository categoryRepository, CuisineRepository cuisineRepository, IngredientRepository ingredientRepository, RecipeIngredientRepository recipeIngredientRepository) {
        this.webClient = webClient;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.cuisineRepository = cuisineRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    @Override
    @Transactional
    public void run(String... args)  {
        User adminUser= createAdminUserIfThereIsNone();
        if (recipeRepository.count() >0){
            log.info("There is already database. Skipping seeding");
            return;
        }

        log.info("Seeding database...");
        List<String> categoriesToFetch= Arrays.asList("Seafood","Beef","Vegan","Chicken","Dessert","Pork");
    }

}
