package org.example.recipeapp.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor

public class EdamamRequestDto {
    private String title;
    private java.util.List<String> ingr;

}
