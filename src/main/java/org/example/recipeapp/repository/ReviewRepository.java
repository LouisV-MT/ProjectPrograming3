package org.example.recipeapp.repository;

import org.example.recipeapp.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
}