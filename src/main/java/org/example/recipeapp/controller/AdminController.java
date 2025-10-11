package org.example.recipeapp.controller;

import org.example.recipeapp.domain.Role;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.repository.UserRepository;
import org.example.recipeapp.repository.RecipeRepository;
import org.example.recipeapp.service.MealDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private MealDbService mealDbService;

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
        List<Recipe> recipes = recipeRepository.findAll();
        model.addAttribute("recipes", recipes != null ? recipes : List.of());
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
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "🗑️ Recipe deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Recipe not found.");
        }
        return "redirect:/admin/recipes";
    }

    // ✅ 显示编辑页面
    @GetMapping("/admin/recipes/edit/{id}")
    public String showEditRecipeForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Recipe recipe = recipeRepository.findById(id).orElse(null);
        if (recipe == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Recipe not found.");
            return "redirect:/admin/recipes";
        }
        model.addAttribute("recipe", recipe);
        return "admin-edit-recipe";
    }

    // ✅ 更新菜谱
    @PostMapping("/admin/recipes/update/{id}")
    public String updateRecipe(@PathVariable Integer id,
                               Recipe updatedRecipe,
                               RedirectAttributes redirectAttributes) {
        Recipe existingRecipe = recipeRepository.findById(id).orElse(null);
        if (existingRecipe == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Recipe not found.");
            return "redirect:/admin/recipes";
        }

        existingRecipe.setName(updatedRecipe.getName());
        existingRecipe.setInstructions(updatedRecipe.getInstructions());
        existingRecipe.setImageUrl(updatedRecipe.getImageUrl());
        recipeRepository.save(existingRecipe);

        redirectAttributes.addFlashAttribute("successMessage",
                "✅ Recipe updated successfully: " + existingRecipe.getName());
        return "redirect:/admin/recipes";
    }
}
