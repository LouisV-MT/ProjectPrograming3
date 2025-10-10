package org.example.recipeapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    // 🚫 处理访问被拒绝的情况
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied"; // 对应 templates/access-denied.html
    }
}
