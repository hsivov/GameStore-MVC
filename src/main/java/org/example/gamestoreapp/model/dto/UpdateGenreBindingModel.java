package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.Size;

public class UpdateGenreBindingModel {
    private long id;
    @Size(min = 3, max = 20)
    private String name;
    @Size(min = 5, max = 500)
    private String description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
