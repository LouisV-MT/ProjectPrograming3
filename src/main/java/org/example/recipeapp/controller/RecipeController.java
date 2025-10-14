package org.example.recipeapp.controller;

import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.service.ImageStorageService;
import org.example.recipeapp.service.NutritionService;
import org.example.recipeapp.service.RecipeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final ImageStorageService imageStorageService;
    private final NutritionService nutritionService;

    public RecipeController(RecipeService recipeService, ImageStorageService imageStorageService, NutritionService nutritionService) {
        this.recipeService = recipeService;
        this.imageStorageService = imageStorageService;
        this.nutritionService = nutritionService;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }
    private String toTitleCase(String input) {
        if (input == null || input.isBlank()) return "";
        String[] parts = input.split("[_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

//    @GetMapping //List all recipes will move to admin controller
//    public String listRecipes(Model model){
//        List<Recipe> recipes = recipeService.findAll();
//        model.addAttribute("recipes",recipes);
//        return "recipes/list";
//    }

    @GetMapping("/{id}")
    public String showRecipe(@PathVariable Integer id, Model model){
        Optional<Recipe> recipeOptional= recipeService.findRecipeById(id);
        if(recipeOptional.isPresent()){
            Recipe recipe= recipeOptional.get();
            model.addAttribute("recipe",recipe);
            String imageUrl= recipe.getImageUrl();
            if (recipe.getInstructions() != null) {
                String[] instructionSteps = recipe.getInstructions().split("\\r?\\n");
                model.addAttribute("instructionSteps", instructionSteps);
            }
            recipeService.addPresignedUrlsToRecipes(List.of(recipe));
            return  "recipes/detail";
        } else {
            System.err.println("Recipe not found");
            return "redirect:/recipes";
        }
    }

    @GetMapping("/search")
    public String searchRecipes(@RequestParam("keyword") String keyword, Model model){
        List<Recipe> searchResults = recipeService.searchRecipes(keyword);
        recipeService.addPresignedUrlsToRecipes(searchResults);
        model.addAttribute("recipes", searchResults);
        model.addAttribute("keyword", keyword);
        model.addAttribute("title", "Search Results for " + keyword);
        return "recipes/list";

    }

    @GetMapping("/my-recipes")
    public String showMyRecipes(Model model, Authentication authentication){
        User currentUser = (User) authentication.getPrincipal();
        List<Recipe> myRecipes = recipeService.findByAuthor(currentUser);
        recipeService.addPresignedUrlsToRecipes(myRecipes);

        model.addAttribute("recipes", myRecipes);
        model.addAttribute("title", "My Recipes");
        return "recipes/list";
    }

    @GetMapping("/surprise")
    public String showSurprise(){
        Optional<Recipe> RandomRecipe = recipeService.findRandomRecipe();
        return RandomRecipe.map(recipe -> "redirect:/recipes/" + recipe.getId()).orElse("redirect:/recipes");
    }

    @GetMapping("/create")
    public String showCreateRecipeForm(Model model){
        model.addAttribute("recipe",new Recipe());
        model.addAttribute("categories", recipeService.findAllCategories());
        model.addAttribute("cuisines", recipeService.findAllCuisines());
        return "recipes/create-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditRecipeForm(@PathVariable Integer id, Model model, Authentication authentication, RedirectAttributes redirectAttributes){
        Optional<Recipe> recipeOptional = recipeService.findRecipeById(id);
        User currentUser= (User)  authentication.getPrincipal();
        if(recipeOptional.isPresent()){
            Recipe recipe = recipeOptional.get();
            if(recipe.getAuthor() == null || !recipe.getAuthor().getId().equals(currentUser.getId())){
                redirectAttributes.addFlashAttribute("errorMessage", "You do not have permission to edit this recipe.");
                return "redirect:/home";

            }
            model.addAttribute("recipe",recipe);
            model.addAttribute("categories", recipeService.findAllCategories());
            model.addAttribute("cuisines", recipeService.findAllCuisines());
            return "recipes/edit-form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Recipe not found.");
            return "redirect:/home";
        }
    }

    @PostMapping
    public String saveOrUpdateRecipe(@ModelAttribute Recipe recipe,
                                     @RequestParam("imageFile") MultipartFile imageFile,
                                     @RequestParam(name = "newCategoryName", required = false) String newCategoryName,
                                     @RequestParam(name = "newCuisineName", required = false) String newCuisineName,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) throws IOException {
        User currentUser = (User) authentication.getPrincipal();


        if(!imageFile.isEmpty()){
            String contentType= imageFile.getContentType();
            if(contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))){
                redirectAttributes.addFlashAttribute("errorMessage","Invalid image file type, only JPEG or PNG");
                return "redirect:" + (recipe.getId() != null ? "/recipes/edit/" + recipe.getId() : "/recipes/create");
            }
        }


        if (recipe.getId() != null) {
            // --- UPDATE LOGIC ---
            Recipe existingRecipe = recipeService.findRecipeById(recipe.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Recipe not found."));

            if (existingRecipe.getAuthor() == null || !existingRecipe.getAuthor().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You do not have permission to edit this recipe.");
                return "redirect:/recipes";
            }


            existingRecipe.setName(recipe.getName());
            existingRecipe.setInstructions(recipe.getInstructions());
            existingRecipe.setCategory(recipe.getCategory());
            existingRecipe.setCuisine(recipe.getCuisine());
            existingRecipe.setSourceUrl(recipe.getSourceUrl());
            existingRecipe.setVideoUrl(recipe.getVideoUrl());

            existingRecipe.setRecipeIngredients(recipe.getRecipeIngredients());

            if (!imageFile.isEmpty()) {
                String recipeName = toTitleCase(existingRecipe.getName());
                String author = toTitleCase(currentUser.getUsername());
                String extension = getFileExtension(imageFile.getOriginalFilename());
                String newFileName = "recipeOf" + recipeName + "By" + author + extension;

                String imageUrl = imageStorageService.upload(imageFile, newFileName);
                existingRecipe.setImageUrl(imageUrl);
            }

            Recipe updatedRecipe = recipeService.save(existingRecipe, newCategoryName, newCuisineName);
            nutritionService.updateRecipeWithNutrition(updatedRecipe);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe updated successfully.");
            return "redirect:/recipes/" + updatedRecipe.getId();

        } else {
            // --- CREATE LOGIC ---
            String imageUrl = null;
            if (!imageFile.isEmpty()) {
                String recipeName = toTitleCase(recipe.getName());
                String author = toTitleCase(currentUser.getUsername());
                String extension = getFileExtension(imageFile.getOriginalFilename());
                String newFileName = "recipeof" + recipeName + "By" + author + extension;
                imageUrl = imageStorageService.upload(imageFile, newFileName);
            }
            recipe.setAuthor(currentUser);
            recipe.setImageUrl(imageUrl);

            Recipe savedRecipe = recipeService.save(recipe, newCategoryName, newCuisineName);
            nutritionService.updateRecipeWithNutrition(savedRecipe);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe created successfully.");
            return "redirect:/recipes/" + savedRecipe.getId();
        }
    }
}
