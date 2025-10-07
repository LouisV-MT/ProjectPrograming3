package org.example.recipeapp.controller;

import org.example.recipeapp.domain.Role;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 显示注册页面
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // 对应 templates/register.html
    }

    // 处理注册表单提交
    @PostMapping("/register")
    public String processRegister(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(Role.USER);
        userRepository.save(newUser);

        model.addAttribute("success", "Registration successful! Please login.");
        return "redirect:/login"; // ✅ 注册后自动跳转登录页
    }
}
