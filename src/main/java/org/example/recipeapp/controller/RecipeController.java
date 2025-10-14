package org.example.recipeapp.controller;

import org.example.recipeapp.domain.Category;
import org.example.recipeapp.domain.Cuisine;
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


    @GetMapping("/{id}")
    public String showRecipe(@PathVariable Integer id, Model model){
        Optional<Recipe> recipeOptional= recipeService.findRecipeById(id);
        if(recipeOptional.isPresent()){
            Recipe recipe= recipeOptional.get();
            List<String> instructionSteps = recipeService.parseInstructions(recipe.getInstructions());
            model.addAttribute("instructionSteps", instructionSteps);
            recipeService.addPresignedUrlsToRecipes(List.of(recipe));
            model.addAttribute("recipe",recipe);
            return  "recipes/detail";
        } else {
            System.err.println("Recipe not found");
            return "redirect:/home";
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
                                     @RequestParam(name = "categoryId", required = false) String categoryId,
                                     @RequestParam(name = "cuisineId", required = false) String cuisineId,
                                     @RequestParam(name = "newCategoryName", required = false) String newCategoryName,
                                     @RequestParam(name = "newCuisineName", required = false) String newCuisineName,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) throws IOException {

        if (categoryId != null && !categoryId.equals("NEW") && !categoryId.isEmpty()) {
            recipeService.findCategoryById(Integer.parseInt(categoryId))
                    .ifPresent(recipe::setCategory);
        } else {
            recipe.setCategory(new Category());
        }

        if (cuisineId != null && !cuisineId.equals("NEW") && !cuisineId.isEmpty()) {
            recipeService.findCuisineById(Integer.parseInt(cuisineId))
                    .ifPresent(recipe::setCuisine);
        } else {
            recipe.setCuisine(new Cuisine());
        }

        User currentUser = (User) authentication.getPrincipal();

        if(!imageFile.isEmpty()){
            String contentType= imageFile.getContentType();
            if(contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))){
                redirectAttributes.addFlashAttribute("errorMessage","Invalid image file type, only JPEG or PNG");
                return "redirect:" + (recipe.getId() != null ? "/recipes/edit/" + recipe.getId() : "/recipes/create");
            }
        }

        if (recipe.getId() != null) {
            //  UPDATE LOGIC ---
            Recipe existingRecipe = recipeService.findRecipeById(recipe.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Recipe not found."));

            if (existingRecipe.getAuthor() == null || !existingRecipe.getAuthor().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You do not have permission to edit this recipe.");
                return "redirect:/home";
            }

            String oldImageUrl = existingRecipe.getImageUrl();
            if (!imageFile.isEmpty()) {
                String recipeName = toTitleCase(recipe.getName());
                String author = toTitleCase(currentUser.getUsername());
                String extension = getFileExtension(imageFile.getOriginalFilename());
                String newFileName = "recipeOf" + recipeName + "By" + author + extension;
                String newImageUrl = imageStorageService.upload(imageFile, newFileName);
                recipe.setImageUrl(newImageUrl);
            } else {
                recipe.setImageUrl(oldImageUrl);
            }

            Recipe savedRecipe = recipeService.save(recipe, newCategoryName, newCuisineName);

            if (oldImageUrl != null && !imageFile.isEmpty() && !oldImageUrl.equals(savedRecipe.getImageUrl())) {
                recipeService.deleteRecipeImage(oldImageUrl);
            }

            nutritionService.updateRecipeWithNutrition(savedRecipe);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe updated successfully.");
            return "redirect:/recipes/" + savedRecipe.getId();

        } else {
            // --- CREATE LOGIC ---
            if (!imageFile.isEmpty()) {
                String recipeName = toTitleCase(recipe.getName());
                String author = toTitleCase(currentUser.getUsername());
                String extension = getFileExtension(imageFile.getOriginalFilename());
                String newFileName = "recipeof" + recipeName + "By" + author + extension;
                String imageUrl = imageStorageService.upload(imageFile, newFileName);
                recipe.setImageUrl(imageUrl);
            }
            recipe.setAuthor(currentUser);

            Recipe savedRecipe = recipeService.save(recipe, newCategoryName, newCuisineName);
            nutritionService.updateRecipeWithNutrition(savedRecipe);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe created successfully.");
            return "redirect:/recipes/" + savedRecipe.getId();
        }
    }
}