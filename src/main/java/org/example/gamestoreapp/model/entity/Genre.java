package org.example.gamestoreapp.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "genres")
public class Genre extends BaseEntity{

    @Column(nullable = false,unique = true)
    private String name;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

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
