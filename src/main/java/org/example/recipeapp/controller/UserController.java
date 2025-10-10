package org.example.recipeapp.controller;

import org.example.recipeapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    //For admin: list all user
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    //For admin: delete an user
    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(Integer id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }

}
