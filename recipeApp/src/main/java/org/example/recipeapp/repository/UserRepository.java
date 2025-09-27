package org.example.recipeapp.repository;

import org.example.recipeapp.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User loadUserByUsername(String username);
    boolean existsByUsername(String username);
}
