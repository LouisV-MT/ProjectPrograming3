package org.example.recipeapp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "favorite_recipes", schema = "recipe_app")
public class FavoriteRecipe {
    @EmbeddedId
    private FavoriteRecipeId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private org.example.recipeapp.domain.User user;

    @MapsId("recipeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "recipe_id", nullable = false)
    private org.example.recipeapp.domain.Recipe recipe;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "favorited_at")
    private LocalDateTime favoritedAt;

}