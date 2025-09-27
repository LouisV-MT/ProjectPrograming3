package org.example.recipeapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "recipes", schema = "recipe_app")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Lob
    @Column(name = "instructions", nullable = false)
    private String instructions;

    @Size(max = 255)
    @Column(name = "image_url")
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "video_url")
    private String videoUrl;

    @Size(max = 255)
    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "calories")
    private Integer calories;

    @ColumnDefault("0.00")
    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "author_id")
    private org.example.recipeapp.domain.User author;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}