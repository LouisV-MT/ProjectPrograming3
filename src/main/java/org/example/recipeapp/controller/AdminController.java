package org.example.recipeapp.controller;

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

    // ✅ 显示所有用户
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin-dashboard";
    }

    // ✅ 删除用户 + 显示绿色成功提示
    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "✅ User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("successMessage", "⚠️ Failed to delete user.");
        }
        return "redirect:/admin/dashboard";
    }
}
