package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.Size;

public class AddGenreBindingModel {
    @Size(min = 3, max = 20)
    String name;
    @Size(min = 5, max = 500)
    String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
