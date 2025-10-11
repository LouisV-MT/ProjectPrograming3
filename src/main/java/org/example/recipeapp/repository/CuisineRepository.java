package org.example.recipeapp.repository;

import org.example.recipeapp.domain.Cuisine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuisineRepository extends JpaRepository<Cuisine, Integer> {
        Optional<Cuisine> findByNameIgnoreCase(String name);


}
