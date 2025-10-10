package org.example.recipeapp.controller;

import org.example.recipeapp.domain.Recipe;
import org.example.recipeapp.service.RecipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    private final RecipeService recipeService;

    public  HomeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/home")
    public String home(@RequestParam(value = "category", required = false) String category, // Add this
                       @RequestParam(value = "cuisine", required = false) String cuisine, // Add this
                       Model model, Principal principal) {
        // ✅ 获取当前登录用户的用户名
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            List<Recipe> filteredRecipes=recipeService.filterRecipes(category, cuisine);
            List<String> categories= recipeService.findAllCategories();
            List<String> cuisines= recipeService.findAllCuisines();

            model.addAttribute("recipes", filteredRecipes);
            model.addAttribute("categories", categories);
            model.addAttribute("cuisines", cuisines);

            return "home"; // 对应 templates/home.html
        }
        return "redirect:/login"; // redirect to login page if not logged in
    }
}

