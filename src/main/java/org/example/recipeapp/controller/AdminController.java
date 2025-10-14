package org.example.recipeapp.controller;

import org.example.recipeapp.domain.Role;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.repository.UserRepository;
import org.example.recipeapp.service.MealDbService;
import org.example.recipeapp.service.NutritionService;
import org.example.recipeapp.service.RecipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final MealDbService mealDbService;
    private final RecipeService recipeService;
    private final NutritionService nutritionService;

    public AdminController(UserRepository userRepository, MealDbService mealDbService, RecipeService recipeService, NutritionService nutritionService) {
        this.userRepository = userRepository;
        this.mealDbService = mealDbService;
        this.recipeService = recipeService;
        this.nutritionService = nutritionService;
    }

    // ✅ Dashboard 首页
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    // ✅ 用户管理页
    @GetMapping("/admin/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin-users";
    }

    // ✅ 删除用户
    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            if ("admin".equalsIgnoreCase(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Cannot delete the main admin account!");
            } else {
                userRepository.delete(user);
                redirectAttributes.addFlashAttribute("successMessage", "✅ User deleted successfully!");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ User not found.");
        }
        return "redirect:/admin/users";
    }

    // ✅ 升级用户为 Admin
    @PostMapping("/admin/make-admin/{id}")
    public String makeUserAdmin(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(Role.ADMIN);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "👑 " + user.getUsername() + " has been promoted to admin!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ User not found.");
        }
        return "redirect:/admin/users";
    }

    // ✅ 显示食谱管理页面
    @GetMapping("/admin/recipes")
    public String showRecipeManagementPage(Model model) {
        List<Recipe> recipes = recipeService.findAll();
        recipeService.addPresignedUrlsToRecipes(recipes);
        model.addAttribute("recipes", recipes);
        return "admin-recipes";
    }

    // ✅ 从 TheMealDB 导入菜谱
    @GetMapping("/admin/recipes/import/{id}")
    public String importRecipe(@PathVariable String id,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Please login as admin to import recipes.");
            return "redirect:/login";
        }

        User author = userRepository.findByUsername(principal.getName()).orElse(null);
        if (author == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Current user not found.");
            return "redirect:/admin/recipes";
        }

        Recipe imported = mealDbService.importRecipeById(id, author);
        if (imported != null) {
            nutritionService.updateRecipeWithNutrition(imported);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Recipe imported successfully: " + imported.getName());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "⚠️ Failed to import recipe or recipe already exists.");
        }
        return "redirect:/admin/recipes";
    }

    // ✅ 删除菜谱
    @PostMapping("/admin/recipes/delete/{id}")
    public String deleteRecipe(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        recipeService.deleteRecipe(id);
        redirectAttributes.addFlashAttribute("successMessage", "🗑️ Recipe deleted successfully!");
        return "redirect:/admin/recipes";
    }

    // ✅ 显示编辑页面
    @GetMapping("/admin/recipes/edit/{id}")
    public String showEditRecipeForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Recipe recipe = recipeService.findRecipeById(id).orElse(null);
        if (recipe == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Recipe not found.");
            return "redirect:/admin/recipes";
        }
        model.addAttribute("recipe", recipe);
        model.addAttribute("categories", recipeService.findAllCategories());
        model.addAttribute("cuisines", recipeService.findAllCuisines());
        return "admin-edit-recipe";
    }

    // ✅ 更新菜谱
    @PostMapping("/admin/recipes/update")
    public String updateRecipe(@ModelAttribute Recipe recipe,
                               @RequestParam(name = "newCategoryName", required = false) String newCategoryName,
                               @RequestParam(name = "newCuisineName", required = false) String newCuisineName,
                               RedirectAttributes redirectAttributes) {

        Recipe existingRecipe = recipeService.findRecipeById(recipe.getId())
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found for update."));

        existingRecipe.setName(recipe.getName());
        existingRecipe.setInstructions(recipe.getInstructions());
        existingRecipe.setCategory(recipe.getCategory());
        existingRecipe.setCuisine(recipe.getCuisine());
        existingRecipe.setSourceUrl(recipe.getSourceUrl());
        existingRecipe.setVideoUrl(recipe.getVideoUrl());
        existingRecipe.setRecipeIngredients(recipe.getRecipeIngredients());

        recipeService.save(existingRecipe, newCategoryName, newCuisineName);

        redirectAttributes.addFlashAttribute("successMessage",
                "✅ Recipe updated successfully: " + existingRecipe.getName());
        return "redirect:/admin/recipes";
    }
}