package org.example.recipeapp.configuration;

import jakarta.annotation.PostConstruct;
import org.example.recipeapp.domain.Role;
import org.example.recipeapp.domain.User;
import org.example.recipeapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ 项目启动后自动执行
    @PostConstruct
    public void init() {
        // 如果还没有管理员，就创建一个
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@recipeapp.com");
            admin.setPassword(passwordEncoder.encode("admin123")); // ✅ 密码加密存储
            admin.setRole(Role.ADMIN); // ✅ 指定角色

            userRepository.save(admin);
            System.out.println("✅ Default admin created: username=admin, password=admin123");
        } else {
            System.out.println("ℹ️ Admin already exists, skipping creation.");
        }
    }
}
