package org.example.recipeapp.service;


import org.example.recipeapp.domain.Role;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findOrCreateAdminUser() {
        return userRepository.findByUsername("admin").orElseGet(() -> {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@recipetoday.com");
            admin.setPassword(passwordEncoder.encode("adminpassword"));
            admin.setRole(Role.ADMIN);
            return userRepository.save(admin);
        });
    }
}
