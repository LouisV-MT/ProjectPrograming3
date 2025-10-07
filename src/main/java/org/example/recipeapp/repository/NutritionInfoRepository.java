package org.example.recipeapp.repository;

import org.example.recipeapp.domain.NutritionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NutritionInfoRepository extends JpaRepository<NutritionInfo, Integer> {
}