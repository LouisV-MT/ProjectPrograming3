package org.example.recipeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecipeAppApplication {

    public static void main(String[] args) {
        System.out.println("My B2 Bucket is: " + System.getenv("B2_BUCKET_NAME"));


        SpringApplication.run(RecipeAppApplication.class, args);
    }

}
