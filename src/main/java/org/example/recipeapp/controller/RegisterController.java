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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 🧩 用户名已存在
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }

        // 🧩 邮箱已存在
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email already registered!");
            return "register";
        }

        // 🧩 检查密码长度
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register";
        }

        // ✅ 保存用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(Role.USER);
        userRepository.save(newUser);

        // ✅ 注册成功消息
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login";
    }
}
