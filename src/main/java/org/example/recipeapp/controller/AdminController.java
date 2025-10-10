package org.example.recipeapp.controller;

import org.example.recipeapp.domain.Role;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

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
                redirectAttributes.addFlashAttribute("successMessage", "⚠️ Cannot delete the admin account!");
            } else {
                userRepository.delete(user);
                redirectAttributes.addFlashAttribute("successMessage", "✅ User deleted successfully!");
            }
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
        }
        return "redirect:/admin/users";
    }

    // ✅ 显示食谱管理页面
    @GetMapping("/admin/recipes")
    public String showRecipeManagementPage() {
        return "admin-recipes"; // 对应 templates/admin-recipes.html
    }

}
