package org.example.recipeapp.repository;

import org.example.recipeapp.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);   // ✅ 改成 Optional
    boolean existsByUsername(String username);
}
