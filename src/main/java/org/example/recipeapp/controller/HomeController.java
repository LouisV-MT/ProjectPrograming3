package org.example.recipeapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        // ✅ 获取当前登录用户的用户名
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "home"; // 对应 templates/home.html
    }
}

