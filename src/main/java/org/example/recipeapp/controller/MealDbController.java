package org.example.recipeapp.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
public class MealDbController {

    private final WebClient mealDbWebClient;

    public MealDbController(@Qualifier("mealDbWebClient") WebClient mealDbWebClient) {
        this.mealDbWebClient = mealDbWebClient;
    }

    // ✅ Step 1: 初始页面加载
    @GetMapping("/admin/mealdb")
    public String showMealDbImportPage(Model model) {
        String categoriesJson = mealDbWebClient.get()
                .uri("/categories.php")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String areasJson = mealDbWebClient.get()
                .uri("/list.php?a=list")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        model.addAttribute("categoriesJson", categoriesJson != null ? categoriesJson : "{}");
        model.addAttribute("areasJson", areasJson != null ? areasJson : "{}");
        model.addAttribute("mealsJson", "{}");
        return "admin-mealdb";
    }

    // ✅ Step 2: 选择分类/地区后加载菜谱
    @GetMapping("/admin/mealdb/filter")
    public String filterMeals(@RequestParam(required = false) String category,
                              @RequestParam(required = false) String area,
                              Model model) {

        // 确定 endpoint
        String endpoint = (category != null && !category.isEmpty())
                ? "/filter.php?c=" + category
                : "/filter.php?a=" + area;

        // 拉取菜谱
        String mealsJson = mealDbWebClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 保证下拉选项持续存在
        String categoriesJson = mealDbWebClient.get()
                .uri("/categories.php")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String areasJson = mealDbWebClient.get()
                .uri("/list.php?a=list")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        model.addAttribute("categoriesJson", categoriesJson != null ? categoriesJson : "{}");
        model.addAttribute("areasJson", areasJson != null ? areasJson : "{}");
        model.addAttribute("mealsJson", mealsJson != null ? mealsJson : "{}");
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedArea", area);

        return "admin-mealdb";
    }
}
