package org.example.recipeapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class FavoriteRecipeId implements Serializable {
    private static final long serialVersionUID = 6764417443798455539L;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @Column(name = "recipe_id", nullable = false)
    private Integer recipeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        FavoriteRecipeId entity = (FavoriteRecipeId) o;
        return Objects.equals(this.userId, entity.userId) &&
                Objects.equals(this.recipeId, entity.recipeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, recipeId);
    }

}