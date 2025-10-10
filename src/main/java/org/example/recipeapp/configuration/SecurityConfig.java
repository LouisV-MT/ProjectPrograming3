package org.example.recipeapp.configuration;

import org.example.recipeapp.service.JpaUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JpaUserDetailsService userDetailsService;

    // ✅ 密码加密方式
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 注册 JPA 用户服务
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ✅ 主安全配置
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/register", "/login", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )

                // ✅ 登录页配置
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        // 登录成功后根据角色跳转
                        .successHandler((request, response, authentication) -> {
                            var authorities = authentication.getAuthorities();
                            boolean isAdmin = authorities.stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            if (isAdmin) {
                                response.sendRedirect("/admin/dashboard");
                            } else {
                                response.sendRedirect("/home");
                            }
                        })
                )

                // ✅ 登出配置
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                // ✅ 使用 JPA 认证
                .authenticationProvider(authenticationProvider())

                // ⚠️ 暂时禁用 CSRF（后期可打开）
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // ✅ AuthenticationManager（备用）
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
